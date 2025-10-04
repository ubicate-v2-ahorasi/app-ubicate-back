package app_jwt.auth_service.domain.dtos.bus;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class BusStatsResponse {
    private Long totalBuses;
    private Long busesActivos;
    private Long busesInactivos;
    private Long busesEnRuta;
    private Long busesEnMantenimiento;
    private Map<String, Long> estadoPorCantidad;

    private Long conectados;
    private Long enMovimiento;
    private Long detenidos;
    private Long sinConexion;

    private Long busesConRuta;
    private Long busesSinRuta;
}