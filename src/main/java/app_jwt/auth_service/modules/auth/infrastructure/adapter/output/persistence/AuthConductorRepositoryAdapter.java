package app_jwt.auth_service.modules.auth.infrastructure.adapter.output.persistence;

import app_jwt.auth_service.modules.auth.domain.port.AuthConductorRepositoryPort;
import app_jwt.auth_service.modules.conductor.domain.model.Conductor;
import app_jwt.auth_service.modules.conductor.infrastructure.adapter.output.persistence.ConductorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AuthConductorRepositoryAdapter implements AuthConductorRepositoryPort {

    private final ConductorRepository conductorRepository;

    @Override
    public Optional<Conductor> findByUsuarioIdAndActivoTrue(Long usuarioId) {
        return conductorRepository.findByUsuarioIdAndActivoTrue(usuarioId);
    }
}
