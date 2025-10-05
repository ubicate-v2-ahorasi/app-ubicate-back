// src/main/java/app_jwt/auth_service/controller/BusTrackingController.java
package app_jwt.auth_service.controller;

import app_jwt.auth_service.domain.dtos.bus.ApiResponse;
import app_jwt.auth_service.domain.dtos.bus.WaitTimeRequest;
import app_jwt.auth_service.domain.dtos.bus.WaitTimeResponse;
import app_jwt.auth_service.domain.service.BusTrackingService;
import app_jwt.auth_service.infra.security.AuthUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bus-tracking")
@RequiredArgsConstructor
@PreAuthorize("hasRole('EMPRESA')")
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