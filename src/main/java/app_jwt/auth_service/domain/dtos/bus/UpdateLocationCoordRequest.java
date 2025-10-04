package app_jwt.auth_service.domain.dtos.bus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateLocationCoordRequest {

    // Acepta "-8.10307, -79.02378"
    @NotBlank(message = "coord es obligatoria")
    @Pattern(
            regexp = "^\\s*-?\\d+(\\.\\d+)?\\s*,\\s*-?\\d+(\\.\\d+)?\\s*$",
            message = "coord debe tener formato 'lat,lng'"
    )
    private String coord;

    // opcional
    private Double velocidad;
}
