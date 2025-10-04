package app_jwt.auth_service.domain.dtos.conductor;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConductorCreatedResponse {
    private ConductorResponse conductor;
    private String username;
    private String tempPassword;
}