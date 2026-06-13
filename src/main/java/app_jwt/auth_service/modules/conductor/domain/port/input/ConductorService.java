package app_jwt.auth_service.modules.conductor.domain.port.input;

import app_jwt.auth_service.modules.conductor.infrastructure.adapter.input.rest.dto.*;
import app_jwt.auth_service.modules.conductor.domain.model.CategoriaLicencia;
import app_jwt.auth_service.modules.conductor.domain.model.EstadoConductor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ConductorService {
    ConductorCreatedResponse createConductor(CreateConductorRequest request, Long empresaId);
    Page<ConductorResponse> getConductores(Long empresaId, String search, EstadoConductor estado, CategoriaLicencia categoria, Pageable pageable);
    Page<ConductorResponse> searchConductores(Long empresaId, String searchTerm, Pageable pageable);
    ConductorResponse getConductorById(Long conductorId, Long empresaId);
    ConductorResponse updateConductor(Long conductorId, UpdateConductorRequest request, Long empresaId);
    ConductorResponse renewLicense(Long conductorId, RenewLicenseRequest request, Long empresaId);
    void changePassword(Long conductorId, String newPassword, Long empresaId);
    void deleteConductor(Long conductorId, Long empresaId);
    ConductorResponse cambiarEstado(Long conductorId, EstadoConductor estado, Long empresaId);
    ConductorResponse asignarBus(Long conductorId, Long busId, Long empresaId);
    ConductorResponse removerBus(Long conductorId, Long empresaId);
    List<ConductorResponse> getConductoresByEstado(Long empresaId, EstadoConductor estado);
    ConductorStatsResponse getStats(Long empresaId);
    DriverAssignmentResponse getMyAssignment(Long usuarioId);
}
