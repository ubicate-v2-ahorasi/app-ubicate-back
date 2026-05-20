package app_jwt.auth_service.shared.application.service;

import app_jwt.auth_service.modules.bus.infrastructure.adapter.input.rest.dto.BusLocationResponse;
import app_jwt.auth_service.modules.bus.domain.model.Bus;
import app_jwt.auth_service.modules.bus.domain.model.EstadoBus;
import app_jwt.auth_service.modules.bus.infrastructure.adapter.output.persistence.BusRepository;
import app_jwt.auth_service.modules.route.infrastructure.adapter.input.rest.dto.BusPositionDTO;
import app_jwt.auth_service.shared.infrastructure.adapter.input.rest.dto.EmpresaPublicResponse;
import app_jwt.auth_service.modules.route.infrastructure.adapter.input.rest.dto.RouteResponse;
import app_jwt.auth_service.modules.route.domain.model.Route;
import app_jwt.auth_service.modules.route.domain.model.EstadoRuta;
import app_jwt.auth_service.modules.route.infrastructure.adapter.output.persistence.RouteRepository;
import app_jwt.auth_service.shared.infrastructure.persistence.EmpresaRepository;
import app_jwt.auth_service.shared.service.RedisRealtimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PublicService {

    private final BusRepository busRepository;
    private final RouteRepository routeRepository;
    private final EmpresaRepository empresaRepository;
    private final RedisRealtimeService redisRealtimeService;

    @Transactional(readOnly = true)
    public List<EmpresaPublicResponse> getAllEmpresas() {
        return empresaRepository.findByActivoTrue()
                .stream()
                .map(EmpresaPublicResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RouteResponse> getRutasByEmpresa(Long empresaId) {
        if (!empresaRepository.existsByIdAndActivoTrue(empresaId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa no encontrada");
        }

        return routeRepository
                .findByEmpresaIdAndActivoTrueAndEstadoWithBuses(empresaId, EstadoRuta.ACTIVA)
                .stream()
                .map(RouteResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BusLocationResponse> getBusesActivosByEmpresa(Long empresaId) {
        if (!empresaRepository.existsByIdAndActivoTrue(empresaId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa no encontrada");
        }

        return busRepository
                .findByEmpresaIdAndActivoTrueAndLatitudIsNotNullAndLongitudIsNotNullWithRoute(empresaId)
                .stream()
                .filter(b -> b.getEstado() == EstadoBus.EN_RUTA || b.getEstado() == EstadoBus.ACTIVO)
                .map(BusLocationResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BusLocationResponse> getBusesUpdatedSince(Long empresaId, LocalDateTime since) {
        if (!empresaRepository.existsByIdAndActivoTrue(empresaId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa no encontrada");
        }

        return busRepository
                .findByEmpresaIdAndActivoTrueAndLatitudIsNotNullAndLongitudIsNotNullWithRoute(empresaId)
                .stream()
                .filter(b -> b.getEstado() == EstadoBus.EN_RUTA || b.getEstado() == EstadoBus.ACTIVO)
                .filter(b -> b.getUltimaUbicacion() != null && b.getUltimaUbicacion().isAfter(since))
                .map(BusLocationResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BusLocationResponse getBusUbicacion(Long busId) {
        Bus bus = busRepository.findByIdWithRoute(busId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Bus no encontrado"));

        if (!Boolean.TRUE.equals(bus.getActivo())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Bus no disponible");
        }

        return BusLocationResponse.from(bus);
    }

    @Transactional(readOnly = true)
    public RouteResponse getRutaById(Long rutaId) {
        Route ruta = routeRepository.findByIdWithBuses(rutaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Ruta no encontrada"));

        if (!Boolean.TRUE.equals(ruta.getActivo()) || ruta.getEstado() != EstadoRuta.ACTIVA) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Ruta no disponible");
        }

        return RouteResponse.from(ruta);
    }

    @Transactional(readOnly = true)
    public List<BusPositionDTO> getBusesPosicionByRuta(Long rutaId) {
        Route ruta = routeRepository.findById(rutaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Ruta no encontrada"));

        if (!Boolean.TRUE.equals(ruta.getActivo()) || ruta.getEstado() != EstadoRuta.ACTIVA) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Ruta no disponible");
        }

        List<BusPositionDTO> positions = redisRealtimeService.getBusesPosicionByRuta(rutaId);

        if (positions.isEmpty()) {
            List<Bus> buses = busRepository
                    .findByRutaAsignadaAndActivoTrue(ruta, org.springframework.data.domain.Pageable.unpaged())
                    .getContent();
            if (!buses.isEmpty()) {
                redisRealtimeService.syncRutaIndex(rutaId, buses);
                positions = buses.stream()
                        .map(b -> BusPositionDTO.from(
                                b.getId(), b.getPlaca(), b.getLatitud(), b.getLongitud(),
                                b.getVelocidad(), b.getEstado().name(), b.getUltimaUbicacion()))
                        .toList();
            }
        }

        return positions;
    }
}
