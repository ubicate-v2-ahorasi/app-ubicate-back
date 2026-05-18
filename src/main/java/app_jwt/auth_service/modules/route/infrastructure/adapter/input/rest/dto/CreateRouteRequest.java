package app_jwt.auth_service.modules.route.infrastructure.adapter.input.rest.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class CreateRouteRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 80, message = "El nombre no puede exceder 80 caracteres")
    private String nombre;

    @Size(max = 200, message = "La descripción no puede exceder 200 caracteres")
    private String descripcion;

    @NotBlank(message = "El código es obligatorio")
    @Size(max = 20, message = "El código no puede exceder 20 caracteres")
    private String codigo;

    @Size(max = 80, message = "El origen no puede exceder 80 caracteres")
    private String origen;

    @Size(max = 80, message = "El destino no puede exceder 80 caracteres")
    private String destino;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "El color debe ser un hex válido (ej: #FF0000)")
    private String colorHex;

    private String polyline;

    private List<Long> busIds;
}
