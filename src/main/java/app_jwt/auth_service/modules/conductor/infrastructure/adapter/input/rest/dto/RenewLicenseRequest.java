package app_jwt.auth_service.modules.conductor.infrastructure.adapter.input.rest.dto;

import app_jwt.auth_service.modules.conductor.domain.model.CategoriaLicencia;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RenewLicenseRequest {
    @NotNull(message = "La nueva fecha de vencimiento es obligatoria")
    @Future(message = "La fecha de vencimiento debe ser futura")
    @JsonAlias({"fecha_vencimiento_licencia", "fechaVencimientoLicencia"})
    private LocalDate fechaVencimientoLicencia;

    @Size(max = 20, message = "El número de licencia no puede exceder 20 caracteres")
    @JsonAlias({"numero_licencia", "numeroLicencia"})
    private String numeroLicencia;

    @JsonAlias({"categoria_licencia", "categoriaLicencia"})
    private CategoriaLicencia categoriaLicencia;
}
