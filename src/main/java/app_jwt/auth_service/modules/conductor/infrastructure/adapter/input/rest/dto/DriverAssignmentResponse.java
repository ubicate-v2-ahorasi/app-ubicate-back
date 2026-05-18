package app_jwt.auth_service.modules.conductor.infrastructure.adapter.input.rest.dto;

import app_jwt.auth_service.modules.bus.domain.model.Bus;
import app_jwt.auth_service.modules.route.domain.model.Route;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DriverAssignmentResponse {
    Long conductorId;
    Long busId;
    String busPlaca;
    Long rutaId;
    String rutaNombre;
    String origen;
    String destino;
    String colorHex;
    String polyline;

    public static DriverAssignmentResponse of(Long conductorId, Bus bus, Route ruta) {
        return DriverAssignmentResponse.builder()
                .conductorId(conductorId)
                .busId(bus != null ? bus.getId() : null)
                .busPlaca(bus != null ? bus.getPlaca() : null)
                .rutaId(ruta != null ? ruta.getId() : null)
                .rutaNombre(ruta != null ? ruta.getNombre() : null)
                .origen(ruta != null ? ruta.getOrigen() : null)
                .destino(ruta != null ? ruta.getDestino() : null)
                .colorHex(ruta != null ? ruta.getColorHex() : null)
                .polyline(ruta != null ? ruta.getPolyline() : null)
                .build();
    }
}
