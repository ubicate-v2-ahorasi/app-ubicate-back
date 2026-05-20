package app_jwt.auth_service.modules.bus.infrastructure.adapter.output.messaging;

import app_jwt.auth_service.config.RabbitMQConfig;
import app_jwt.auth_service.modules.bus.infrastructure.adapter.input.rest.dto.BusLocationEvent;
import app_jwt.auth_service.modules.bus.infrastructure.adapter.output.persistence.BusLocationHistoryRepository;
import app_jwt.auth_service.modules.bus.infrastructure.adapter.output.persistence.entity.BusLocationHistory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LocationHistoryConsumer {

    private final BusLocationHistoryRepository historyRepository;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_LOCATION_HISTORY)
    public void handleLocationEvent(BusLocationEvent event) {
        log.debug("Recibiendo evento de ubicación para historial: busId={}, placa={}", 
                event.getBusId(), event.getPlaca());

        BusLocationHistory history = BusLocationHistory.builder()
                .busId(event.getBusId())
                .placa(event.getPlaca())
                .latitud(event.getLatitud())
                .longitud(event.getLongitud())
                .velocidad(event.getVelocidad())
                .estado(event.getEstado())
                .empresaId(event.getEmpresaId())
                .rutaId(event.getRutaId())
                .timestamp(event.getTimestamp())
                .build();

        historyRepository.save(history);
        log.debug("Historial guardado para bus {}: ({}, {})", event.getPlaca(), event.getLatitud(), event.getLongitud());
    }
}