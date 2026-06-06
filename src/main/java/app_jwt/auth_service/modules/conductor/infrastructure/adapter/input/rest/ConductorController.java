package app_jwt.auth_service.modules.conductor.infrastructure.adapter.input.rest;

import app_jwt.auth_service.modules.conductor.domain.port.input.ConductorService;
import app_jwt.auth_service.modules.conductor.infrastructure.adapter.input.rest.dto.*;
import app_jwt.auth_service.modules.conductor.domain.model.CategoriaLicencia;
import app_jwt.auth_service.modules.conductor.domain.model.EstadoConductor;
import app_jwt.auth_service.shared.dto.ApiResponse;
import app_jwt.auth_service.shared.utils.AuthUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conductores")
@RequiredArgsConstructor
@PreAuthorize("hasRole('EMPRESA')")
public class ConductorController {

    private final ConductorService conductorService;
    private final AuthUtils authUtils;

    @PostMapping
    public ResponseEntity<ConductorCreatedResponse> createConductor(
            @Valid @RequestBody CreateConductorRequest request, Authentication authentication) {
        authUtils.validateIsEmpresa(authentication);
        Long empresaId = authUtils.getEmpresaId(authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(conductorService.createConductor(request, empresaId));
    }

    @GetMapping
    public ResponseEntity<Page<ConductorResponse>> getConductores(
            Authentication authentication,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) EstadoConductor estado,
            @RequestParam(required = false) CategoriaLicencia categoria,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long empresaId = authUtils.getEmpresaId(authentication);
        PageRequest pageable = PageRequest.of(Math.max(0, page - 1), size);
        return ResponseEntity.ok(conductorService.getConductores(empresaId, search, estado, categoria, pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ConductorResponse>> searchConductores(
            @RequestParam(required = false) String q,
            Authentication authentication,
            @PageableDefault(size = 20) Pageable pageable) {
        authUtils.validateIsEmpresa(authentication);
        Long empresaId = authUtils.getEmpresaId(authentication);
        return ResponseEntity.ok(conductorService.searchConductores(empresaId, q, pageable));
    }

    @GetMapping("/{conductorId}")
    public ResponseEntity<ConductorResponse> getConductorById(
            @PathVariable Long conductorId, Authentication authentication) {
        authUtils.validateIsEmpresa(authentication);
        Long empresaId = authUtils.getEmpresaId(authentication);
        return ResponseEntity.ok(conductorService.getConductorById(conductorId, empresaId));
    }

    @PutMapping("/{conductorId}")
    public ResponseEntity<ConductorResponse> updateConductor(
            @PathVariable Long conductorId,
            @Valid @RequestBody UpdateConductorRequest request,
            Authentication authentication) {
        authUtils.validateIsEmpresa(authentication);
        Long empresaId = authUtils.getEmpresaId(authentication);
        return ResponseEntity.ok(conductorService.updateConductor(conductorId, request, empresaId));
    }

    @PatchMapping("/{conductorId}/password")
    public ResponseEntity<ApiResponse> changePassword(
            @PathVariable Long conductorId,
            @Valid @RequestBody ChangeConductorPasswordRequest request,
            Authentication authentication) {
        authUtils.validateIsEmpresa(authentication);
        Long empresaId = authUtils.getEmpresaId(authentication);
        conductorService.changePassword(conductorId, request.getNewPassword(), empresaId);
        return ResponseEntity.ok(new ApiResponse("Contraseña actualizada exitosamente", true));
    }

    @DeleteMapping("/{conductorId}")
    public ResponseEntity<ApiResponse> deleteConductor(
            @PathVariable Long conductorId, Authentication authentication) {
        authUtils.validateIsEmpresa(authentication);
        Long empresaId = authUtils.getEmpresaId(authentication);
        conductorService.deleteConductor(conductorId, empresaId);
        return ResponseEntity.ok(new ApiResponse("Conductor eliminado exitosamente", true));
    }

    @PatchMapping("/{conductorId}/estado")
    public ResponseEntity<ConductorResponse> cambiarEstado(
            @PathVariable Long conductorId,
            @RequestParam EstadoConductor estado,
            Authentication authentication) {
        authUtils.validateIsEmpresa(authentication);
        Long empresaId = authUtils.getEmpresaId(authentication);
        return ResponseEntity.ok(conductorService.cambiarEstado(conductorId, estado, empresaId));
    }

    @PatchMapping("/{conductorId}/asignar-bus")
    public ResponseEntity<ConductorResponse> asignarBus(
            @PathVariable Long conductorId,
            @RequestParam Long busId,
            Authentication authentication) {
        authUtils.validateIsEmpresa(authentication);
        Long empresaId = authUtils.getEmpresaId(authentication);
        return ResponseEntity.ok(conductorService.asignarBus(conductorId, busId, empresaId));
    }

    @PatchMapping("/{conductorId}/remover-bus")
    public ResponseEntity<ConductorResponse> removerBus(
            @PathVariable Long conductorId, Authentication authentication) {
        authUtils.validateIsEmpresa(authentication);
        Long empresaId = authUtils.getEmpresaId(authentication);
        return ResponseEntity.ok(conductorService.removerBus(conductorId, empresaId));
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<ConductorResponse>> getConductoresByEstado(
            @PathVariable EstadoConductor estado, Authentication authentication) {
        authUtils.validateIsEmpresa(authentication);
        Long empresaId = authUtils.getEmpresaId(authentication);
        return ResponseEntity.ok(conductorService.getConductoresByEstado(empresaId, estado));
    }

    @GetMapping("/stats")
    public ResponseEntity<ConductorStatsResponse> getStats(Authentication authentication) {
        authUtils.validateIsEmpresa(authentication);
        Long empresaId = authUtils.getEmpresaId(authentication);
        return ResponseEntity.ok(conductorService.getStats(empresaId));
    }
}
