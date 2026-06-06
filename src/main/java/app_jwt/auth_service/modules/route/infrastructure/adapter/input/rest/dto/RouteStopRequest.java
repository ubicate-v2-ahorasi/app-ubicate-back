package app_jwt.auth_service.modules.route.infrastructure.adapter.input.rest.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RouteStopRequest {
    @Size(max = 120, message = "El nombre de la parada no puede exceder 120 caracteres")
    private String nombre;

    @Size(max = 255, message = "La dirección de la parada no puede exceder 255 caracteres")
    private String direccion;

    @NotNull(message = "La latitud es obligatoria")
    private Double latitud;

    @NotNull(message = "La longitud es obligatoria")
    private Double longitud;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "El color debe ser un hex válido (ej: #FF0000)")
    private String colorHex;

    private Integer orden;
}
