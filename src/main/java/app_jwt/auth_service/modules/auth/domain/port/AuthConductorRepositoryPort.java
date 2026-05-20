package app_jwt.auth_service.modules.auth.domain.port;

import app_jwt.auth_service.modules.conductor.domain.model.Conductor;
import java.util.Optional;

public interface AuthConductorRepositoryPort {
    Optional<Conductor> findByUsuarioIdAndActivoTrue(Long usuarioId);
}
