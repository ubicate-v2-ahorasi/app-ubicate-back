package app_jwt.auth_service.modules.conductor.infrastructure.adapter.output.persistence;

import app_jwt.auth_service.modules.conductor.domain.model.Conductor;
import app_jwt.auth_service.modules.conductor.domain.model.CategoriaLicencia;
import app_jwt.auth_service.modules.conductor.domain.model.EstadoConductor;
import app_jwt.auth_service.modules.conductor.domain.model.TurnoConductor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConductorRepository extends JpaRepository<Conductor, Long> {

    Page<Conductor> findByEmpresaIdAndActivoTrue(Long empresaId, Pageable pageable);
    List<Conductor> findByEmpresaIdAndActivoTrue(Long empresaId);
    Long countByEmpresaIdAndActivoTrue(Long empresaId);
    List<Conductor> findByEmpresaIdAndEstadoAndActivoTrue(Long empresaId, EstadoConductor estado);
    Long countByEmpresaIdAndEstadoAndActivoTrue(Long empresaId, EstadoConductor estado);
    List<Conductor> findByEmpresaIdAndTurnoAndActivoTrue(Long empresaId, TurnoConductor turno);
    Long countByEmpresaIdAndBusAsignadoIsNotNullAndActivoTrue(Long empresaId);
    Long countByEmpresaIdAndBusAsignadoIsNullAndActivoTrue(Long empresaId);
    boolean existsByNumeroLicenciaAndActivoTrue(String numeroLicencia);
    boolean existsByUsuarioIdAndActivoTrue(Long usuarioId);
    boolean existsByBusAsignadoIdAndActivoTrue(Long busId);
    Optional<Conductor> findByNumeroLicenciaAndActivoTrue(String numeroLicencia);

    @Query("SELECT c FROM Conductor c LEFT JOIN FETCH c.busAsignado WHERE c.usuario.id = :usuarioId AND c.activo = true ORDER BY c.id DESC")
    List<Conductor> findByUsuarioIdAndActivoTrueOrderByIdDesc(@Param("usuarioId") Long usuarioId);

    @Query("SELECT c FROM Conductor c WHERE c.usuario.id = :usuarioId AND c.activo = true ORDER BY c.id DESC LIMIT 1")
    Optional<Conductor> findByUsuarioIdAndActivoTrue(@Param("usuarioId") Long usuarioId);

    Optional<Conductor> findByBusAsignadoIdAndActivoTrue(Long busId);

    @Query("SELECT COUNT(c) FROM Conductor c WHERE c.empresaId = :empresaId AND c.activo = true AND c.fechaVencimientoLicencia < CURRENT_DATE")
    Long countLicenciasVencidas(@Param("empresaId") Long empresaId);

    @Query("SELECT COUNT(c) FROM Conductor c WHERE c.empresaId = :empresaId AND c.activo = true AND c.fechaVencimientoLicencia BETWEEN CURRENT_DATE AND :fechaLimite")
    Long countLicenciasPorVencer(@Param("empresaId") Long empresaId, @Param("fechaLimite") LocalDate fechaLimite);

    @Query("SELECT c FROM Conductor c LEFT JOIN c.usuario u WHERE c.empresaId = :empresaId " +
            "AND c.activo = true " +
            "AND (:estado IS NULL OR c.estado = :estado) " +
            "AND (:categoria IS NULL OR c.categoriaLicencia = :categoria) " +
            "AND (:searchTerm IS NULL OR LOWER(u.nombre) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.dni) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.numeroLicencia) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Conductor> searchConductores(
            @Param("empresaId") Long empresaId,
            @Param("searchTerm") String searchTerm,
            @Param("estado") EstadoConductor estado,
            @Param("categoria") CategoriaLicencia categoria,
            Pageable pageable);
}



