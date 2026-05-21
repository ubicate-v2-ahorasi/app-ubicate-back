package app_jwt.auth_service.shared.service;

import app_jwt.auth_service.modules.bus.domain.model.Bus;
import app_jwt.auth_service.modules.bus.infrastructure.adapter.input.rest.dto.BusLocationEvent;
import app_jwt.auth_service.modules.route.infrastructure.adapter.input.rest.dto.BusPositionDTO;
import app_jwt.auth_service.shared.domain.model.Empresa;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisRealtimeService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    private static final String BUS_KEY_PREFIX = "empresa:%d:bus:%d";
    private static final String EMPRESA_KEY_PREFIX = "empresa:%d";
    private static final String RUTA_KEY_PREFIX = "ruta:%d:buses";
    private static final long RUTA_INDEX_TTL_HOURS = 24;

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

        BusLocationEvent event = BusLocationEvent.create(
                bus.getId(),
                bus.getPlaca(),
                bus.getLatitud(),
                bus.getLongitud(),
                bus.getVelocidad(),
                bus.getEstado().name(),
                bus.getUltimaUbicacion(),
                bus.getEmpresaId(),
                bus.getRutaAsignada() != null ? bus.getRutaAsignada().getId() : null
        );

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

    public void upsertBusToRutaIndex(Bus bus) {
        if (bus.getRutaAsignada() == null) {
            return;
        }
        Long rutaId = bus.getRutaAsignada().getId();
        String rutaKey = String.format(RUTA_KEY_PREFIX, rutaId);

        BusPositionDTO position = BusPositionDTO.from(
                bus.getId(),
                bus.getPlaca(),
                bus.getLatitud(),
                bus.getLongitud(),
                bus.getVelocidad(),
                bus.getEstado().name(),
                bus.getUltimaUbicacion()
        );

        try {
            String jsonValue = objectMapper.writeValueAsString(position);
            redisTemplate.opsForValue().set(rutaKey + ":" + bus.getId(), jsonValue);
            redisTemplate.expire(rutaKey, java.time.Duration.ofHours(RUTA_INDEX_TTL_HOURS));

            messagingTemplate.convertAndSend("/topic/ruta/" + rutaId + "/buses", position);
            log.debug("Bus {} agregado al índice de ruta {}", bus.getId(), rutaId);
        } catch (JsonProcessingException e) {
            log.error("Error serializando BusPositionDTO: {}", e.getMessage());
        }
    }

    public void removeBusFromRutaIndex(Long rutaId, Long busId) {
        String key = String.format(RUTA_KEY_PREFIX, rutaId) + ":" + busId;
        redisTemplate.delete(key);

        Map<String, Object> removal = new HashMap<>();
        removal.put("busId", busId);
        removal.put("removed", true);
        messagingTemplate.convertAndSend("/topic/ruta/" + rutaId + "/buses", removal);
        log.debug("Bus {} removido del índice de ruta {}", busId, rutaId);
    }

    public List<BusPositionDTO> getBusesPosicionByRuta(Long rutaId) {
        String pattern = String.format(RUTA_KEY_PREFIX, rutaId) + ":*";
        Set<String> keys = redisTemplate.keys(pattern);

        if (keys == null || keys.isEmpty()) {
            return Collections.emptyList();
        }

        List<BusPositionDTO> positions = new ArrayList<>();
        for (String key : keys) {
            Object value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                try {
                    if (value instanceof String jsonStr) {
                        positions.add(objectMapper.readValue(jsonStr, BusPositionDTO.class));
                    } else {
                        positions.add(objectMapper.convertValue(value, BusPositionDTO.class));
                    }
                } catch (Exception e) {
                    log.error("Error deserializando bus position: {}", e.getMessage());
                }
            }
        }
        return positions;
    }

    public void syncRutaIndex(Long rutaId, List<Bus> buses) {
        String rutaKey = String.format(RUTA_KEY_PREFIX, rutaId);

        Set<String> existingKeys = redisTemplate.keys(rutaKey + ":*");
        if (existingKeys != null && !existingKeys.isEmpty()) {
            redisTemplate.delete(existingKeys);
        }

        for (Bus bus : buses) {
            if (bus.getActivo() != null && bus.getActivo()) {
                BusPositionDTO position = BusPositionDTO.from(
                        bus.getId(),
                        bus.getPlaca(),
                        bus.getLatitud(),
                        bus.getLongitud(),
                        bus.getVelocidad(),
                        bus.getEstado().name(),
                        bus.getUltimaUbicacion()
                );
                try {
                    String jsonValue = objectMapper.writeValueAsString(position);
                    redisTemplate.opsForValue().set(rutaKey + ":" + bus.getId(), jsonValue);
                } catch (JsonProcessingException e) {
                    log.error("Error serializando BusPositionDTO para bus {}: {}", bus.getId(), e.getMessage());
                }
            }
        }
        redisTemplate.expire(rutaKey, java.time.Duration.ofHours(RUTA_INDEX_TTL_HOURS));
        log.info("Índice de ruta {} sincronizado con {} buses", rutaId, buses.size());
    }
}
