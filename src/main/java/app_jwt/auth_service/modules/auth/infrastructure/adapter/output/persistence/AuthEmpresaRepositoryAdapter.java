package app_jwt.auth_service.modules.auth.infrastructure.adapter.output.persistence;

import app_jwt.auth_service.modules.auth.domain.port.AuthEmpresaRepositoryPort;
import app_jwt.auth_service.shared.domain.model.Empresa;
import app_jwt.auth_service.shared.infrastructure.persistence.EmpresaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthEmpresaRepositoryAdapter implements AuthEmpresaRepositoryPort {

    private final EmpresaRepository empresaRepository;

    @Override
    public Empresa save(Empresa empresa) {
        return empresaRepository.save(empresa);
    }

    @Override
    public boolean existsById(Long id) {
        return empresaRepository.existsById(id);
    }
}
