package app_jwt.auth_service.domain.service;

import app_jwt.auth_service.domain.dtos.route.CreateRouteRequest;
import app_jwt.auth_service.domain.dtos.route.RouteResponse;
import app_jwt.auth_service.domain.dtos.route.UpdateRouteRequest;
import app_jwt.auth_service.domain.entity.Route;
import app_jwt.auth_service.domain.enums.EstadoRuta;
import app_jwt.auth_service.infra.repository.RouteRepository;
import app_jwt.auth_service.infra.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RouteService {

    private final RouteRepository routeRepository;
    private final SecurityUtils securityUtils;

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

    @Transactional(readOnly = true)
    public List<RouteResponse> listAll(Long empresaId) {
        List<Route> routes = routeRepository.findByEmpresaIdAndActivoTrueWithBuses(empresaId);
        return routes.stream().map(RouteResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<RouteResponse> listByEstado(Long empresaId, EstadoRuta estado) {
        List<Route> routes = routeRepository.findByEmpresaIdAndEstadoAndActivoTrueWithBuses(empresaId, estado);
        return routes.stream().map(RouteResponse::from).toList();
    }

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

    @Transactional
    public RouteResponse update(Long id, UpdateRouteRequest req, Long empresaId) {
        Route r = routeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ruta no encontrada"));

        securityUtils.validateEmpresaAccess(r.getEmpresaId(), empresaId, "ruta");

        if (!Boolean.TRUE.equals(r.getActivo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede modificar una ruta inactiva");
        }

        if (req.getNombre() != null) r.setNombre(req.getNombre());
        if (req.getDescripcion() != null) r.setDescripcion(req.getDescripcion());
        if (req.getOrigen() != null) r.setOrigen(req.getOrigen());
        if (req.getDestino() != null) r.setDestino(req.getDestino());
        if (req.getColorHex() != null) r.setColorHex(req.getColorHex());
        if (req.getPolyline() != null) r.setPolyline(req.getPolyline());
        if (req.getEstado() != null) r.setEstado(req.getEstado());

        Route updated = routeRepository.save(r);
        return RouteResponse.from(updated);
    }

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
}