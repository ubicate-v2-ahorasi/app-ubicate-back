package app_jwt.auth_service.modules.bus.infrastructure.scheduler;

import app_jwt.auth_service.modules.bus.domain.model.Bus;
import app_jwt.auth_service.modules.bus.domain.model.EstadoSenal;
import app_jwt.auth_service.modules.bus.infrastructure.adapter.output.persistence.BusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private static final int MINUTOS_SIN_SEÑAL = 2;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void verificarSeñales() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(MINUTOS_SIN_SEÑAL);

        List<Bus> busesActivos = busRepository.findByActivoTrue();

        for (Bus bus : busesActivos) {
            if (bus.getUltimaUbicacion() == null) {
                continue;
            }

            boolean necesitaActualizar = false;

            if (bus.getUltimaUbicacion().isBefore(threshold)) {
                if (bus.getEstadoSenal() != EstadoSenal.SIN_SEÑAL) {
                    bus.setEstadoSenal(EstadoSenal.SIN_SEÑAL);
                    necesitaActualizar = true;
                    log.warn("Bus {} marcado como SIN_SEÑAL (última ubicación: {})",
                            bus.getPlaca(), bus.getUltimaUbicacion());
                }
            } else {
                if (bus.getEstadoSenal() != EstadoSenal.EN_LINEA) {
                    bus.setEstadoSenal(EstadoSenal.EN_LINEA);
                    necesitaActualizar = true;
                    log.info("Bus {} volvió a EN_LINEA", bus.getPlaca());
                }
            }

            if (necesitaActualizar) {
                busRepository.save(bus);
            }
        }
    }
}