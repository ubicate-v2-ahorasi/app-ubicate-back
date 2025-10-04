package app_jwt.auth_service.domain.dtos.bus;

import app_jwt.auth_service.domain.enums.EstadoBus;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateBusRequest {

    @Size(max = 50, message = "El modelo no puede exceder 50 caracteres")
    private String modelo;

    @Min(value = 1, message = "La capacidad debe ser mayor a 0")
    @Max(value = 100, message = "La capacidad no puede exceder 100 pasajeros")
    private Integer capacidad;

    @Pattern(regexp = "^(19|20)\\d{2}$", message = "El año debe estar entre 1900 y 2099")
    private String anio;

    @Size(max = 50, message = "El color no puede exceder 50 caracteres")
    private String color;

    private EstadoBus estado;

    private Long rutaId;
}