package app_jwt.auth_service.modules.route.infrastructure.adapter.output.persistence;

import app_jwt.auth_service.modules.route.domain.model.RouteStop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RouteStopRepository extends JpaRepository<RouteStop, Long> {
    List<RouteStop> findByRouteIdAndActivoTrueOrderByOrdenAsc(Long routeId);

    long countByRouteIdAndActivoTrue(Long routeId);
}
