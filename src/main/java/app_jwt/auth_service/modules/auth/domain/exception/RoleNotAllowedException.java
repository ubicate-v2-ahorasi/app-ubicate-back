package app_jwt.auth_service.modules.auth.domain.exception;

public class RoleNotAllowedException extends AuthException {
    public RoleNotAllowedException(String message) {
        super(message);
    }
}
