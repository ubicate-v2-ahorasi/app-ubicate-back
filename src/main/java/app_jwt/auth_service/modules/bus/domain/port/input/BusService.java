package app_jwt.auth_service.modules.bus.domain.port.input;

import app_jwt.auth_service.modules.bus.domain.model.Bus;
import app_jwt.auth_service.modules.bus.domain.model.EstadoBus;
import app_jwt.auth_service.modules.bus.infrastructure.adapter.input.rest.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BusService {
    BusResponse createBus(CreateBusRequest request, Long empresaId);

    Page<BusResponse> getBusesByEmpresa(Long empresaId, Pageable pageable);

    Page<BusResponse> searchBuses(Long empresaId, String search, EstadoBus estado, Pageable pageable);

    Page<BusResponse> getBusesByRuta(Long rutaId, Long empresaId, Pageable pageable);

    List<BusResponse> getBusesByRuta(Long rutaId, Long empresaId);

    List<BusResponse> getBusesByEstado(Long empresaId, EstadoBus estado);

    BusResponse getBusById(Long busId, Long empresaId);

    BusResponse updateBus(Long busId, UpdateBusRequest request, Long empresaId);

    BusResponse asignarRuta(Long busId, Long rutaId, Long empresaId);

    BusResponse removerRuta(Long busId, Long empresaId);

    void deleteBus(Long busId, Long empresaId);

    BusResponse changeEstadoBus(Long busId, EstadoBus nuevoEstado, Long empresaId);

    BusStatsResponse getBusStats(Long empresaId);

    Bus getBusEntity(Long busId, Long empresaId);

    void syncLocationSnapshot(Long busId, Double latitud, Double longitud, Double velocidad);
}
