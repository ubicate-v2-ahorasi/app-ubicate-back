package app_jwt.auth_service.modules.passage.infrastructure.adapter.output.persistence;

import app_jwt.auth_service.modules.passage.domain.model.RouteStopPassage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RouteStopPassageRepository extends JpaRepository<RouteStopPassage, Long> {

    List<RouteStopPassage> findByRutaIdAndBusIdOrderByHoraCruceAsc(Long rutaId, Long busId);

    List<RouteStopPassage> findByRutaIdOrderByHoraCruceAsc(Long rutaId);

    Optional<RouteStopPassage> findTopByBusIdOrderByHoraCruceDesc(Long busId);
}
