package app_jwt.auth_service.modules.bus.infrastructure.adapter.input.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class WaitTimeResponse {
    private Integer tiempoEsperaMinutos;
    private Double distanciaKm;
    private String placaBus;
    private Double busLatitud;
    private Double busLongitud;
    private String nombreRuta;
    private Boolean calculoExitoso;
    private String mensaje;
}
