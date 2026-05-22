package app_jwt.auth_service.modules.bus.infrastructure.scheduler;

import app_jwt.auth_service.config.RabbitMQConfig;
import app_jwt.auth_service.modules.bus.domain.model.Bus;
import app_jwt.auth_service.modules.bus.domain.model.EstadoSenal;
import app_jwt.auth_service.modules.bus.infrastructure.adapter.input.rest.dto.SenalNotificacionEvent;
import app_jwt.auth_service.modules.bus.infrastructure.adapter.output.persistence.BusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BusSignalMonitor {

    private final BusRepository busRepository;
    private final RabbitTemplate rabbitTemplate;

    private static final int SEGUNDOS_SIN_SEÑAL = 10;

    @Scheduled(fixedRate = 10000)
    @Transactional
    public void verificarSeñales() {
        LocalDateTime threshold = LocalDateTime.now().minusSeconds(SEGUNDOS_SIN_SEÑAL);

        List<Bus> busesActivos = busRepository.findByActivoTrue();

        for (Bus bus : busesActivos) {
            if (bus.getUltimaUbicacion() == null) {
                continue;
            }

            if (bus.getUltimaUbicacion().isBefore(threshold) && bus.getEstadoSenal() != EstadoSenal.SIN_SEÑAL) {
                bus.setEstadoSenal(EstadoSenal.SIN_SEÑAL);
                busRepository.save(bus);
                publicarEventoSenal(bus);
                log.warn("Bus {} marcado como SIN_SEÑAL (última ubicación: {})",
                        bus.getPlaca(), bus.getUltimaUbicacion());
            }
        }

        log.debug("Verificación de señales completada");
    }

    private void publicarEventoSenal(Bus bus) {
        try {
            String mensaje = "El bus " + bus.getPlaca() + " ha perdido señal GPS. " +
                    "Última ubicación hace " + SEGUNDOS_SIN_SEÑAL + " segundo(s).";

            SenalNotificacionEvent event = SenalNotificacionEvent.builder()
                    .busId(bus.getId())
                    .placa(bus.getPlaca())
                    .empresaId(bus.getEmpresaId())
                    .tipo(EstadoSenal.SIN_SEÑAL)
                    .mensaje(mensaje)
                    .latitud(bus.getLatitud())
                    .longitud(bus.getLongitud())
                    .conductorId(bus.getConductorAsignado() != null ? bus.getConductorAsignado().getId() : null)
                    .timestamp(LocalDateTime.now())
                    .ultimaUbicacion(bus.getUltimaUbicacion())
                    .build();

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NOTIFICATIONS,
                    "notification.senal",
                    event
            );

            log.debug("Evento publicado a RabbitMQ para bus {} - SIN_SEÑAL", bus.getPlaca());
        } catch (Exception e) {
            log.error("Error al publicar evento de señal para bus {}: {}", bus.getPlaca(), e.getMessage());
        }
    }
}