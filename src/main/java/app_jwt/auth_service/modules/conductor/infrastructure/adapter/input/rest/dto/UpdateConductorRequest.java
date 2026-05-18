package app_jwt.auth_service.modules.conductor.infrastructure.adapter.input.rest.dto;

import app_jwt.auth_service.modules.conductor.domain.model.EstadoConductor;
import app_jwt.auth_service.modules.conductor.domain.model.TurnoConductor;
import jakarta.validation.constraints.Future;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateConductorRequest {
    private String telefono;

    @Future(message = "La fecha debe ser futura")
    private LocalDate fechaVencimientoLicencia;

    private TurnoConductor turno;
    private EstadoConductor estado;
    private Long busAsignadoId;
}
