package app_jwt.auth_service.modules.route.infrastructure.adapter.input.rest.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class BusPositionDTO {
    private Long busId;
    private String placa;
    private Double latitud;
    private Double longitud;
    private Double velocidad;
    private String estado;
    private LocalDateTime timestamp;

    public static BusPositionDTO from(Long busId, String placa, Double latitud, Double longitud, 
            Double velocidad, String estado, LocalDateTime timestamp) {
        return BusPositionDTO.builder()
                .busId(busId)
                .placa(placa)
                .latitud(latitud)
                .longitud(longitud)
                .velocidad(velocidad)
                .estado(estado)
                .timestamp(timestamp)
                .build();
    }
}