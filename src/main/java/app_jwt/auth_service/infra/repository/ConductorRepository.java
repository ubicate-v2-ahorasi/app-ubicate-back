package app_jwt.auth_service.infra.repository;

import app_jwt.auth_service.domain.entity.Conductor;
import app_jwt.auth_service.domain.enums.EstadoConductor;
import app_jwt.auth_service.domain.enums.TurnoConductor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ConductorRepository extends JpaRepository<Conductor, Long> {

    // Básicos
    Page<Conductor> findByEmpresaIdAndActivoTrue(Long empresaId, Pageable pageable);
    List<Conductor> findByEmpresaIdAndActivoTrue(Long empresaId);
    Long countByEmpresaIdAndActivoTrue(Long empresaId);

    // Por estado
    List<Conductor> findByEmpresaIdAndEstadoAndActivoTrue(Long empresaId, EstadoConductor estado);
    Long countByEmpresaIdAndEstadoAndActivoTrue(Long empresaId, EstadoConductor estado);

    // Por turno
    List<Conductor> findByEmpresaIdAndTurnoAndActivoTrue(Long empresaId, TurnoConductor turno);

    // Buses
    Long countByEmpresaIdAndBusAsignadoIsNotNullAndActivoTrue(Long empresaId);
    Long countByEmpresaIdAndBusAsignadoIsNullAndActivoTrue(Long empresaId);

    // Validaciones
    boolean existsByNumeroLicenciaAndActivoTrue(String numeroLicencia);

    // Licencias vencidas
    @Query("SELECT COUNT(c) FROM Conductor c WHERE c.empresaId = :empresaId AND c.activo = true AND c.fechaVencimientoLicencia < CURRENT_DATE")
    Long countLicenciasVencidas(@Param("empresaId") Long empresaId);

    // Licencias por vencer (30 días)
    @Query("SELECT COUNT(c) FROM Conductor c WHERE c.empresaId = :empresaId AND c.activo = true AND c.fechaVencimientoLicencia BETWEEN CURRENT_DATE AND :fechaLimite")
    Long countLicenciasPorVencer(@Param("empresaId") Long empresaId, @Param("fechaLimite") LocalDate fechaLimite);

    // Búsqueda simple
    @Query("SELECT c FROM Conductor c JOIN c.usuario u WHERE c.empresaId = :empresaId AND c.activo = true AND " +
            "(:searchTerm IS NULL OR LOWER(u.nombre) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.apellido) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.dni) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.numeroLicencia) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Conductor> searchConductores(@Param("empresaId") Long empresaId, @Param("searchTerm") String searchTerm, Pageable pageable);
}