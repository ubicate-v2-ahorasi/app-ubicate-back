package app_jwt.auth_service.modules.bus.infrastructure.adapter.input.rest;

import app_jwt.auth_service.modules.bus.domain.model.EstadoBus;
import app_jwt.auth_service.modules.bus.domain.port.input.BusService;
import app_jwt.auth_service.modules.bus.infrastructure.adapter.input.rest.dto.*;
import app_jwt.auth_service.shared.dto.ApiResponse;
import app_jwt.auth_service.shared.utils.AuthUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    private final AuthUtils authUtils;

    @PostMapping
    public ResponseEntity<BusResponse> createBus(@Valid @RequestBody CreateBusRequest request,
                                                 Authentication authentication) {
        Long empresaId = authUtils.getEmpresaId(authentication);
        BusResponse response = busService.createBus(request, empresaId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<BusResponse>> getBuses(
            Authentication authentication,
            @RequestParam(required = false) Long rutaId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) EstadoBus estado,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id,desc") String sort) {

        Long empresaId = authUtils.getEmpresaId(authentication);

        int zeroBasedPage = Math.max(0, page - 1);

        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        String sortDirection = sortParams.length > 1 ? sortParams[1] : "asc";

        Pageable pageable = PageRequest.of(
                zeroBasedPage,
                size,
                sortDirection.equalsIgnoreCase("desc")
                        ? org.springframework.data.domain.Sort.by(sortField).descending()
                        : org.springframework.data.domain.Sort.by(sortField).ascending()
        );

        Page<BusResponse> buses;

        if (rutaId != null) {
            buses = busService.getBusesByRuta(rutaId, empresaId, pageable);
        } else if (search != null || estado != null) {
            buses = busService.searchBuses(empresaId, search, estado, pageable);
        } else {
            buses = busService.getBusesByEmpresa(empresaId, pageable);
        }

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
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{busId}/asignar-ruta")
    public ResponseEntity<BusResponse> asignarRuta(@PathVariable Long busId,
                                                   @RequestParam Long rutaId,
                                                   Authentication authentication) {
        Long empresaId = authUtils.getEmpresaId(authentication);
        BusResponse response = busService.asignarRuta(busId, rutaId, empresaId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{busId}/remover-ruta")
    public ResponseEntity<BusResponse> removerRuta(@PathVariable Long busId,
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
        return ResponseEntity.ok(new ApiResponse("Bus eliminado exitosamente", true));
    }

    @PatchMapping("/{busId}/estado")
    public ResponseEntity<BusResponse> changeEstadoBus(@PathVariable Long busId,
                                                       @RequestParam EstadoBus estado,
                                                       Authentication authentication) {
        Long empresaId = authUtils.getEmpresaId(authentication);
        BusResponse response = busService.changeEstadoBus(busId, estado, empresaId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/buses/ruta/{rutaId}")
    public ResponseEntity<List<BusResponse>> getBusesByRuta(@PathVariable Long rutaId,
                                                            Authentication authentication) {
        Long empresaId = authUtils.getEmpresaId(authentication);
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

    @GetMapping("/ruta/{rutaId}/ubicaciones")
    public ResponseEntity<List<BusUbicacionResponse>> getUbicacionesBusesPorRuta(
            @PathVariable Long rutaId,
            Authentication authentication) {
        Long empresaId = authUtils.getEmpresaId(authentication);
        List<BusUbicacionResponse> ubicaciones = busService.getUbicacionesBusesPorRuta(rutaId, empresaId);
        return ResponseEntity.ok(ubicaciones);
    }
}
