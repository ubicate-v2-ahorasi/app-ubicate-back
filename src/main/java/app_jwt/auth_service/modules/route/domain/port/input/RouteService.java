package app_jwt.auth_service.modules.route.domain.port.input;

import app_jwt.auth_service.modules.route.domain.model.EstadoRuta;
import app_jwt.auth_service.modules.route.infrastructure.adapter.input.rest.dto.CreateRouteRequest;
import app_jwt.auth_service.modules.route.infrastructure.adapter.input.rest.dto.RouteResponse;
import app_jwt.auth_service.modules.route.infrastructure.adapter.input.rest.dto.UpdateRouteRequest;

import java.util.List;

public interface RouteService {
    RouteResponse create(CreateRouteRequest request, Long empresaId);
    List<RouteResponse> listAll(Long empresaId);
    List<RouteResponse> listByEstado(Long empresaId, EstadoRuta estado);
    RouteResponse getById(Long routeId, Long empresaId);
    RouteResponse update(Long routeId, UpdateRouteRequest request, Long empresaId);
    void delete(Long routeId, Long empresaId);
}
