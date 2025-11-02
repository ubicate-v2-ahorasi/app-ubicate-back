package app_jwt.auth_service.domain.dtos.bus;

import app_jwt.auth_service.domain.entity.Bus;
import app_jwt.auth_service.domain.enums.EstadoBus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BusResponse {
    private Long id;
    private String placa;
    private String marca;
    private String modelo;
    private Integer capacidad;
    private Integer anio;
    private String color;
    private EstadoBus estado;
    private Boolean activo;
    private Long empresaId;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    private Double latitud;
    private Double longitud;
    private Double velocidad;
    private LocalDateTime ultimaUbicacion;

    private RutaInfo ruta;

    @Data
    @Builder
    public static class RutaInfo {
        private Long id;
        private String nombre;
        private String codigo;
        private String colorHex;
        private String origen;
        private String destino;
    }

    public static BusResponse from(Bus bus) {
        RutaInfo rutaInfo = null;
        if (bus.getRutaAsignada() != null) {
            rutaInfo = RutaInfo.builder()
                    .id(bus.getRutaAsignada().getId())
                    .nombre(bus.getRutaAsignada().getNombre())
                    .codigo(bus.getRutaAsignada().getCodigo())
                    .colorHex(bus.getRutaAsignada().getColorHex())
                    .origen(bus.getRutaAsignada().getOrigen())
                    .destino(bus.getRutaAsignada().getDestino())
                    .build();
        }

        return BusResponse.builder()
                .id(bus.getId())
                .placa(bus.getPlaca())
                .marca(bus.getMarca())
                .modelo(bus.getModelo())
                .capacidad(bus.getCapacidad())
                .anio(bus.getAnio())
                .color(bus.getColor())
                .estado(bus.getEstado())
                .activo(bus.getActivo())
                .empresaId(bus.getEmpresaId())
                .fechaCreacion(bus.getFechaCreacion())
                .fechaActualizacion(bus.getFechaActualizacion())
                .latitud(bus.getLatitud())
                .longitud(bus.getLongitud())
                .velocidad(bus.getVelocidad())
                .ultimaUbicacion(bus.getUltimaUbicacion())
                .ruta(rutaInfo)
                .build();
    }
}