package app_jwt.auth_service.modules.auth.application.port.input;

import app_jwt.auth_service.modules.auth.application.dto.AuthResponse;
import app_jwt.auth_service.modules.auth.application.dto.LoginRequest;
import app_jwt.auth_service.modules.auth.application.dto.RegisterRequest;
import app_jwt.auth_service.shared.domain.model.Usuario;

public interface AuthUseCase {
    AuthResponse registerEmpresa(RegisterRequest request);
    AuthResponse registerChofer(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    Usuario getUsuarioByEmail(String email);
    Usuario getUsuarioByUsername(String username);
}
