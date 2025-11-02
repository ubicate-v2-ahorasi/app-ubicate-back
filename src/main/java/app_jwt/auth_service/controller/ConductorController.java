package app_jwt.auth_service.controller;

import app_jwt.auth_service.domain.dtos.bus.ApiResponse;
import app_jwt.auth_service.domain.dtos.conductor.*;
import app_jwt.auth_service.domain.enums.EstadoConductor;
import app_jwt.auth_service.domain.service.ConductorService;
import app_jwt.auth_service.infra.security.AuthUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
            @Valid @RequestBody CreateConductorRequest request,
            Authentication authentication) {

        authUtils.validateIsEmpresa(authentication);
        Long empresaId = authUtils.getEmpresaId(authentication);

        ConductorCreatedResponse response = conductorService.createConductor(request, empresaId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<ConductorResponse>> getConductores(
            Authentication authentication,
            @PageableDefault(size = 20) Pageable pageable) {

        authUtils.validateIsEmpresa(authentication);
        Long empresaId = authUtils.getEmpresaId(authentication);

        Page<ConductorResponse> conductores = conductorService.getConductores(empresaId, pageable);
        return ResponseEntity.ok(conductores);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ConductorResponse>> searchConductores(
            @RequestParam(required = false) String q,
            Authentication authentication,
            @PageableDefault(size = 20) Pageable pageable) {

        authUtils.validateIsEmpresa(authentication);
        Long empresaId = authUtils.getEmpresaId(authentication);

        Page<ConductorResponse> conductores = conductorService.searchConductores(empresaId, q, pageable);
        return ResponseEntity.ok(conductores);
    }

    @GetMapping("/{conductorId}")
    public ResponseEntity<ConductorResponse> getConductorById(
            @PathVariable Long conductorId,
            Authentication authentication) {

        authUtils.validateIsEmpresa(authentication);
        Long empresaId = authUtils.getEmpresaId(authentication);

        ConductorResponse conductor = conductorService.getConductorById(conductorId, empresaId);
        return ResponseEntity.ok(conductor);
    }

    @PutMapping("/{conductorId}")
    public ResponseEntity<ConductorResponse> updateConductor(
            @PathVariable Long conductorId,
            @Valid @RequestBody UpdateConductorRequest request,
            Authentication authentication) {

        authUtils.validateIsEmpresa(authentication);
        Long empresaId = authUtils.getEmpresaId(authentication);

        ConductorResponse response = conductorService.updateConductor(conductorId, request, empresaId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{conductorId}")
    public ResponseEntity<ApiResponse> deleteConductor(
            @PathVariable Long conductorId,
            Authentication authentication) {

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

        ConductorResponse response = conductorService.cambiarEstado(conductorId, estado, empresaId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{conductorId}/asignar-bus")
    public ResponseEntity<ConductorResponse> asignarBus(
            @PathVariable Long conductorId,
            @RequestParam Long busId,
            Authentication authentication) {

        authUtils.validateIsEmpresa(authentication);
        Long empresaId = authUtils.getEmpresaId(authentication);

        ConductorResponse response = conductorService.asignarBus(conductorId, busId, empresaId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{conductorId}/remover-bus")
    public ResponseEntity<ConductorResponse> removerBus(
            @PathVariable Long conductorId,
            Authentication authentication) {

        authUtils.validateIsEmpresa(authentication);
        Long empresaId = authUtils.getEmpresaId(authentication);

        ConductorResponse response = conductorService.removerBus(conductorId, empresaId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<ConductorResponse>> getConductoresByEstado(
            @PathVariable EstadoConductor estado,
            Authentication authentication) {

        authUtils.validateIsEmpresa(authentication);
        Long empresaId = authUtils.getEmpresaId(authentication);

        List<ConductorResponse> conductores = conductorService.getConductoresByEstado(empresaId, estado);
        return ResponseEntity.ok(conductores);
    }

    @GetMapping("/stats")
    public ResponseEntity<ConductorStatsResponse> getStats(Authentication authentication) {
        authUtils.validateIsEmpresa(authentication);
        Long empresaId = authUtils.getEmpresaId(authentication);

        ConductorStatsResponse stats = conductorService.getStats(empresaId);
        return ResponseEntity.ok(stats);
    }
}