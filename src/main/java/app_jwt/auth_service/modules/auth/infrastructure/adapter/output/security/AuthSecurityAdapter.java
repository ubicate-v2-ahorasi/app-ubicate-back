package app_jwt.auth_service.modules.auth.infrastructure.adapter.output.security;

import app_jwt.auth_service.modules.auth.domain.exception.InvalidCredentialsException;
import app_jwt.auth_service.modules.auth.domain.port.AuthSecurityPort;
import app_jwt.auth_service.shared.domain.model.Usuario;
import app_jwt.auth_service.shared.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthSecurityAdapter implements AuthSecurityPort {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public void authenticate(String username, String password) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
        } catch (AuthenticationException e) {
            throw new InvalidCredentialsException("Credenciales inválidas");
        }
    }

    @Override
    public String generateToken(Usuario usuario) {
        return jwtService.getToken(usuario, usuario);
    }
}
