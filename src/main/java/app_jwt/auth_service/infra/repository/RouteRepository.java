package app_jwt.auth_service.infra.repository;

import app_jwt.auth_service.domain.entity.Route;
import app_jwt.auth_service.domain.enums.EstadoRuta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {

    boolean existsByCodigoAndEmpresaIdAndActivoTrue(String codigo, Long empresaId);

    List<Route> findByEmpresaIdAndActivoTrue(Long empresaId);
    List<Route> findByEmpresaIdAndEstadoAndActivoTrue(Long empresaId, EstadoRuta estado);

    @Query("SELECT DISTINCT r FROM Route r LEFT JOIN FETCH r.buses b WHERE r.empresaId = :empresaId AND r.activo = true AND (b.activo = true OR b IS NULL)")
    List<Route> findByEmpresaIdAndActivoTrueWithBuses(@Param("empresaId") Long empresaId);

    @Query("SELECT DISTINCT r FROM Route r LEFT JOIN FETCH r.buses b WHERE r.empresaId = :empresaId AND r.estado = :estado AND r.activo = true AND (b.activo = true OR b IS NULL)")
    List<Route> findByEmpresaIdAndEstadoAndActivoTrueWithBuses(@Param("empresaId") Long empresaId, @Param("estado") EstadoRuta estado);

    @Query("SELECT r FROM Route r LEFT JOIN FETCH r.buses b WHERE r.id = :routeId AND (b.activo = true OR b IS NULL)")
    Optional<Route> findByIdWithBuses(@Param("routeId") Long routeId);

    @Query("SELECT COUNT(b) FROM Bus b WHERE b.rutaAsignada.id = :routeId AND b.activo = true")
    Long countBusesByRouteId(@Param("routeId") Long routeId);
    // Agregar este método al final de RouteRepository.java

    @Query("SELECT r FROM Route r LEFT JOIN FETCH r.buses WHERE r.activo = true AND r.estado = :estado")
    List<Route> findAllActivoTrueAndEstado(@Param("estado") EstadoRuta estado);

    @Query("SELECT r FROM Route r LEFT JOIN FETCH r.buses WHERE r.empresaId = :empresaId AND r.activo = true AND r.estado = :estado")
    List<Route> findByEmpresaIdAndActivoTrueAndEstadoWithBuses(
            @Param("empresaId") Long empresaId,
            @Param("estado") EstadoRuta estado
    );
}