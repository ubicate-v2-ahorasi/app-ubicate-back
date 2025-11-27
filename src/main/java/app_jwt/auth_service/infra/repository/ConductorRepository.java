package app_jwt.auth_service.infra.repository;

import app_jwt.auth_service.domain.entity.Conductor;
import app_jwt.auth_service.domain.enums.CategoriaLicencia;
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
import java.util.Optional;

@Repository
public interface ConductorRepository extends JpaRepository<Conductor, Long> {

    // Método para obtener todos los conductores activos de una empresa con paginación
    Page<Conductor> findByEmpresaIdAndActivoTrue(Long empresaId, Pageable pageable);

    // Método para obtener todos los conductores activos de una empresa sin paginación
    List<Conductor> findByEmpresaIdAndActivoTrue(Long empresaId);

    // Método para contar conductores activos de una empresa
    Long countByEmpresaIdAndActivoTrue(Long empresaId);

    // Método para filtrar por estado (activo, inactivo, etc.) y empresa
    List<Conductor> findByEmpresaIdAndEstadoAndActivoTrue(Long empresaId, EstadoConductor estado);

    // Método para contar conductores activos por estado
    Long countByEmpresaIdAndEstadoAndActivoTrue(Long empresaId, EstadoConductor estado);

    // Método para filtrar por turno de conductor y empresa
    List<Conductor> findByEmpresaIdAndTurnoAndActivoTrue(Long empresaId, TurnoConductor turno);

    // Método para contar conductores asignados a un bus
    Long countByEmpresaIdAndBusAsignadoIsNotNullAndActivoTrue(Long empresaId);

    // Método para contar conductores no asignados a un bus
    Long countByEmpresaIdAndBusAsignadoIsNullAndActivoTrue(Long empresaId);

    // Método para verificar si un conductor existe con el número de licencia
    boolean existsByNumeroLicenciaAndActivoTrue(String numeroLicencia);

    // Método para verificar si un conductor existe por el ID de usuario
    boolean existsByUsuarioIdAndActivoTrue(Long usuarioId);

    // Método para verificar si un bus está asignado a un conductor
    boolean existsByBusAsignadoIdAndActivoTrue(Long busId);

    // Método para obtener conductores por usuario y ordenarlos por ID en orden descendente
    @Query("SELECT c FROM Conductor c LEFT JOIN FETCH c.busAsignado WHERE c.usuario.id = :usuarioId AND c.activo = true ORDER BY c.id DESC")
    List<Conductor> findByUsuarioIdAndActivoTrueOrderByIdDesc(@Param("usuarioId") Long usuarioId);

    // Método para obtener un conductor por usuario, activo, ordenado por ID
    @Query("SELECT c FROM Conductor c WHERE c.usuario.id = :usuarioId AND c.activo = true ORDER BY c.id DESC LIMIT 1")
    Optional<Conductor> findByUsuarioIdAndActivoTrue(@Param("usuarioId") Long usuarioId);

    // Método para obtener un conductor por bus asignado
    Optional<Conductor> findByBusAsignadoIdAndActivoTrue(Long busId);

    // Método para contar conductores con licencia vencida
    @Query("SELECT COUNT(c) FROM Conductor c WHERE c.empresaId = :empresaId AND c.activo = true AND c.fechaVencimientoLicencia < CURRENT_DATE")
    Long countLicenciasVencidas(@Param("empresaId") Long empresaId);

    // Método para contar conductores con licencia por vencer
    @Query("SELECT COUNT(c) FROM Conductor c WHERE c.empresaId = :empresaId AND c.activo = true AND c.fechaVencimientoLicencia BETWEEN CURRENT_DATE AND :fechaLimite")
    Long countLicenciasPorVencer(@Param("empresaId") Long empresaId, @Param("fechaLimite") LocalDate fechaLimite);

    @Query("SELECT c FROM Conductor c LEFT JOIN c.usuario u WHERE c.empresaId = :empresaId " +
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


    @Query("SELECT c FROM Conductor c LEFT JOIN c.usuario u WHERE c.empresaId = :empresaId " +
            "AND (:estado IS NULL OR c.estado = :estado) " +
            "AND (:categoria IS NULL OR c.categoriaLicencia = :categoria) " +
            "AND (:searchTerm IS NULL OR LOWER(u.nombre) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Conductor> findByEmpresaIdAndEstadoAndCategoriaAndSearchTerm(
            @Param("empresaId") Long empresaId,
            @Param("estado") EstadoConductor estado,
            @Param("categoria") CategoriaLicencia categoria,
            @Param("searchTerm") String searchTerm,
            Pageable pageable);

}
