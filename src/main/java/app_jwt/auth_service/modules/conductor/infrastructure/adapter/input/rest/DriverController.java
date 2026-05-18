package app_jwt.auth_service.modules.conductor.infrastructure.adapter.input.rest;

import app_jwt.auth_service.modules.conductor.domain.port.input.ConductorService;
import app_jwt.auth_service.modules.conductor.infrastructure.adapter.input.rest.dto.DriverAssignmentResponse;
import app_jwt.auth_service.shared.utils.AuthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/driver")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CHOFER')")
public class DriverController {

    private final ConductorService conductorService;
    private final AuthUtils authUtils;

    @GetMapping("/me/asignacion")
    public ResponseEntity<DriverAssignmentResponse> getMyAssignment(Authentication authentication) {
        Long usuarioId = authUtils.getUserId(authentication);
        return ResponseEntity.ok(conductorService.getMyAssignment(usuarioId));
    }
}
