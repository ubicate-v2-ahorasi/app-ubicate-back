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

    boolean existsByPlacaAndActivoTrue(String placa);
    Long countByEmpresaIdAndActivoTrue(Long empresaId);

    @Query("SELECT b FROM Bus b LEFT JOIN FETCH b.rutaAsignada WHERE b.empresaId = :empresaId AND b.activo = true")
    Page<Bus> findByEmpresaIdAndActivoTrueWithRoute(@Param("empresaId") Long empresaId, Pageable pageable);

    @Query("SELECT b FROM Bus b LEFT JOIN FETCH b.rutaAsignada WHERE b.empresaId = :empresaId " +
            "AND (:search IS NULL OR :search = '' OR " +
            "LOWER(b.placa) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(b.modelo) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(b.marca) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "CAST(b.id AS string) LIKE CONCAT('%', :search, '%')) " +
            "AND (:estado IS NULL OR b.estado = :estado) " +
            "AND b.activo = true")
    Page<Bus> searchBusesByEmpresa(
            @Param("empresaId") Long empresaId,
            @Param("search") String search,
            @Param("estado") EstadoBus estado,
            Pageable pageable
    );

    @Query("SELECT b FROM Bus b LEFT JOIN FETCH b.rutaAsignada WHERE b.id = :busId")
    Optional<Bus> findByIdWithRoute(@Param("busId") Long busId);

    @Query("SELECT b FROM Bus b LEFT JOIN FETCH b.rutaAsignada WHERE b.empresaId = :empresaId AND b.estado = :estado AND b.activo = true")
    List<Bus> findByEmpresaIdAndEstadoAndActivoTrueWithRoute(@Param("empresaId") Long empresaId, @Param("estado") EstadoBus estado);

    Page<Bus> findByRutaAsignadaAndActivoTrue(Route ruta, Pageable pageable);

    Long countByEmpresaIdAndActivoTrueAndRutaAsignadaIsNotNull(Long empresaId);
    Long countByEmpresaIdAndActivoTrueAndRutaAsignadaIsNull(Long empresaId);

    @Query("SELECT b FROM Bus b LEFT JOIN FETCH b.rutaAsignada WHERE b.empresaId = :empresaId AND b.activo = true AND b.latitud IS NOT NULL AND b.longitud IS NOT NULL")
    List<Bus> findByEmpresaIdAndActivoTrueAndLatitudIsNotNullAndLongitudIsNotNullWithRoute(@Param("empresaId") Long empresaId);

    @Query("SELECT b.estado, COUNT(b) FROM Bus b WHERE b.empresaId = :empresaId AND b.activo = true GROUP BY b.estado")
    List<Object[]> findBusStatsByEmpresaId(@Param("empresaId") Long empresaId);

    Optional<Bus> findByPlacaAndActivoTrue(String placa);

    @Query("SELECT b FROM Bus b JOIN FETCH b.rutaAsignada r WHERE b.placa = :placa AND b.activo = true")
    Optional<Bus> findByPlacaWithRoute(@Param("placa") String placa);
}