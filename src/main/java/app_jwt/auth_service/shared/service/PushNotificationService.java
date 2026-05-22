package app_jwt.auth_service.shared.service;

import app_jwt.auth_service.modules.bus.domain.model.EstadoSenal;
import app_jwt.auth_service.modules.bus.infrastructure.adapter.input.rest.dto.SenalNotificacionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PushNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void sendSignalAlertToAdmin(Long empresaId, Long busId, String placa, EstadoSenal tipo, String mensaje) {
        Map<String, Object> notification = buildNotification(busId, placa, tipo, mensaje, "ADMIN");

        messagingTemplate.convertAndSend("/topic/empresa/" + empresaId + "/senales", notification);
        log.info("Push notification sent to /topic/empresa/{}/senales for bus {}", empresaId, placa);
    }

    public void sendSignalAlertToConductor(Long conductorId, Long busId, String placa, EstadoSenal tipo, String mensaje) {
        Map<String, Object> notification = buildNotification(busId, placa, tipo, mensaje, "CONDUCTOR");

        messagingTemplate.convertAndSend("/topic/conductor/" + conductorId + "/senales", notification);
        log.info("Push notification sent to /topic/conductor/{}/senales for bus {}", conductorId, placa);
    }

    public void sendSignalAlertToBus(Long busId, Long empresaId, String placa, EstadoSenal tipo, String mensaje) {
        Map<String, Object> notification = buildNotification(busId, placa, tipo, mensaje, "BUS");

        messagingTemplate.convertAndSend("/topic/bus/" + busId + "/senal", notification);
        log.info("Push notification sent to /topic/bus/{}/senal", busId);
    }

    public void broadcastSenalAlertToEmpresa(Long empresaId, SenalNotificacionEvent event) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("id", System.currentTimeMillis());
        notification.put("busId", event.getBusId());
        notification.put("placa", event.getPlaca());
        notification.put("tipo", event.getTipo().name());
        notification.put("mensaje", event.getMensaje());
        notification.put("latitud", event.getLatitud());
        notification.put("longitud", event.getLongitud());
        notification.put("timestamp", LocalDateTime.now().format(FORMATTER));
        notification.put("leida", false);
        notification.put("canal", "SENAL");

        if (event.getTipo() == EstadoSenal.SIN_SEÑAL) {
            notification.put("titulo", "🚨 Alerta de Señal");
            notification.put("descripcion", "El bus " + event.getPlaca() + " perdió señal GPS");
        } else {
            notification.put("titulo", "✅ Señal Recuperada");
            notification.put("descripcion", "El bus " + event.getPlaca() + " recuperó señal GPS");
        }

        messagingTemplate.convertAndSend("/topic/empresa/" + empresaId + "/notificaciones", notification);
        log.info("Broadcast senal alert to empresa {}: bus {}", empresaId, event.getPlaca());
    }

    private Map<String, Object> buildNotification(Long busId, String placa, EstadoSenal tipo, String mensaje, String canal) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("id", System.currentTimeMillis());
        notification.put("busId", busId);
        notification.put("placa", placa);
        notification.put("tipo", tipo.name());
        notification.put("mensaje", mensaje);
        notification.put("timestamp", LocalDateTime.now().format(FORMATTER));
        notification.put("canal", canal);

        if (tipo == EstadoSenal.SIN_SEÑAL) {
            notification.put("titulo", "🚨 Alerta de Señal");
            notification.put("descripcion", "El bus " + placa + " perdió señal GPS");
            notification.put("prioridad", "ALTA");
            notification.put("icono", "warning");
        } else {
            notification.put("titulo", "✅ Señal Recuperada");
            notification.put("descripcion", "El bus " + placa + " está en línea nuevamente");
            notification.put("prioridad", "INFO");
            notification.put("icono", "check_circle");
        }

        return notification;
    }
}