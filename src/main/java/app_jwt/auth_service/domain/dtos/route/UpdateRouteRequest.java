package app_jwt.auth_service.domain.dtos.route;

import app_jwt.auth_service.domain.enums.EstadoRuta;
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
