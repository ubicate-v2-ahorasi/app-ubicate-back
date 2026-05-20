package app_jwt.auth_service.modules.auth.domain.port;

import app_jwt.auth_service.shared.domain.model.Usuario;

public interface AuthSecurityPort {
    String encodePassword(String rawPassword);
    void authenticate(String username, String password);
    String generateToken(Usuario usuario);
}
