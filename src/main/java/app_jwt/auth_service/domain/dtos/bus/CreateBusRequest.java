package app_jwt.auth_service.domain.dtos.bus;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateBusRequest {

    @NotBlank(message = "La placa es obligatoria")
    @Size(max = 10, message = "La placa no puede exceder 10 caracteres")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "La placa solo puede contener letras mayúsculas, números y guiones")
    private String placa;

    @NotBlank(message = "El modelo es obligatorio")
    @Size(max = 50, message = "El modelo no puede exceder 50 caracteres")
    private String modelo;

    @NotNull(message = "La capacidad es obligatoria")
    @Min(value = 1, message = "La capacidad debe ser mayor a 0")
    @Max(value = 100, message = "La capacidad no puede exceder 100 pasajeros")
    private Integer capacidad;

    @Pattern(regexp = "^(19|20)\\d{2}$", message = "El año debe estar entre 1900 y 2099")
    private String anio;

    @Size(max = 50, message = "El color no puede exceder 50 caracteres")
    private String color;

    // 🚌 RUTA ASIGNADA (OPCIONAL AL CREAR)
    private Long rutaId;
}