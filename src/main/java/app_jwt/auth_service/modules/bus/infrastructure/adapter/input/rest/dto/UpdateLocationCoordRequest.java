package app_jwt.auth_service.modules.bus.infrastructure.adapter.input.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateLocationCoordRequest {

    @NotBlank(message = "coord es obligatoria")
    @Pattern(
            regexp = "^\\s*-?\\d+(\\.\\d+)?\\s*,\\s*-?\\d+(\\.\\d+)?\\s*$",
            message = "coord debe tener formato 'lat,lng'"
    )
    private String coord;

    private Double velocidad;
}
