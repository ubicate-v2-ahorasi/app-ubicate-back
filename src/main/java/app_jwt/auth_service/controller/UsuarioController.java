package app_jwt.auth_service.controller;

import app_jwt.auth_service.domain.dtos.auth.AuthResponse;
import app_jwt.auth_service.domain.dtos.auth.LoginRequest;
import app_jwt.auth_service.domain.dtos.auth.RegisterRequest;
import app_jwt.auth_service.domain.enums.Role;
import app_jwt.auth_service.domain.service.AuthService;
import app_jwt.auth_service.domain.service.FirebaseTrackingService;
import com.google.firebase.auth.FirebaseAuthException;
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
public class UsuarioController {

    private final AuthService authService;
    private final FirebaseTrackingService firebaseTrackingService;

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

        // Comparar el enum directamente
        log.info("Rol del usuario: {}", response.getUser().getRole());

        if (response.getUser().getRole() == Role.CHOFER) {  // Comparación directa de enums
            try {
                log.info("Generando token Firebase para chofer: {}", request.getEmail());
                String firebaseToken = firebaseTrackingService.generateFirebaseToken(
                        authService.getUsuarioByEmail(request.getEmail())
                );
                response.setFirebaseToken(firebaseToken);
                log.info("Token Firebase generado exitosamente");
            } catch (Exception e) {
                log.error("Error generando token Firebase: ", e);
            }
        } else {
            log.info("Usuario no es chofer, no se genera token Firebase");
        }

        log.info("Login exitoso para email: {}", request.getEmail());
        return ResponseEntity.ok(response);
    }
}