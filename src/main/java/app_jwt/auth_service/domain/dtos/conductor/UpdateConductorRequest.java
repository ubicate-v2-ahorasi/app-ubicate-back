package app_jwt.auth_service.domain.dtos.conductor;

import app_jwt.auth_service.domain.enums.EstadoConductor;
import app_jwt.auth_service.domain.enums.TurnoConductor;
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