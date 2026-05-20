package app_jwt.auth_service.modules.auth.infrastructure.adapter.output.persistence;

import app_jwt.auth_service.modules.auth.domain.port.AuthUserRepositoryPort;
import app_jwt.auth_service.shared.domain.model.Usuario;
import app_jwt.auth_service.shared.infrastructure.persistence.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AuthUserRepositoryAdapter implements AuthUserRepositoryPort {

    private final UsuarioRepository usuarioRepository;

    @Override
    public Usuario save(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    @Override
    public Optional<Usuario> findByCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo);
    }

    @Override
    public Optional<Usuario> findByUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    @Override
    public boolean existsByEmpresaId(Long empresaId) {
        return usuarioRepository.findByEmpresaId(empresaId).isPresent();
    }
}
