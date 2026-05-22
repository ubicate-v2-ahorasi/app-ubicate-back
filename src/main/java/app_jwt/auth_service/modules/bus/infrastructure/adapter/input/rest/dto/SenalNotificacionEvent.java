package app_jwt.auth_service.modules.bus.infrastructure.adapter.input.rest.dto;

import app_jwt.auth_service.modules.bus.domain.model.EstadoSenal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SenalNotificacionEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long busId;
    private String placa;
    private Long empresaId;
    private EstadoSenal tipo;
    private String mensaje;
    private Double latitud;
    private Double longitud;
    private LocalDateTime timestamp;
    private LocalDateTime ultimaUbicacion;
}