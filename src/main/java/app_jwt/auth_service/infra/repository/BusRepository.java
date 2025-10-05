package app_jwt.auth_service.infra.repository;

import app_jwt.auth_service.domain.entity.Bus;
import app_jwt.auth_service.domain.entity.Route;
import app_jwt.auth_service.domain.enums.EstadoBus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BusRepository extends JpaRepository<Bus, Long> {

    Optional<Bus> findByPlaca(String placa);
    boolean existsByPlacaAndActivoTrue(String placa);

    Page<Bus> findByEmpresaIdAndActivoTrue(Long empresaId, Pageable pageable);
    List<Bus> findByEmpresaIdAndActivoTrue(Long empresaId);
    Long countByEmpresaIdAndActivoTrue(Long empresaId);

    @Query("SELECT b FROM Bus b LEFT JOIN FETCH b.rutaAsignada WHERE b.empresaId = :empresaId AND b.activo = true")
    Page<Bus> findByEmpresaIdAndActivoTrueWithRoute(@Param("empresaId") Long empresaId, Pageable pageable);

    @Query("SELECT b FROM Bus b LEFT JOIN FETCH b.rutaAsignada WHERE b.id = :busId")
    Optional<Bus> findByIdWithRoute(@Param("busId") Long busId);

    List<Bus> findByEmpresaIdAndEstadoAndActivoTrue(Long empresaId, EstadoBus estado);

    @Query("SELECT b FROM Bus b LEFT JOIN FETCH b.rutaAsignada WHERE b.empresaId = :empresaId AND b.estado = :estado AND b.activo = true")
    List<Bus> findByEmpresaIdAndEstadoAndActivoTrueWithRoute(@Param("empresaId") Long empresaId, @Param("estado") EstadoBus estado);

    Page<Bus> findByRutaAsignadaAndActivoTrue(Route ruta, Pageable pageable);
    List<Bus> findByRutaAsignadaAndActivoTrue(Route ruta);

    List<Bus> findByEmpresaIdAndActivoTrueAndRutaAsignadaIsNull(Long empresaId);
    Long countByEmpresaIdAndActivoTrueAndRutaAsignadaIsNotNull(Long empresaId);
    Long countByEmpresaIdAndActivoTrueAndRutaAsignadaIsNull(Long empresaId);

    List<Bus> findByEmpresaIdAndActivoTrueAndLatitudIsNotNullAndLongitudIsNotNull(Long empresaId);

    @Query("SELECT b FROM Bus b LEFT JOIN FETCH b.rutaAsignada WHERE b.empresaId = :empresaId AND b.activo = true AND b.latitud IS NOT NULL AND b.longitud IS NOT NULL")
    List<Bus> findByEmpresaIdAndActivoTrueAndLatitudIsNotNullAndLongitudIsNotNullWithRoute(@Param("empresaId") Long empresaId);

    @Query("SELECT b.estado, COUNT(b) FROM Bus b WHERE b.empresaId = :empresaId AND b.activo = true GROUP BY b.estado")
    List<Object[]> findBusStatsByEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT b FROM Bus b LEFT JOIN FETCH b.rutaAsignada WHERE b.activo = true AND b.latitud IS NOT NULL AND b.longitud IS NOT NULL")
    List<Bus> findAllActivoTrueAndLatitudIsNotNullWithRoute();


    Optional<Bus> findByPlacaAndActivoTrue(String placa);

    @Query("SELECT b FROM Bus b JOIN FETCH b.rutaAsignada r WHERE b.placa = :placa AND b.activo = true")
    Optional<Bus> findByPlacaWithRoute(@Param("placa") String placa);
}