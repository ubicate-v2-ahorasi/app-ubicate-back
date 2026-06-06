package app_jwt.auth_service.modules.route.infrastructure.adapter.input.rest;

import app_jwt.auth_service.modules.route.domain.model.EstadoRuta;
import app_jwt.auth_service.modules.route.domain.port.input.RouteService;
import app_jwt.auth_service.modules.route.infrastructure.adapter.input.rest.dto.BusPositionDTO;
import app_jwt.auth_service.modules.route.infrastructure.adapter.input.rest.dto.CreateRouteRequest;
import app_jwt.auth_service.modules.route.infrastructure.adapter.input.rest.dto.RouteResponse;
import app_jwt.auth_service.modules.route.infrastructure.adapter.input.rest.dto.RouteStopRequest;
import app_jwt.auth_service.modules.route.infrastructure.adapter.input.rest.dto.RouteStopResponse;
import app_jwt.auth_service.modules.route.infrastructure.adapter.input.rest.dto.UpdateRouteRequest;
import app_jwt.auth_service.shared.dto.ApiResponse;
import app_jwt.auth_service.shared.utils.AuthUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rutas")
@RequiredArgsConstructor
@PreAuthorize("hasRole('EMPRESA')")
public class RouteController {

    private final RouteService routeService;
    private final AuthUtils authUtils;

    @PostMapping
    public ResponseEntity<RouteResponse> create(
            @Valid @RequestBody CreateRouteRequest request,
            Authentication authentication) {
        Long empresaId = authUtils.getEmpresaId(authentication);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(routeService.create(request, empresaId));
    }

    @GetMapping
    public ResponseEntity<List<RouteResponse>> listAll(
            Authentication authentication,
            @RequestParam(required = false) EstadoRuta estado) {
        Long empresaId = authUtils.getEmpresaId(authentication);
        if (estado != null) {
            return ResponseEntity.ok(routeService.listByEstado(empresaId, estado));
        }
        return ResponseEntity.ok(routeService.listAll(empresaId));
    }

    @GetMapping("/{routeId}")
    public ResponseEntity<RouteResponse> getById(
            @PathVariable Long routeId,
            Authentication authentication) {
        Long empresaId = authUtils.getEmpresaId(authentication);
        return ResponseEntity.ok(routeService.getById(routeId, empresaId));
    }

    @PutMapping("/{routeId}")
    public ResponseEntity<RouteResponse> update(
            @PathVariable Long routeId,
            @Valid @RequestBody UpdateRouteRequest request,
            Authentication authentication) {
        Long empresaId = authUtils.getEmpresaId(authentication);
        return ResponseEntity.ok(routeService.update(routeId, request, empresaId));
    }

    @DeleteMapping("/{routeId}")
    public ResponseEntity<ApiResponse> delete(
            @PathVariable Long routeId,
            Authentication authentication) {
        Long empresaId = authUtils.getEmpresaId(authentication);
        routeService.delete(routeId, empresaId);
        return ResponseEntity.ok(new ApiResponse("Ruta eliminada exitosamente", true));
    }

    @GetMapping("/{routeId}/buses-posicion")
    public ResponseEntity<List<BusPositionDTO>> getBusesPosicion(
            @PathVariable Long routeId,
            Authentication authentication) {
        Long empresaId = authUtils.getEmpresaId(authentication);
        return ResponseEntity.ok(routeService.getBusesPosicion(routeId, empresaId));
    }

    @GetMapping("/{routeId}/paradas")
    public ResponseEntity<List<RouteStopResponse>> getStops(
            @PathVariable Long routeId,
            Authentication authentication) {
        Long empresaId = authUtils.getEmpresaId(authentication);
        return ResponseEntity.ok(routeService.getStops(routeId, empresaId));
    }

    @PutMapping("/{routeId}/paradas")
    public ResponseEntity<List<RouteStopResponse>> replaceStops(
            @PathVariable Long routeId,
            @Valid @RequestBody List<@Valid RouteStopRequest> request,
            Authentication authentication) {
        Long empresaId = authUtils.getEmpresaId(authentication);
        return ResponseEntity.ok(routeService.replaceStops(routeId, request, empresaId));
    }
}
