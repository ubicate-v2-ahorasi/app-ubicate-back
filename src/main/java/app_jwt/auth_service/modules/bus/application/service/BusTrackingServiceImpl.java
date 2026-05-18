package app_jwt.auth_service.modules.bus.application.service;

import app_jwt.auth_service.modules.bus.domain.model.Bus;
import app_jwt.auth_service.modules.bus.domain.model.EstadoBus;
import app_jwt.auth_service.modules.bus.domain.port.input.BusTrackingService;
import app_jwt.auth_service.modules.bus.infrastructure.adapter.input.rest.dto.BusLocationEvent;
import app_jwt.auth_service.modules.bus.infrastructure.adapter.input.rest.dto.WaitTimeRequest;
import app_jwt.auth_service.modules.bus.infrastructure.adapter.input.rest.dto.WaitTimeResponse;
import app_jwt.auth_service.modules.bus.infrastructure.adapter.output.persistence.BusRepository;
import app_jwt.auth_service.modules.route.domain.model.Route;
import app_jwt.auth_service.shared.service.RedisRealtimeService;
import app_jwt.auth_service.shared.utils.SecurityUtils;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.LatLng;
import com.google.maps.model.TravelMode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class BusTrackingServiceImpl implements BusTrackingService {

    private final BusRepository busRepository;
    private final SecurityUtils securityUtils;
    private final RedisRealtimeService redisRealtimeService;

    @Value("${google.maps.api.key:}")
    private String googleMapsApiKey;

    @Override
    @Transactional(readOnly = true)
    public WaitTimeResponse calcularTiempoEspera(WaitTimeRequest request, Long empresaId) {
        try {
            Bus bus = busRepository.findByPlacaWithRoute(request.getPlacaBus())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bus no encontrado"));

            securityUtils.validateEmpresaAccess(bus.getEmpresaId(), empresaId, "bus");

            if (!bus.getActivo() || bus.getEstado() != EstadoBus.EN_RUTA) {
                return WaitTimeResponse.builder()
                        .calculoExitoso(false)
                        .mensaje("El bus no está en ruta actualmente")
                        .placaBus(bus.getPlaca())
                        .build();
            }

            if (bus.getLatitud() == null || bus.getLongitud() == null) {
                return WaitTimeResponse.builder()
                        .calculoExitoso(false)
                        .mensaje("El bus no tiene ubicación GPS disponible")
                        .placaBus(bus.getPlaca())
                        .build();
            }

            // Using local model import
            var ruta = bus.getRutaAsignada();
            if (ruta == null || ruta.getPolyline() == null) {
                return WaitTimeResponse.builder()
                        .calculoExitoso(false)
                        .mensaje("La ruta no tiene polyline configurada")
                        .placaBus(bus.getPlaca())
                        .build();
            }

            return calcularConGoogleMaps(bus, request.getUserLatitud(), request.getUserLongitud());

        } catch (Exception e) {
            log.error("Error calculando tiempo de espera: ", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error en el cálculo: " + e.getMessage());
        }
    }

    private WaitTimeResponse calcularConGoogleMaps(Bus bus, Double userLat, Double userLng) {
        try {
            if (googleMapsApiKey == null || googleMapsApiKey.isEmpty()) {
                return calcularTiempoEstimado(bus, userLat, userLng);
            }

            GeoApiContext context = new GeoApiContext.Builder()
                    .apiKey(googleMapsApiKey)
                    .build();

            DirectionsResult result = DirectionsApi.newRequest(context)
                    .origin(new LatLng(bus.getLatitud(), bus.getLongitud()))
                    .destination(new LatLng(userLat, userLng))
                    .mode(TravelMode.DRIVING)
                    .await();

            if (result.routes.length > 0) {
                DirectionsRoute googleRoute = result.routes[0];
                DirectionsLeg leg = googleRoute.legs[0];

                long durationSeconds = leg.duration.inSeconds;
                int tiempoMinutos = (int) (durationSeconds / 60);
                double distanciaKm = leg.distance.inMeters / 1000.0;

                return WaitTimeResponse.builder()
                        .tiempoEsperaMinutos(tiempoMinutos)
                        .distanciaKm(distanciaKm)
                        .placaBus(bus.getPlaca())
                        .busLatitud(bus.getLatitud())
                        .busLongitud(bus.getLongitud())
                        .nombreRuta(bus.getRutaAsignada().getNombre())
                        .calculoExitoso(true)
                        .mensaje("Tiempo calculado con Google Maps")
                        .build();
            }

            return calcularTiempoEstimado(bus, userLat, userLng);

        } catch (Exception e) {
            log.warn("Error con Google Maps API, usando cálculo estimado: ", e);
            return calcularTiempoEstimado(bus, userLat, userLng);
        }
    }

    private WaitTimeResponse calcularTiempoEstimado(Bus bus, Double userLat, Double userLng) {
        double lat1 = Math.toRadians(bus.getLatitud());
        double lon1 = Math.toRadians(bus.getLongitud());
        double lat2 = Math.toRadians(userLat);
        double lon2 = Math.toRadians(userLng);

        double dlat = lat2 - lat1;
        double dlon = lon2 - lon1;
        double a = Math.sin(dlat/2) * Math.sin(dlat/2) +
                Math.cos(lat1) * Math.cos(lat2) *
                        Math.sin(dlon/2) * Math.sin(dlon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double distanciaKm = 6371 * c; 

        double velocidadKmh = bus.getVelocidad() != null ? bus.getVelocidad() : 25.0;
        int tiempoMinutos = (int) ((distanciaKm / velocidadKmh) * 60);

        return WaitTimeResponse.builder()
                .tiempoEsperaMinutos(tiempoMinutos)
                .distanciaKm(distanciaKm)
                .placaBus(bus.getPlaca())
                .busLatitud(bus.getLatitud())
                .busLongitud(bus.getLongitud())
                .nombreRuta(bus.getRutaAsignada().getNombre())
                .calculoExitoso(true)
                .mensaje("Tiempo estimado (sin Google Maps)")
                .build();
    }

    @Override
    @Transactional
    public void actualizarUbicacionBus(String placa, Double latitud, Double longitud, Long empresaId) {
        Bus bus = busRepository.findByPlacaAndActivoTrue(placa)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bus no encontrado"));

        securityUtils.validateEmpresaAccess(bus.getEmpresaId(), empresaId, "bus");

        bus.setLatitud(latitud);
        bus.setLongitud(longitud);
        bus.setUltimaUbicacion(LocalDateTime.now());

        if (bus.getEstado() == EstadoBus.INACTIVO) {
            bus.setEstado(EstadoBus.EN_RUTA);
        }

        busRepository.save(bus);
        
        // Prepare event
        BusLocationEvent event = BusLocationEvent.builder()
                .busId(bus.getId())
                .placa(bus.getPlaca())
                .latitud(latitud)
                .longitud(longitud)
                .velocidad(bus.getVelocidad())
                .estado(bus.getEstado().name())
                .timestamp(bus.getUltimaUbicacion())
                .empresaId(bus.getEmpresaId())
                .rutaId(bus.getRutaAsignada() != null ? bus.getRutaAsignada().getId() : null)
                .build();
        
        // Broadcast via Redis & WebSocket
        redisRealtimeService.publishBusLocation(bus.getEmpresaId(), bus.getId(), event);
        
        log.info("Ubicación actualizada y publicada para bus {}: ({}, {})", placa, latitud, longitud);
    }
}
