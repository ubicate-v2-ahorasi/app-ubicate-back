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
    public ResponseEntity<BusResponse> createBus(@Valid @RequestBody CreateBusRequest request,
                                                 Authentication authentication) {
        Long empresaId = authUtils.getEmpresaId(authentication);
        BusResponse response = busService.createBus(request, empresaId);
        try { firebaseTrackingService.upsertBus(busService.getBusEntity(response.getId(), empresaId)); }
        catch (Exception e) { log.error("Error al registrar bus {} en Firebase: {}", response.getId(), e.getMessage()); }
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
    public ResponseEntity<BusResponse> updateBus(@PathVariable Long busId,
                                                 @Valid @RequestBody UpdateBusRequest request,
                                                 Authentication authentication) {
        Long empresaId = authUtils.getEmpresaId(authentication);
        BusResponse response = busService.updateBus(busId, request, empresaId);
        try { firebaseTrackingService.upsertBus(busService.getBusEntity(busId, empresaId)); }
        catch (Exception e) { log.error("Error al actualizar bus {} en Firebase: {}", busId, e.getMessage()); }
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{busId}/asignar-ruta")
    public ResponseEntity<BusResponse> asignarRuta(@PathVariable Long busId,
                                                   @RequestParam Long rutaId,
                                                   Authentication authentication) {
        Long empresaId = authUtils.getEmpresaId(authentication);
        BusResponse response = busService.asignarRuta(busId, rutaId, empresaId);

        try {
            var bus = busService.getBusEntity(busId, empresaId);
            firebaseTrackingService.updateBusRuta(empresaId, busId, bus.getRutaAsignada().getId());
            firebaseTrackingService.upsertBus(bus);
        } catch (Exception e) {
            log.error("Error al actualizar ruta del bus {} en Firebase: {}", busId, e.getMessage());
        }

        return ResponseEntity.ok(response);
    }


    @PatchMapping("/{busId}/remover-ruta")
    public ResponseEntity<BusResponse> removerRuta(@PathVariable Long busId,
                                                   Authentication authentication) {
        Long empresaId = authUtils.getEmpresaId(authentication);
        BusResponse response = busService.removerRuta(busId, empresaId);
        try {
            firebaseTrackingService.updateBusRuta(empresaId, busId, null);
            firebaseTrackingService.upsertBus(busService.getBusEntity(busId, empresaId));
        } catch (Exception e) { log.error("Error al remover ruta del bus {} en Firebase: {}", busId, e.getMessage()); }
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
    public ResponseEntity<BusResponse> changeEstadoBus(@PathVariable Long busId,
                                                       @RequestParam EstadoBus estado,
                                                       Authentication authentication) {
        Long empresaId = authUtils.getEmpresaId(authentication);
        BusResponse response = busService.changeEstadoBus(busId, estado, empresaId);
        try {
            boolean activo = estado != EstadoBus.INACTIVO && estado != EstadoBus.MANTENIMIENTO;
            firebaseTrackingService.updateBusEstado(empresaId, busId, estado.name(), activo);
            if (activo) firebaseTrackingService.upsertBus(busService.getBusEntity(busId, empresaId));
        } catch (Exception e) { log.error("Error reflejando estado en Firebase para bus {}: {}", busId, e.getMessage()); }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/buses/ruta/{rutaId}")
    public ResponseEntity<List<BusResponse>> getBusesByRuta(@PathVariable Long rutaId,
                                                            Authentication authentication) {
        Long empresaId = authUtils.getEmpresaId(authentication);
        // Usa Pageable.unpaged() para obtener todos los resultados
        List<BusResponse> buses = busService.getBusesByRuta(rutaId, empresaId, Pageable.unpaged())
                .getContent();
        return ResponseEntity.ok(buses);
    }


    @GetMapping("/stats")
    public ResponseEntity<BusStatsResponse> getBusStats(Authentication authentication) {
        Long empresaId = authUtils.getEmpresaId(authentication);
        BusStatsResponse stats = busService.getBusStats(empresaId);
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/test-firebase")
    public ResponseEntity<ApiResponse> testFirebase(Authentication authentication) {
        try {
            Long empresaId = authUtils.getEmpresaId(authentication);
            Page<BusResponse> buses = busService.getBusesByEmpresa(empresaId,
                    org.springframework.data.domain.PageRequest.of(0, 1));
            if (buses.hasContent()) {
                BusResponse firstBus = buses.getContent().get(0);
                firebaseTrackingService.upsertBus(busService.getBusEntity(firstBus.getId(), empresaId));
                return ResponseEntity.ok(new ApiResponse("Test de Firebase exitoso con bus " + firstBus.getPlaca(), true));
            } else {
                return ResponseEntity.ok(new ApiResponse("No hay buses para probar Firebase. Crea un bus primero.", false));
            }
        } catch (Exception e) {
            log.error("Error en test de Firebase: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(new ApiResponse("Error en Firebase: " + e.getMessage(), false));
        }
    }

    @PostMapping("/sync-firebase")
    public ResponseEntity<ApiResponse> syncAllBusesToFirebase(Authentication authentication) {
        try {
            Long empresaId = authUtils.getEmpresaId(authentication);
            List<BusResponse> allBuses = busService.getBusesByEmpresa(empresaId,
                    org.springframework.data.domain.Pageable.unpaged()).getContent();
            int syncedCount = 0, errorCount = 0;
            for (BusResponse b : allBuses) {
                try {
                    firebaseTrackingService.upsertBus(busService.getBusEntity(b.getId(), empresaId));
                    syncedCount++;
                } catch (Exception e) {
                    errorCount++;
                    log.error("Error sincronizando bus {} con Firebase: {}", b.getPlaca(), e.getMessage());
                }
            }
            return ResponseEntity.ok(new ApiResponse(String.format(
                    "Sincronización completada. %d buses sincronizados, %d errores", syncedCount, errorCount), true));
        } catch (Exception e) {
            log.error("Error en sincronización masiva con Firebase: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(new ApiResponse("Error en sincronización masiva: " + e.getMessage(), false));
        }
    }
}