package app_jwt.auth_service.shared.utils;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class SecurityUtils {

    public void validateEmpresaAccess(Long recursoEmpresaId, Long usuarioEmpresaId, String recursoTipo) {
        if (recursoEmpresaId == null || usuarioEmpresaId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Error de identificación de empresa");
        }

        if (!recursoEmpresaId.equals(usuarioEmpresaId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No tiene permisos para acceder a " + recursoTipo + " de otra empresa");
        }
    }

    public void validateMultipleEmpresaAccess(Iterable<?> recursos, Long usuarioEmpresaId, String recursoTipo) {
        for (Object recurso : recursos) {
            if (recurso instanceof EmpresaAware empresaAware) {
                validateEmpresaAccess(empresaAware.getEmpresaId(), usuarioEmpresaId, recursoTipo);
            }
        }
    }
}
