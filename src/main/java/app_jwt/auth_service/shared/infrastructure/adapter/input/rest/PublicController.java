package app_jwt.auth_service.shared.infrastructure.adapter.input.rest;

import app_jwt.auth_service.modules.bus.infrastructure.adapter.input.rest.dto.BusLocationResponse;
import app_jwt.auth_service.modules.route.infrastructure.adapter.input.rest.dto.BusPositionDTO;
import app_jwt.auth_service.shared.infrastructure.adapter.input.rest.dto.EmpresaPublicResponse;
import app_jwt.auth_service.modules.route.infrastructure.adapter.input.rest.dto.RouteResponse;
import app_jwt.auth_service.shared.application.service.PublicService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {

    private final PublicService publicService;

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

    @GetMapping("/rutas/{rutaId}/buses-posicion")
    public ResponseEntity<List<BusPositionDTO>> getBusesPosicionByRuta(@PathVariable Long rutaId) {
        return ResponseEntity.ok(publicService.getBusesPosicionByRuta(rutaId));
    }

    @GetMapping("/buses/{busId}")
    public ResponseEntity<BusLocationResponse> getBusUbicacion(@PathVariable Long busId) {
        return ResponseEntity.ok(publicService.getBusUbicacion(busId));
    }
}
