package app_jwt.auth_service.modules.conductor.infrastructure.adapter.input.rest.dto;

import app_jwt.auth_service.modules.conductor.domain.model.CategoriaLicencia;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RenewLicenseRequest {
    @NotNull(message = "La nueva fecha de vencimiento es obligatoria")
    @Future(message = "La fecha de vencimiento debe ser futura")
    private LocalDate fechaVencimientoLicencia;

    @Size(max = 20, message = "El número de licencia no puede exceder 20 caracteres")
    private String numeroLicencia;

    private CategoriaLicencia categoriaLicencia;
}
