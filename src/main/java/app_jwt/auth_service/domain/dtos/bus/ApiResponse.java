package app_jwt.auth_service.domain.dtos.bus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class ApiResponse {
    private String message;
    private boolean success;
}
