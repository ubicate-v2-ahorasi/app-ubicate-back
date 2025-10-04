package app_jwt.auth_service.controller;

import app_jwt.auth_service.domain.dtos.bus.*;
import app_jwt.auth_service.domain.enums.EstadoBus;
import app_jwt.auth_service.domain.service.BusService;
import app_jwt.auth_service.domain.service.FirebaseTrackingService;
import app_jwt.auth_service.infra.security.AuthUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequestMapping("/api/buses")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('EMPRESA')")
public class BusController {

    private final BusService busService;
    private final FirebaseTrackingService firebaseTrackingService;
    private final AuthUtils authUtils;

    @PostMapping
    public ResponseEntity<BusResponse> createBus(
            @Valid @RequestBody CreateBusRequest request,
            Authentication authentication) {
        Long empresaId = authUtils.getEmpresaId(authentication);
        BusResponse response = busService.createBus(request, empresaId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<BusResponse>> getBuses(
            Authentication authentication,
            @RequestParam(required = false) Long rutaId,
            @PageableDefault(size = 20) Pageable pageable) {
        Long empresaId = authUtils.getEmpresaId(authentication);

        if (rutaId != null) {
            Page<BusResponse> buses = busService.getBusesByRuta(rutaId, empresaId, pageable);
            return ResponseEntity.ok(buses);
        }

        Page<BusResponse> buses = busService.getBusesByEmpresa(empresaId, pageable);
        return ResponseEntity.ok(buses);
    }

    @GetMapping("/{busId}")
    public ResponseEntity<BusResponse> getBusById(
            @PathVariable Long busId,
            Authentication authentication) {
        Long empresaId = authUtils.getEmpresaId(authentication);
        BusResponse bus = busService.getBusById(busId, empresaId);
        return ResponseEntity.ok(bus);
    }

    @PutMapping("/{busId}")
    public ResponseEntity<BusResponse> updateBus(
            @PathVariable Long busId,
            @Valid @RequestBody UpdateBusRequest request,
            Authentication authentication) {
        Long empresaId = authUtils.getEmpresaId(authentication);
        BusResponse response = busService.updateBus(busId, request, empresaId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{busId}/asignar-ruta")
    public ResponseEntity<BusResponse> asignarRuta(
            @PathVariable Long busId,
            @RequestParam Long rutaId,
            Authentication authentication) {
        Long empresaId = authUtils.getEmpresaId(authentication);
        BusResponse response = busService.asignarRuta(busId, rutaId, empresaId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{busId}/remover-ruta")
    public ResponseEntity<BusResponse> removerRuta(
            @PathVariable Long busId,
            Authentication authentication) {
        Long empresaId = authUtils.getEmpresaId(authentication);
        BusResponse response = busService.removerRuta(busId, empresaId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{busId}")
    public ResponseEntity<ApiResponse> deleteBus(
            @PathVariable Long busId,
            Authentication authentication) {
        Long empresaId = authUtils.getEmpresaId(authentication);
        busService.deleteBus(busId, empresaId);
        firebaseTrackingService.removeBusFromFirebase(empresaId, busId);
        return ResponseEntity.ok(new ApiResponse("Bus eliminado exitosamente", true));
    }

    @PatchMapping("/{busId}/estado")
    public ResponseEntity<BusResponse> changeEstadoBus(
            @PathVariable Long busId,
            @RequestParam EstadoBus estado,
            Authentication authentication) {
        Long empresaId = authUtils.getEmpresaId(authentication);
        BusResponse response = busService.changeEstadoBus(busId, estado, empresaId);

        if (estado == EstadoBus.EN_RUTA) {
            firebaseTrackingService.initializeBusLocation(
                    busService.getBusEntity(busId, empresaId)
            );
        } else if (estado == EstadoBus.INACTIVO || estado == EstadoBus.MANTENIMIENTO) {
            firebaseTrackingService.deactivateBusLocation(empresaId, busId);
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<BusResponse>> getBusesByEstado(
            @PathVariable EstadoBus estado,
            Authentication authentication) {
        Long empresaId = authUtils.getEmpresaId(authentication);
        List<BusResponse> buses = busService.getBusesByEstado(empresaId, estado);
        return ResponseEntity.ok(buses);
    }

    @GetMapping("/stats")
    public ResponseEntity<BusStatsResponse> getBusStats(Authentication authentication) {
        Long empresaId = authUtils.getEmpresaId(authentication);
        BusStatsResponse stats = busService.getBusStats(empresaId);
        return ResponseEntity.ok(stats);
    }

}