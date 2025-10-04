package app_jwt.auth_service.domain.dtos.bus;

import app_jwt.auth_service.domain.entity.Bus;
import app_jwt.auth_service.domain.enums.EstadoBus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BusLocationResponse {
    private Long id;
    private String placa;
    private Double latitud;
    private Double longitud;
    private Double velocidad;
    private LocalDateTime ultimaUbicacion;
    private EstadoBus estado;
    private Long rutaId;
    private String rutaNombre;
    private String modelo;

    public static BusLocationResponse from(Bus bus) {
        return BusLocationResponse.builder()
                .id(bus.getId())
                .placa(bus.getPlaca())
                .latitud(bus.getLatitud())
                .longitud(bus.getLongitud())
                .velocidad(bus.getVelocidad())
                .ultimaUbicacion(bus.getUltimaUbicacion())
                .estado(bus.getEstado())
                .rutaId(bus.getRutaAsignada() != null ? bus.getRutaAsignada().getId() : null)
                .rutaNombre(bus.getRutaAsignada() != null ? bus.getRutaAsignada().getNombre() : null)
                .modelo(bus.getModelo())
                .build();
    }
}