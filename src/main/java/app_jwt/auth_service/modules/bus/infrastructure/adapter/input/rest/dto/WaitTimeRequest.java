package app_jwt.auth_service.modules.bus.infrastructure.adapter.input.rest.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WaitTimeRequest {
    @NotNull
    private String placaBus;
    @NotNull
    private Double userLatitud;
    @NotNull
    private Double userLongitud;
    @NotNull
    private Long rutaId;
}
