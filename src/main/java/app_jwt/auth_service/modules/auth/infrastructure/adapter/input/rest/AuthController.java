package app_jwt.auth_service.modules.auth.infrastructure.adapter.input.rest;

import app_jwt.auth_service.modules.auth.application.dto.AuthResponse;
import app_jwt.auth_service.modules.auth.application.dto.LoginRequest;
import app_jwt.auth_service.modules.auth.application.dto.RegisterRequest;
import app_jwt.auth_service.modules.auth.application.port.input.AuthUseCase;
import app_jwt.auth_service.shared.enums.Role;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthUseCase authService;

    @PostMapping("/register/empresa")
    public ResponseEntity<AuthResponse> registerEmpresa(@Valid @RequestBody RegisterRequest request) {
        log.info("Intento de registro de empresa para email: {}", request.getEmail());
        AuthResponse response = authService.registerEmpresa(request);
        log.info("Empresa registrada exitosamente: {}", request.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/register/chofer")
    public ResponseEntity<AuthResponse> registerChofer(@Valid @RequestBody RegisterRequest request) {
        log.info("Intento de registro de chofer para email: {}", request.getEmail());
        AuthResponse response = authService.registerChofer(request);
        log.info("Chofer registrado exitosamente: {}", request.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Intento de login para email: {}", request.getEmail());
        AuthResponse response = authService.login(request);
        log.info("Rol del usuario: {}", response.getUser().getRole());

        log.info("Login exitoso para email: {}", request.getEmail());
        return ResponseEntity.ok(response);
    }
}
