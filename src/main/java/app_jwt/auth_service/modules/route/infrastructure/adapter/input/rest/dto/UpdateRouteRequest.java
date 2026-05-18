package app_jwt.auth_service.modules.route.infrastructure.adapter.input.rest.dto;

import app_jwt.auth_service.modules.route.domain.model.EstadoRuta;
import lombok.Data;

import java.util.List;

@Data
public class UpdateRouteRequest {
    private String nombre;
    private String descripcion;
    private String origen;
    private String destino;
    private String colorHex;
    private String polyline;
    private EstadoRuta estado;
    private List<Long> busIds;
}
