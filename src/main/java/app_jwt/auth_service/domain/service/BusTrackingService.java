// src/main/java/app_jwt/auth_service/domain/service/BusTrackingService.java
package app_jwt.auth_service.domain.service;

import app_jwt.auth_service.domain.dtos.bus.WaitTimeRequest;
import app_jwt.auth_service.domain.dtos.bus.WaitTimeResponse;
import app_jwt.auth_service.domain.entity.Bus;
import app_jwt.auth_service.domain.entity.Route;
import app_jwt.auth_service.domain.enums.EstadoBus;
import app_jwt.auth_service.infra.repository.BusRepository;
import app_jwt.auth_service.infra.security.SecurityUtils;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.*;
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
public class BusTrackingService {

    private final BusRepository busRepository;
    private final SecurityUtils securityUtils;

    @Value("${google.maps.api.key:}")
    private String googleMapsApiKey;

    @Transactional(readOnly = true)
    public WaitTimeResponse calcularTiempoEspera(WaitTimeRequest request, Long empresaId) {
        try {
            // 1. Obtener el bus con su ruta
            Bus bus = busRepository.findByPlacaWithRoute(request.getPlacaBus())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bus no encontrado"));

            // 2. Validar empresa
            securityUtils.validateEmpresaAccess(bus.getEmpresaId(), empresaId, "bus");

            // 3. Validar que el bus esté activo y en ruta
            if (!bus.getActivo() || bus.getEstado() != EstadoBus.EN_RUTA) {
                return WaitTimeResponse.builder()
                        .calculoExitoso(false)
                        .mensaje("El bus no está en ruta actualmente")
                        .placaBus(bus.getPlaca())
                        .build();
            }

            // 4. Validar que tenga ubicación actual
            if (bus.getLatitud() == null || bus.getLongitud() == null) {
                return WaitTimeResponse.builder()
                        .calculoExitoso(false)
                        .mensaje("El bus no tiene ubicación GPS disponible")
                        .placaBus(bus.getPlaca())
                        .build();
            }

            Route ruta = bus.getRutaAsignada();
            if (ruta == null || ruta.getPolyline() == null) {
                return WaitTimeResponse.builder()
                        .calculoExitoso(false)
                        .mensaje("La ruta no tiene polyline configurada")
                        .placaBus(bus.getPlaca())
                        .build();
            }

            // 5. Calcular tiempo usando Google Maps API
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

            // Configurar Google Maps API
            GeoApiContext context = new GeoApiContext.Builder()
                    .apiKey(googleMapsApiKey)
                    .build();

            // Calcular ruta desde el bus hasta el usuario
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
        // Cálculo básico usando distancia euclidiana
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
        double distanciaKm = 6371 * c; // Radio de la Tierra en km

        // Velocidad estimada (25 km/h en ciudad)
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

    @Transactional
    public void actualizarUbicacionBus(String placa, Double latitud, Double longitud, Long empresaId) {
        Bus bus = busRepository.findByPlacaAndActivoTrue(placa)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bus no encontrado"));

        securityUtils.validateEmpresaAccess(bus.getEmpresaId(), empresaId, "bus");

        bus.setLatitud(latitud);
        bus.setLongitud(longitud);
        bus.setUltimaUbicacion(LocalDateTime.now());

        // Si no estaba en ruta, cambiar estado
        if (bus.getEstado() == EstadoBus.INACTIVO) {
            bus.setEstado(EstadoBus.EN_RUTA);
        }

        busRepository.save(bus);
        log.info("Ubicación actualizada para bus {}: ({}, {})", placa, latitud, longitud);
    }
}