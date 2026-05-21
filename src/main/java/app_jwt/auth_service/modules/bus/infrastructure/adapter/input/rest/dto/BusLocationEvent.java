package app_jwt.auth_service.modules.bus.infrastructure.adapter.input.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
    private String timestamp;

    private Long empresaId;
    private Long rutaId;

    public static BusLocationEvent create(
            Long busId, String placa, Double latitud, Double longitud,
            Double velocidad, String estado, LocalDateTime timestamp,
            Long empresaId, Long rutaId) {
        return BusLocationEvent.builder()
                .busId(busId)
                .placa(placa)
                .latitud(latitud)
                .longitud(longitud)
                .velocidad(velocidad)
                .estado(estado)
                .timestamp(timestamp != null ? timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null)
                .empresaId(empresaId)
                .rutaId(rutaId)
                .build();
    }
}
