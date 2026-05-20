package app_jwt.auth_service.modules.bus.infrastructure.adapter.output.persistence;

import app_jwt.auth_service.modules.bus.infrastructure.adapter.output.persistence.entity.BusLocationHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BusLocationHistoryRepository extends JpaRepository<BusLocationHistory, Long> {

    Page<BusLocationHistory> findByBusIdOrderByTimestampDesc(Long busId, Pageable pageable);

    @Query("SELECT h FROM BusLocationHistory h WHERE h.busId = :busId AND h.timestamp BETWEEN :start AND :end ORDER BY h.timestamp DESC")
    List<BusLocationHistory> findByBusIdAndTimestampBetween(
            @Param("busId") Long busId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT h FROM BusLocationHistory h WHERE h.empresaId = :empresaId AND h.timestamp BETWEEN :start AND :end ORDER BY h.timestamp DESC")
    List<BusLocationHistory> findByEmpresaIdAndTimestampBetween(
            @Param("empresaId") Long empresaId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}