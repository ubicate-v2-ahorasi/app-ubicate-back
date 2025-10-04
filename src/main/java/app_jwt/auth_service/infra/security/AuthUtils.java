package app_jwt.auth_service.infra.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class AuthUtils {

    public Long getEmpresaId(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
        }
        if (auth.getPrincipal() instanceof JwtUser jwtUser) {
            Long empresaId = jwtUser.getEmpresaId();
            if (empresaId == null) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "empresaId no presente en el token");
            }
            return empresaId;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Principal inválido");
    }

    public Long getUserId(Authentication auth) {
        if (auth != null && auth.getPrincipal() instanceof JwtUser jwtUser) {
            return jwtUser.getUserId();
        }
        return null;
    }
}
