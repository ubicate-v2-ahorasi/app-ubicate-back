package app_jwt.auth_service.modules.auth.domain.port;

import app_jwt.auth_service.shared.domain.model.Empresa;

public interface AuthEmpresaRepositoryPort {
    Empresa save(Empresa empresa);
    boolean existsById(Long id);
}
