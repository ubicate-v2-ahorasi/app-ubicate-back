package app_jwt.auth_service.modules.route.infrastructure.adapter.input.rest.dto;

import app_jwt.auth_service.modules.bus.domain.model.Bus;
import app_jwt.auth_service.modules.route.domain.model.EstadoRuta;
import app_jwt.auth_service.modules.route.domain.model.Route;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class RouteResponse {
    private Long id;
    private String nombre;
    private String descripcion;
    private String codigo;
    private String origen;
    private String destino;
    private String colorHex;
    private String polyline;
    private EstadoRuta estado;
    private Boolean activo;
    private Long empresaId;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    private List<BusBasicInfo> buses;
    private Integer totalBuses;
    private Long totalParadas;

    @Data
    @Builder
    public static class BusBasicInfo {
        private Long id;
        private String placa;
        private String modelo;
        private String estado;
    }

    public static RouteResponse from(Route route) {
        return from(route, null);
    }

    public static RouteResponse from(Route route, Long totalParadas) {
        List<BusBasicInfo> busesInfo = route.getBuses() != null ?
                route.getBuses().stream()
                        .filter(bus -> Boolean.TRUE.equals(bus.getActivo()))
                        .map(bus -> BusBasicInfo.builder()
                                .id(bus.getId())
                                .placa(bus.getPlaca())
                                .modelo(bus.getModelo())
                                .estado(bus.getEstado().name())
                                .build())
                        .collect(Collectors.toList()) : List.of();

        return RouteResponse.builder()
                .id(route.getId())
                .nombre(route.getNombre())
                .descripcion(route.getDescripcion())
                .codigo(route.getCodigo())
                .origen(route.getOrigen())
                .destino(route.getDestino())
                .colorHex(route.getColorHex())
                .polyline(route.getPolyline())
                .estado(route.getEstado())
                .activo(route.getActivo())
                .empresaId(route.getEmpresaId())
                .fechaCreacion(route.getFechaCreacion())
                .fechaActualizacion(route.getFechaActualizacion())
                .buses(busesInfo)
                .totalBuses(busesInfo.size())
                .totalParadas(totalParadas)
                .build();
    }
}
