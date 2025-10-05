package app_jwt.auth_service.domain.dtos.conductor;

import app_jwt.auth_service.domain.entity.Conductor;
import app_jwt.auth_service.domain.enums.CategoriaLicencia;
import app_jwt.auth_service.domain.enums.EstadoConductor;
import app_jwt.auth_service.domain.enums.TurnoConductor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConductorResponse {
    private Long id;
    private String nombreCompleto;
    private String dni;
    private String telefono;
    private String numeroLicencia;
    private CategoriaLicencia categoriaLicencia;
    private TurnoConductor turno;
    private EstadoConductor estado;
    private String placaBusAsignado;
    private Boolean licenciaVencida;
    private Boolean licenciaPorVencer;

    // ✅ INFORMACIÓN DE LA EMPRESA
    private Long empresaId;
    private String empresaNombre;

    public static ConductorResponse from(Conductor conductor) {
        return ConductorResponse.builder()
                .id(conductor.getId())
                .nombreCompleto(conductor.getUsuario().getNombre() + " " + conductor.getUsuario().getApellido())
                .dni(conductor.getUsuario().getDni())
                .telefono(conductor.getUsuario().getTelefono())
                .numeroLicencia(conductor.getNumeroLicencia())
                .categoriaLicencia(conductor.getCategoriaLicencia())
                .turno(conductor.getTurno())
                .estado(conductor.getEstado())
                .placaBusAsignado(conductor.getBusAsignado() != null ? conductor.getBusAsignado().getPlaca() : "Sin asignar")
                .licenciaVencida(conductor.isLicenciaVencida())
                .licenciaPorVencer(conductor.isLicenciaPorVencer())
                .empresaId(conductor.getEmpresaId())
                .empresaNombre("Cargando...") // Se llenará en el servicio
                .build();
    }

    public static ConductorResponse fromWithEmpresa(Conductor conductor, String empresaNombre) {
        ConductorResponse response = from(conductor);
        response.setEmpresaNombre(empresaNombre);
        return response;
    }
}