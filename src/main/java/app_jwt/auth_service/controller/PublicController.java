package app_jwt.auth_service.controller;

import app_jwt.auth_service.domain.dtos.bus.BusLocationResponse;
import app_jwt.auth_service.domain.dtos.empresa.EmpresaPublicResponse;
import app_jwt.auth_service.domain.dtos.route.RouteResponse;
import app_jwt.auth_service.domain.service.PublicService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {

    private final PublicService publicService;

    // Obtener empresas (Acceso público, no necesita rol)
    @GetMapping("/empresas")
    public ResponseEntity<List<EmpresaPublicResponse>> getEmpresas() {
        return ResponseEntity.ok(publicService.getAllEmpresas());
    }
    @GetMapping("/empresas/{empresaId}/rutas")
    public ResponseEntity<List<RouteResponse>> getRutasByEmpresa(
            @PathVariable Long empresaId) {
        return ResponseEntity.ok(publicService.getRutasByEmpresa(empresaId));
    }

    @GetMapping("/empresas/{empresaId}/buses")
    public ResponseEntity<List<BusLocationResponse>> getBusesActivos(
            @PathVariable Long empresaId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime since) {

        if (since != null) {
            return ResponseEntity.ok(publicService.getBusesUpdatedSince(empresaId, since));
        }
        return ResponseEntity.ok(publicService.getBusesActivosByEmpresa(empresaId));
    }

    @GetMapping("/rutas/{rutaId}")
    public ResponseEntity<RouteResponse> getRutaById(@PathVariable Long rutaId) {
        return ResponseEntity.ok(publicService.getRutaById(rutaId));
    }

    @GetMapping("/buses/{busId}")
    public ResponseEntity<BusLocationResponse> getBusUbicacion(@PathVariable Long busId) {
        return ResponseEntity.ok(publicService.getBusUbicacion(busId));
    }
}
