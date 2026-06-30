package app_jwt.auth_service.modules.auth.domain.port;

import app_jwt.auth_service.shared.domain.model.Usuario;

public interface AuthSecurityPort {
    String encodePassword(String rawPassword);
    void authenticate(String username, String password);
    String generateToken(Usuario usuario);
    String generateRefreshToken(Usuario usuario);

    /** Valida el refresh token y devuelve el userId; null si es invalido/expirado. */
    Long validateRefreshTokenAndGetUserId(String refreshToken);
}
