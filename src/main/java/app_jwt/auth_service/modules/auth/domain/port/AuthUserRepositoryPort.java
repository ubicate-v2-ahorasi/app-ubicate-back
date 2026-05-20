package app_jwt.auth_service.modules.auth.domain.port;

import app_jwt.auth_service.shared.domain.model.Usuario;
import java.util.Optional;

public interface AuthUserRepositoryPort {
    Usuario save(Usuario usuario);
    Optional<Usuario> findByCorreo(String correo);
    Optional<Usuario> findByUsername(String username);
    boolean existsByEmpresaId(Long empresaId);
}
