package app_jwt.auth_service.modules.auth.domain.port;

import app_jwt.auth_service.shared.domain.model.Empresa;

public interface AuthCachePort {
    void upsertEmpresa(Empresa empresa);
}
