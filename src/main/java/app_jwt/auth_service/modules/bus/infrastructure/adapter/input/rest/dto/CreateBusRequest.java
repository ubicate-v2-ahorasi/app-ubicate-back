package app_jwt.auth_service.modules.bus.infrastructure.adapter.input.rest.dto;

import app_jwt.auth_service.modules.bus.domain.model.EstadoBus;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateBusRequest {

    @NotBlank(message = "La placa es obligatoria")
    @Size(max = 10, message = "La placa no puede exceder 10 caracteres")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "La placa solo puede contener letras mayúsculas, números y guiones")
    private String placa;

    @NotBlank(message = "La marca es obligatoria")
    @Size(max = 50, message = "La marca no puede exceder 50 caracteres")
    private String marca;

    @NotBlank(message = "El modelo es obligatorio")
    @Size(max = 50, message = "El modelo no puede exceder 50 caracteres")
    private String modelo;

    @NotNull(message = "La capacidad es obligatoria")
    @Min(value = 1, message = "La capacidad debe ser mayor a 0")
    @Max(value = 100, message = "La capacidad no puede exceder 100 pasajeros")
    private Integer capacidad;

    @NotNull(message = "El año es obligatorio")
    @Min(value = 1990, message = "El año debe ser mayor a 1990")
    @Max(value = 2030, message = "El año no puede ser mayor a 2030")
    private Integer anio;

    @Size(max = 50, message = "El color no puede exceder 50 caracteres")
    private String color;

    private EstadoBus estado;

    private Long rutaId;
}
