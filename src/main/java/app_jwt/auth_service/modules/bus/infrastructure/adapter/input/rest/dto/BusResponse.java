package app_jwt.auth_service.modules.bus.infrastructure.adapter.input.rest.dto;

import app_jwt.auth_service.modules.bus.domain.model.Bus;
import app_jwt.auth_service.modules.bus.domain.model.EstadoBus;
import app_jwt.auth_service.modules.conductor.domain.model.Conductor;
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
    private ConductorInfo conductor;

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

    @Data
    @Builder
    public static class ConductorInfo {
        private Long id;
        private String nombreCompleto;
        private String email;
        private String dni;
        private String telefono;
        private String numeroLicencia;
        private String categoriaLicencia;
        private String estado;
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

        ConductorInfo conductorInfo = null;
        Conductor conductor = bus.getConductorAsignado();
        if (conductor != null && Boolean.TRUE.equals(conductor.getActivo())) {
            conductorInfo = ConductorInfo.builder()
                    .id(conductor.getId())
                    .nombreCompleto(conductor.getUsuario().getNombre() + " " + conductor.getUsuario().getApellido())
                    .email(conductor.getUsuario().getCorreo())
                    .dni(conductor.getUsuario().getDni())
                    .telefono(conductor.getUsuario().getTelefono())
                    .numeroLicencia(conductor.getNumeroLicencia())
                    .categoriaLicencia(conductor.getCategoriaLicencia() != null ? conductor.getCategoriaLicencia().name() : null)
                    .estado(conductor.getEstado() != null ? conductor.getEstado().name() : null)
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
                .conductor(conductorInfo)
                .build();
    }
}
