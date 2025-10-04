package app_jwt.auth_service.domain.dtos.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private String firebaseToken;
    private UserResponse user;
    private Long empresaId;
    private String message;

    public static AuthResponse success(String token, UserResponse user) {
        return AuthResponse.builder()
                .token(token)
                .user(user)
                .empresaId(user.getEmpresaId())
                .message("Autenticación exitosa")
                .build();
    }
}