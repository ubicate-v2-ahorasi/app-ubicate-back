package app_jwt.auth_service.modules.route.application.service;

import app_jwt.auth_service.modules.route.domain.model.EstadoRuta;
import app_jwt.auth_service.modules.route.domain.model.Route;
import app_jwt.auth_service.modules.route.domain.port.input.RouteService;
import app_jwt.auth_service.modules.route.infrastructure.adapter.input.rest.dto.BusPositionDTO;
import app_jwt.auth_service.modules.route.infrastructure.adapter.input.rest.dto.CreateRouteRequest;
import app_jwt.auth_service.modules.route.infrastructure.adapter.input.rest.dto.RouteResponse;
import app_jwt.auth_service.modules.route.infrastructure.adapter.input.rest.dto.UpdateRouteRequest;
import app_jwt.auth_service.modules.route.infrastructure.adapter.output.persistence.RouteRepository;
import app_jwt.auth_service.modules.bus.infrastructure.adapter.output.persistence.BusRepository;
import app_jwt.auth_service.shared.service.RedisRealtimeService;
import app_jwt.auth_service.shared.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RouteServiceImpl implements RouteService {

    private final RouteRepository routeRepository;
    private final BusRepository busRepository;
    private final RedisRealtimeService redisRealtimeService;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional
    public RouteResponse create(CreateRouteRequest req, Long empresaId) {
        if (routeRepository.existsByCodigoAndEmpresaIdAndActivoTrue(req.getCodigo(), empresaId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe una ruta con código '" + req.getCodigo() + "' en su empresa");
        }

        Route r = Route.builder()
                .nombre(req.getNombre())
                .descripcion(req.getDescripcion())
                .codigo(req.getCodigo())
                .origen(req.getOrigen())
                .destino(req.getDestino())
                .colorHex(req.getColorHex() == null ? null : req.getColorHex().trim())
                .polyline(req.getPolyline())
                .estado(EstadoRuta.ACTIVA)
                .activo(true)
                .empresaId(empresaId)
                .build();

        Route saved = routeRepository.save(r);
        return RouteResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RouteResponse> listAll(Long empresaId) {
        return routeRepository.findByEmpresaIdAndActivoTrueWithBuses(empresaId)
                .stream().map(RouteResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RouteResponse> listByEstado(Long empresaId, EstadoRuta estado) {
        return routeRepository.findByEmpresaIdAndEstadoAndActivoTrueWithBuses(empresaId, estado)
                .stream().map(RouteResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RouteResponse getById(Long id, Long empresaId) {
        Route r = routeRepository.findByIdWithBuses(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ruta no encontrada"));

        securityUtils.validateEmpresaAccess(r.getEmpresaId(), empresaId, "ruta");

        if (!Boolean.TRUE.equals(r.getActivo())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ruta no disponible");
        }

        return RouteResponse.from(r);
    }

    @Override
    @Transactional
    public RouteResponse update(Long id, UpdateRouteRequest req, Long empresaId) {
        Route r = routeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ruta no encontrada"));

        securityUtils.validateEmpresaAccess(r.getEmpresaId(), empresaId, "ruta");

        if (!Boolean.TRUE.equals(r.getActivo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede modificar una ruta inactiva");
        }

        if (req.getNombre() != null)      r.setNombre(req.getNombre());
        if (req.getDescripcion() != null) r.setDescripcion(req.getDescripcion());
        if (req.getOrigen() != null)      r.setOrigen(req.getOrigen());
        if (req.getDestino() != null)     r.setDestino(req.getDestino());
        if (req.getColorHex() != null)    r.setColorHex(req.getColorHex());
        if (req.getPolyline() != null)    r.setPolyline(req.getPolyline());
        if (req.getEstado() != null)      r.setEstado(req.getEstado());

        Route updated = routeRepository.save(r);
        return RouteResponse.from(updated);
    }

    @Override
    @Transactional
    public void delete(Long id, Long empresaId) {
        Route r = routeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ruta no encontrada"));

        securityUtils.validateEmpresaAccess(r.getEmpresaId(), empresaId, "ruta");

        Long busesAsignados = routeRepository.countBusesByRouteId(id);
        if (busesAsignados > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No se puede eliminar la ruta porque tiene " + busesAsignados + " bus(es) asignado(s)");
        }

        r.setActivo(false);
        routeRepository.save(r);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BusPositionDTO> getBusesPosicion(Long routeId, Long empresaId) {
        Route r = routeRepository.findById(routeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ruta no encontrada"));

        securityUtils.validateEmpresaAccess(r.getEmpresaId(), empresaId, "ruta");

        if (!Boolean.TRUE.equals(r.getActivo())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ruta no disponible");
        }

        List<BusPositionDTO> positions = redisRealtimeService.getBusesPosicionByRuta(routeId);

        if (positions.isEmpty()) {
            List<app_jwt.auth_service.modules.bus.domain.model.Bus> buses =
                    busRepository.findByRutaAsignadaAndActivoTrue(r, org.springframework.data.domain.Pageable.unpaged()).getContent();
            if (!buses.isEmpty()) {
                redisRealtimeService.syncRutaIndex(routeId, buses);
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
