package app_jwt.auth_service.modules.bus.infrastructure.adapter.input.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class BusLocationEvent {
    private Long busId;
    private String placa;
    private Double latitud;
    private Double longitud;
    private Double velocidad;
    private String estado;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    private Long empresaId;
    private Long rutaId;
}
