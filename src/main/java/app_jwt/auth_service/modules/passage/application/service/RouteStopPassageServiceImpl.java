package app_jwt.auth_service.modules.passage.application.service;

import app_jwt.auth_service.modules.passage.domain.model.RouteStopPassage;
import app_jwt.auth_service.modules.passage.domain.port.input.RouteStopPassageService;
import app_jwt.auth_service.modules.passage.infrastructure.adapter.output.persistence.RouteStopPassageRepository;
import app_jwt.auth_service.modules.route.domain.model.RouteStop;
import app_jwt.auth_service.modules.route.infrastructure.adapter.output.persistence.RouteStopRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RouteStopPassageServiceImpl implements RouteStopPassageService {

    private final RouteStopRepository routeStopRepository;
    private final RouteStopPassageRepository passageRepository;

    /** Radio (metros) dentro del cual se considera que el bus cruzo la parada. */
    private static final double RADIO_METROS = 50.0;

    @Override
    @Transactional
    public void detectAndRecord(Long busId, String placa, Long rutaId, Double latitud, Double longitud) {
        if (busId == null || rutaId == null || latitud == null || longitud == null) {
            return;
        }

        List<RouteStop> stops = routeStopRepository.findByRouteIdAndActivoTrueOrderByOrdenAsc(rutaId);
        if (stops.isEmpty()) {
            return;
        }

        RouteStop nearest = null;
        double nearestDist = Double.MAX_VALUE;
        for (RouteStop s : stops) {
            if (s.getLatitud() == null || s.getLongitud() == null) continue;
            double d = haversine(latitud, longitud, s.getLatitud(), s.getLongitud());
            if (d < nearestDist) {
                nearestDist = d;
                nearest = s;
            }
        }

        if (nearest == null || nearestDist > RADIO_METROS) {
            return;
        }

        Optional<RouteStopPassage> last = passageRepository.findTopByBusIdOrderByHoraCruceDesc(busId);
        if (last.isPresent() && nearest.getId().equals(last.get().getRouteStopId())) {
            // El bus sigue sobre la misma parada: no duplicar el registro.
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        Integer delta = last
                .map(p -> (int) Duration.between(p.getHoraCruce(), now).getSeconds())
                .orElse(null);

        RouteStopPassage passage = RouteStopPassage.builder()
                .rutaId(rutaId)
                .routeStopId(nearest.getId())
                .busId(busId)
                .placa(placa)
                .orden(nearest.getOrden())
                .nombreParada(nearest.getNombre())
                .horaCruce(now)
                .segundosDesdeAnterior(delta)
                .build();

        passageRepository.save(passage);
        log.info("Paso registrado: bus {} -> parada {} ({}) ruta {} (delta={}s)",
                busId, nearest.getOrden(), nearest.getNombre(), rutaId, delta);
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final double earthRadius = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return earthRadius * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
