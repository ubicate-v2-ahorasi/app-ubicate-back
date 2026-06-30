package app_jwt.auth_service.modules.auth.application.dto;

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
    private String refreshToken;
    private UserResponse user;
    private Long empresaId;
    private String message;

    public static AuthResponse success(String token, String refreshToken, UserResponse user) {
        return AuthResponse.builder()
                .token(token).refreshToken(refreshToken).user(user)
                .empresaId(user.getEmpresaId())
                .message("Autenticación exitosa")
                .build();
    }
}
