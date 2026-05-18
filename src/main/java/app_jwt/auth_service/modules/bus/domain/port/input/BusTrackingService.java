package app_jwt.auth_service.modules.bus.domain.port.input;

import app_jwt.auth_service.modules.bus.infrastructure.adapter.input.rest.dto.WaitTimeRequest;
import app_jwt.auth_service.modules.bus.infrastructure.adapter.input.rest.dto.WaitTimeResponse;

public interface BusTrackingService {
    WaitTimeResponse calcularTiempoEspera(WaitTimeRequest request, Long empresaId);
    void actualizarUbicacionBus(String placa, Double latitud, Double longitud, Long empresaId);
}
