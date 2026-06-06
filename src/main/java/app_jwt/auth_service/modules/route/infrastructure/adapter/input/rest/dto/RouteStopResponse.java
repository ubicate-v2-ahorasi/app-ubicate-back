package app_jwt.auth_service.modules.route.infrastructure.adapter.input.rest.dto;

import app_jwt.auth_service.modules.route.domain.model.RouteStop;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RouteStopResponse {
    private Long id;
    private String nombre;
    private String direccion;
    private Double latitud;
    private Double longitud;
    private String colorHex;
    private Integer orden;
    private Boolean activo;

    public static RouteStopResponse from(RouteStop stop) {
        return RouteStopResponse.builder()
                .id(stop.getId())
                .nombre(stop.getNombre())
                .direccion(stop.getDireccion())
                .latitud(stop.getLatitud())
                .longitud(stop.getLongitud())
                .colorHex(stop.getColorHex())
                .orden(stop.getOrden())
                .activo(stop.getActivo())
                .build();
    }
}
