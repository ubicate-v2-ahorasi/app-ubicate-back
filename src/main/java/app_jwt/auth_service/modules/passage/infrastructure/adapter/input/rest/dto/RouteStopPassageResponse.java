package app_jwt.auth_service.modules.passage.infrastructure.adapter.input.rest.dto;

import app_jwt.auth_service.modules.passage.domain.model.RouteStopPassage;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RouteStopPassageResponse {

    private Long id;
    private Long routeStopId;
    private Long busId;
    private String placa;
    private Integer orden;
    private String nombreParada;
    private LocalDateTime horaCruce;
    private Integer segundosDesdeAnterior;

    public static RouteStopPassageResponse from(RouteStopPassage p) {
        return RouteStopPassageResponse.builder()
                .id(p.getId())
                .routeStopId(p.getRouteStopId())
                .busId(p.getBusId())
                .placa(p.getPlaca())
                .orden(p.getOrden())
                .nombreParada(p.getNombreParada())
                .horaCruce(p.getHoraCruce())
                .segundosDesdeAnterior(p.getSegundosDesdeAnterior())
                .build();
    }
}
