package app_jwt.auth_service.modules.bus.infrastructure.adapter.output.messaging;

import app_jwt.auth_service.config.RabbitMQConfig;
import app_jwt.auth_service.modules.bus.domain.model.EstadoSenal;
import app_jwt.auth_service.modules.bus.infrastructure.adapter.input.rest.dto.SenalNotificacionEvent;
import app_jwt.auth_service.modules.bus.infrastructure.adapter.output.persistence.NotificacionSenalRepository;
import app_jwt.auth_service.modules.bus.infrastructure.adapter.output.persistence.entity.NotificacionSenal;
import app_jwt.auth_service.modules.conductor.infrastructure.adapter.output.persistence.ConductorRepository;
import app_jwt.auth_service.modules.conductor.domain.model.Conductor;
import app_jwt.auth_service.shared.domain.model.Empresa;
import app_jwt.auth_service.shared.domain.model.Usuario;
import app_jwt.auth_service.shared.infrastructure.persistence.EmpresaRepository;
import app_jwt.auth_service.shared.service.EmailService;
import app_jwt.auth_service.shared.service.PushNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class BusSignalNotificationConsumer {

    private final NotificacionSenalRepository notificacionSenalRepository;
    private final EmpresaRepository empresaRepository;
    private final ConductorRepository conductorRepository;
    private final EmailService emailService;
    private final PushNotificationService pushNotificationService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NOTIFICATIONS)
    @Transactional
    public void handleSenalNotificacionEvent(SenalNotificacionEvent event) {
        log.info("Recibiendo evento de señal: busId={}, tipo={}", event.getBusId(), event.getTipo());

        if (event.getTipo() != EstadoSenal.SIN_SEÑAL && event.getTipo() != EstadoSenal.EN_LINEA) {
            log.debug("Tipo de evento no es para notificación, ignorando");
            return;
        }

        NotificacionSenal notificacion = NotificacionSenal.builder()
                .busId(event.getBusId())
                .placa(event.getPlaca())
                .empresaId(event.getEmpresaId())
                .tipo(event.getTipo())
                .mensaje(event.getMensaje())
                .latitud(event.getLatitud())
                .longitud(event.getLongitud())
                .timestamp(LocalDateTime.now())
                .leida(false)
                .build();

        notificacionSenalRepository.save(notificacion);
        log.info("Notificación guardada para bus {} - {}", event.getPlaca(), event.getTipo());

        enviarNotificaciones(event);
    }

    private void enviarNotificaciones(SenalNotificacionEvent event) {
        Optional<Empresa> empresaOpt = empresaRepository.findById(event.getEmpresaId());
        String subject = construirAsunto(event);
        String body = construirCuerpo(event);

        if (empresaOpt.isPresent()) {
            Empresa empresa = empresaOpt.get();

            if (empresa.getEmail() != null && !empresa.getEmail().isEmpty()) {
                emailService.sendSignalAlert(empresa.getEmail(), subject, body);
                log.info("Email enviado a admin: {}", empresa.getEmail());
            }

            pushNotificationService.broadcastSenalAlertToEmpresa(event.getEmpresaId(), event);
        }

        Optional<Conductor> conductorOpt = conductorRepository.findByBusAsignadoIdAndActivoTrue(event.getBusId());
        if (conductorOpt.isPresent()) {
            Conductor conductor = conductorOpt.get();
            Usuario usuario = conductor.getUsuario();
            if (usuario != null && usuario.getCorreo() != null && !usuario.getCorreo().isEmpty()) {
                String subjectConductor = construirAsuntoParaConductor(event);
                String bodyConductor = construirCuerpoParaConductor(event);
                emailService.sendSignalAlert(usuario.getCorreo(), subjectConductor, bodyConductor);
                log.info("Email enviado a conductor: {} ({})", conductor.getUsuario().getNombre(), usuario.getCorreo());
            }

            pushNotificationService.sendSignalAlertToConductor(
                    conductor.getId(),
                    event.getBusId(),
                    event.getPlaca(),
                    event.getTipo(),
                    event.getMensaje()
            );
        }

        pushNotificationService.sendSignalAlertToBus(
                event.getBusId(),
                event.getEmpresaId(),
                event.getPlaca(),
                event.getTipo(),
                event.getMensaje()
        );
    }

    private String construirAsunto(SenalNotificacionEvent event) {
        if (event.getTipo() == EstadoSenal.SIN_SEÑAL) {
            return "🚨 Alerta: Bus " + event.getPlaca() + " perdió señal";
        } else {
            return "✅ Info: Bus " + event.getPlaca() + " recuperó señal";
        }
    }

    private String construirAsuntoParaConductor(SenalNotificacionEvent event) {
        if (event.getTipo() == EstadoSenal.SIN_SEÑAL) {
            return "🚨 Tu bus " + event.getPlaca() + " perdió señal GPS";
        } else {
            return "✅ Tu bus " + event.getPlaca() + " recuperó señal GPS";
        }
    }

    private String construirCuerpo(SenalNotificacionEvent event) {
        StringBuilder sb = new StringBuilder();
        sb.append("Notificación de Señal de Bus\n\n");
        sb.append("Placa: ").append(event.getPlaca()).append("\n");
        sb.append("Tipo: ").append(event.getTipo()).append("\n");
        sb.append("Fecha: ").append(LocalDateTime.now().format(FORMATTER)).append("\n\n");

        if (event.getTipo() == EstadoSenal.SIN_SEÑAL) {
            sb.append("El bus ha perdido señal GPS.\n");
            if (event.getUltimaUbicacion() != null) {
                sb.append("Última ubicación conocida: ").append(event.getUltimaUbicacion().format(FORMATTER)).append("\n");
            }
            if (event.getLatitud() != null && event.getLongitud() != null) {
                sb.append("Coordenadas: ").append(event.getLatitud()).append(", ").append(event.getLongitud()).append("\n");
            }
        } else {
            sb.append("El bus ha recuperado señal GPS y está en línea nuevamente.\n");
        }

        sb.append("\n--\nGeoRoute - Sistema de Monitoreo");
        return sb.toString();
    }

    private String construirCuerpoParaConductor(SenalNotificacionEvent event) {
        StringBuilder sb = new StringBuilder();
        sb.append("Notificación de Señal de Bus\n\n");

        if (event.getTipo() == EstadoSenal.SIN_SEÑAL) {
            sb.append("⚠️ ALERTA: Tu bus ha perdido señal GPS.\n\n");
            sb.append("Placa: ").append(event.getPlaca()).append("\n");
            sb.append("Fecha: ").append(LocalDateTime.now().format(FORMATTER)).append("\n\n");
            sb.append("Por favor, verifica lo siguiente:\n");
            sb.append("- Que el dispositivo GPS esté encendido\n");
            sb.append("- Que tenga batería suficiente\n");
            sb.append("- Que esté en un lugar con buena vista al cielo\n\n");
            if (event.getUltimaUbicacion() != null) {
                sb.append("Última ubicación conocida: ").append(event.getUltimaUbicacion().format(FORMATTER)).append("\n");
            }
            if (event.getLatitud() != null && event.getLongitud() != null) {
                sb.append("Coordenadas: ").append(event.getLatitud()).append(", ").append(event.getLongitud()).append("\n");
            }
        } else {
            sb.append("✅ Tu bus ha recuperado señal GPS y está en línea nuevamente.\n\n");
            sb.append("Placa: ").append(event.getPlaca()).append("\n");
            sb.append("Fecha: ").append(LocalDateTime.now().format(FORMATTER)).append("\n\n");
            sb.append("El monitoreo está activo nuevamente.\n");
        }

        sb.append("\n--\nGeoRoute - Sistema de Monitoreo");
        return sb.toString();
    }
}