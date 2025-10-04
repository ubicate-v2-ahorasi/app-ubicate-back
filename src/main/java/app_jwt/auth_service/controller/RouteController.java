package app_jwt.auth_service.controller;

import app_jwt.auth_service.domain.dtos.bus.ApiResponse;
import app_jwt.auth_service.domain.dtos.route.CreateRouteRequest;
import app_jwt.auth_service.domain.dtos.route.RouteResponse;
import app_jwt.auth_service.domain.dtos.route.UpdateRouteRequest;
import app_jwt.auth_service.domain.enums.EstadoRuta;
import app_jwt.auth_service.domain.service.RouteService;
import app_jwt.auth_service.infra.security.AuthUtils;
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
            @RequestParam(required = false) EstadoRuta estado) {  // Cambiado a EstadoRuta
        Long empresaId = authUtils.getEmpresaId(authentication);
        if (estado != null) {
            return ResponseEntity.ok(routeService.listByEstado(empresaId, estado));
        }
        return ResponseEntity.ok(routeService.listAll(empresaId));
    }
    // 🔹 detalle de una ruta por id (para al hacer click)
    @GetMapping("/{routeId}")
    public ResponseEntity<RouteResponse> getById(@PathVariable Long routeId,
                                                 Authentication authentication) {
        Long empresaId = authUtils.getEmpresaId(authentication);
        return ResponseEntity.ok(routeService.getById(routeId, empresaId));
    }

    @PutMapping("/{routeId}")
    public ResponseEntity<RouteResponse> update(@PathVariable Long routeId,
                                                @Valid @RequestBody UpdateRouteRequest request,
                                                Authentication authentication) {
        Long empresaId = authUtils.getEmpresaId(authentication);
        return ResponseEntity.ok(routeService.update(routeId, request, empresaId));
    }

    @DeleteMapping("/{routeId}")
    public ResponseEntity<ApiResponse> delete(@PathVariable Long routeId,
                                              Authentication authentication) {
        Long empresaId = authUtils.getEmpresaId(authentication);
        routeService.delete(routeId, empresaId);
        return ResponseEntity.ok(new ApiResponse("Ruta eliminada exitosamente", true));
    }
}
