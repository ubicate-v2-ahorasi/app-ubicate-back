package app_jwt.auth_service.modules.bus.infrastructure.adapter.input.rest.dto;

import app_jwt.auth_service.modules.bus.domain.model.EstadoSenal;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BusUbicacionResponse {
    private Long busId;
    private String placa;
    private Double latitud;
    private Double longitud;
    private Double velocidad;
    private String estado;
    private EstadoSenal estadoSenal;
    private LocalDateTime ultimaUbicacion;
    private Long rutaId;
    private String nombreRuta;
}