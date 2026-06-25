package app_jwt.auth_service.modules.passage.domain.port.input;

public interface RouteStopPassageService {

    /**
     * Detecta si el bus esta sobre una parada de su ruta y, de ser asi, registra
     * el paso (con la hora y la demora desde la parada anterior). Idempotente
     * mientras el bus permanezca en la misma parada.
     */
    void detectAndRecord(Long busId, String placa, Long rutaId, Double latitud, Double longitud);
}
