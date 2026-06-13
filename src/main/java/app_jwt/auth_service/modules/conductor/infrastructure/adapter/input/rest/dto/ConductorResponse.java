package app_jwt.auth_service.modules.conductor.infrastructure.adapter.input.rest.dto;

import app_jwt.auth_service.modules.conductor.domain.model.Conductor;
import app_jwt.auth_service.modules.conductor.domain.model.CategoriaLicencia;
import app_jwt.auth_service.modules.conductor.domain.model.EstadoConductor;
import app_jwt.auth_service.modules.conductor.domain.model.TurnoConductor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ConductorResponse {
    private Long id;
    private String nombreCompleto;
    private String email;
    private String dni;
    private String telefono;
    private String numeroLicencia;
    private CategoriaLicencia categoriaLicencia;
    private LocalDate fechaVencimientoLicencia;
    private TurnoConductor turno;
    private EstadoConductor estado;
    private String placaBusAsignado;
    private Boolean licenciaVencida;
    private Boolean licenciaPorVencer;
    private Long empresaId;
    private String empresaNombre;

    public static ConductorResponse from(Conductor conductor) {
        return ConductorResponse.builder()
                .id(conductor.getId())
                .nombreCompleto(conductor.getUsuario().getNombre() + " " + conductor.getUsuario().getApellido())
                .email(conductor.getUsuario().getCorreo())
                .dni(conductor.getUsuario().getDni())
                .telefono(conductor.getUsuario().getTelefono())
                .numeroLicencia(conductor.getNumeroLicencia())
                .categoriaLicencia(conductor.getCategoriaLicencia())
                .fechaVencimientoLicencia(conductor.getFechaVencimientoLicencia())
                .turno(conductor.getTurno())
                .estado(conductor.getEstado())
                .placaBusAsignado(conductor.getBusAsignado() != null ? conductor.getBusAsignado().getPlaca() : "Sin asignar")
                .licenciaVencida(conductor.isLicenciaVencida())
                .licenciaPorVencer(conductor.isLicenciaPorVencer())
                .empresaId(conductor.getEmpresaId())
                .empresaNombre("Cargando...")
                .build();
    }

    public static ConductorResponse fromWithEmpresa(Conductor conductor, String empresaNombre) {
        ConductorResponse response = from(conductor);
        response.setEmpresaNombre(empresaNombre);
        return response;
    }
}
