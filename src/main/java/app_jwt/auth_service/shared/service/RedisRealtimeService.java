package app_jwt.auth_service.shared.service;

import app_jwt.auth_service.modules.bus.domain.model.Bus;
import app_jwt.auth_service.modules.bus.infrastructure.adapter.input.rest.dto.BusLocationEvent;
import app_jwt.auth_service.shared.domain.model.Empresa;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisRealtimeService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    private static final String BUS_KEY_PREFIX = "empresa:%d:bus:%d";
    private static final String EMPRESA_KEY_PREFIX = "empresa:%d";

    /**
     * Publica la ubicación de un bus en Redis y la envía por WebSocket.
     */
    public void publishBusLocation(Long empresaId, Long busId, Object locationData) {
        String redisKey = String.format(BUS_KEY_PREFIX + ":location", empresaId, busId);
        
        // 1. Guardar última ubicación conocida en Redis (Cache)
        redisTemplate.opsForValue().set(redisKey, locationData);
        
        // 2. Enviar directamente al WebSocket (Topic STOMP)
        broadcastToWebSockets(empresaId, busId, locationData);
        
        log.debug("Ubicación publicada para bus {} en WebSockets", busId);
    }

    /**
     * Guarda o actualiza el estado completo de un bus en el caché de Redis.
     */
    public void upsertBus(Bus bus) {
        String redisKey = String.format(BUS_KEY_PREFIX + ":data", bus.getEmpresaId(), bus.getId());
        
        BusLocationEvent event = BusLocationEvent.builder()
                .busId(bus.getId())
                .placa(bus.getPlaca())
                .latitud(bus.getLatitud())
                .longitud(bus.getLongitud())
                .velocidad(bus.getVelocidad())
                .estado(bus.getEstado().name())
                .timestamp(bus.getUltimaUbicacion())
                .empresaId(bus.getEmpresaId())
                .rutaId(bus.getRutaAsignada() != null ? bus.getRutaAsignada().getId() : null)
                .build();

        redisTemplate.opsForValue().set(redisKey, event);
        
        // Notificar cambio de estado por WebSocket
        broadcastToWebSockets(bus.getEmpresaId(), bus.getId(), event);
        
        log.info("Bus {} (estado: {}) actualizado en Redis Realtime", bus.getPlaca(), bus.getEstado());
    }

    /**
     * Elimina un bus del caché de tiempo real.
     */
    public void removeBus(Long empresaId, Long busId) {
        String dataKey = String.format(BUS_KEY_PREFIX + ":data", empresaId, busId);
        String locKey = String.format(BUS_KEY_PREFIX + ":location", empresaId, busId);
        redisTemplate.delete(dataKey);
        redisTemplate.delete(locKey);
        
        Map<String, Object> deleteEvent = new HashMap<>();
        deleteEvent.put("busId", busId);
        deleteEvent.put("deleted", true);
        
        broadcastToWebSockets(empresaId, busId, deleteEvent);
    }

    /**
     * Guarda o actualiza una empresa en Redis.
     */
    public void upsertEmpresa(Empresa empresa) {
        String redisKey = String.format(EMPRESA_KEY_PREFIX, empresa.getId());
        redisTemplate.opsForValue().set(redisKey, empresa);
        log.info("Empresa {} actualizada en Redis Realtime", empresa.getNombre());
    }

    private void broadcastToWebSockets(Long empresaId, Long busId, Object data) {
        // Topic por empresa
        messagingTemplate.convertAndSend("/topic/empresa/" + empresaId + "/buses", data);
        // Topic individual por bus
        messagingTemplate.convertAndSend("/topic/bus/" + busId, data);
    }
}
