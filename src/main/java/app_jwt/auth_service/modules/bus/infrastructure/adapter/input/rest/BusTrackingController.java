package app_jwt.auth_service.modules.bus.infrastructure.adapter.input.rest;

import app_jwt.auth_service.modules.bus.domain.port.input.BusTrackingService;
import app_jwt.auth_service.modules.bus.infrastructure.adapter.input.rest.dto.WaitTimeRequest;
import app_jwt.auth_service.modules.bus.infrastructure.adapter.input.rest.dto.WaitTimeResponse;
import app_jwt.auth_service.shared.dto.ApiResponse;
import app_jwt.auth_service.shared.utils.AuthUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bus-tracking")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('EMPRESA', 'CHOFER')")
public class BusTrackingController {

    private final BusTrackingService busTrackingService;
    private final AuthUtils authUtils;

    @PostMapping("/tiempo-espera")
    public ResponseEntity<WaitTimeResponse> calcularTiempoEspera(
            @Valid @RequestBody WaitTimeRequest request,
            Authentication authentication) {

        Long empresaId = authUtils.getEmpresaId(authentication);
        WaitTimeResponse response = busTrackingService.calcularTiempoEspera(request, empresaId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/bus/{placa}/ubicacion")
    public ResponseEntity<ApiResponse> actualizarUbicacion(
            @PathVariable String placa,
            @RequestParam Double latitud,
            @RequestParam Double longitud,
            Authentication authentication) {

        Long empresaId = authUtils.getEmpresaId(authentication);
        busTrackingService.actualizarUbicacionBus(placa, latitud, longitud, empresaId);

        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Ubicación del bus actualizada correctamente")
                .build());
    }
}
