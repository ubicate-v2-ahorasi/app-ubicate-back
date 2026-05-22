package app_jwt.auth_service.modules.bus.infrastructure.adapter.output.persistence;

import app_jwt.auth_service.modules.bus.infrastructure.adapter.output.persistence.entity.NotificacionSenal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacionSenalRepository extends JpaRepository<NotificacionSenal, Long> {

    List<NotificacionSenal> findByEmpresaIdAndLeidaFalseOrderByTimestampDesc(Long empresaId);

    Page<NotificacionSenal> findByEmpresaIdOrderByTimestampDesc(Long empresaId, Pageable pageable);

    Long countByEmpresaIdAndLeidaFalse(Long empresaId);

    @Modifying
    @Query("UPDATE NotificacionSenal n SET n.leida = true WHERE n.id = :id AND n.empresaId = :empresaId")
    int markAsRead(@Param("id") Long id, @Param("empresaId") Long empresaId);

    @Modifying
    @Query("UPDATE NotificacionSenal n SET n.leida = true WHERE n.empresaId = :empresaId AND n.leida = false")
    int markAllAsRead(@Param("empresaId") Long empresaId);
}