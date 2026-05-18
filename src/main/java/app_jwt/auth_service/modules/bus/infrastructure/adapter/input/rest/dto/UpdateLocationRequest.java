package app_jwt.auth_service.modules.bus.infrastructure.adapter.input.rest.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateLocationRequest {

    @NotNull(message = "Latitud es obligatoria")
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    private Double latitud;

    @NotNull(message = "Longitud es obligatoria")
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    private Double longitud;

    @DecimalMin(value = "0.0")
    private Double velocidad;
}
