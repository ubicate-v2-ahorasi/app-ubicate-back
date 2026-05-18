package app_jwt.auth_service.modules.conductor.infrastructure.adapter.input.rest.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConductorStatsResponse {
    private Long totalConductores;
    private Long conductoresActivos;
    private Long conductoresInactivos;
    private Long conductoresVacaciones;
    private Long conductoresSuspendidos;
    private Long conductoresConBus;
    private Long conductoresSinBus;
    private Long licenciasVencidas;
    private Long licenciasPorVencer;
}
