package app_jwt.auth_service.modules.bus.application.service;

import app_jwt.auth_service.modules.bus.domain.model.Bus;
import app_jwt.auth_service.modules.bus.domain.model.EstadoBus;
import app_jwt.auth_service.modules.bus.domain.port.input.BusService;
import app_jwt.auth_service.modules.bus.infrastructure.adapter.input.rest.dto.*;
import app_jwt.auth_service.modules.bus.infrastructure.adapter.output.persistence.BusRepository;
import app_jwt.auth_service.modules.route.domain.model.Route;
import app_jwt.auth_service.modules.route.infrastructure.adapter.output.persistence.RouteRepository;
import app_jwt.auth_service.shared.service.RedisRealtimeService;
import app_jwt.auth_service.shared.service.QRCodeService;
import app_jwt.auth_service.shared.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BusServiceImpl implements BusService {

    private final BusRepository busRepository;
    private final RouteRepository routeRepository;
    private final SecurityUtils securityUtils;
    private final RedisRealtimeService redisRealtimeService;


    @Override
    @Transactional
    public BusResponse createBus(CreateBusRequest request, Long empresaId) {
        log.info("Creando nuevo bus para empresa: {}", empresaId);

        if (busRepository.existsByPlacaAndActivoTrue(request.getPlaca())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un bus con la placa: " + request.getPlaca());
        }

        Route rutaAsignada = null;
        if (request.getRutaId() != null) {
            rutaAsignada = routeRepository.findById(request.getRutaId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ruta no encontrada"));

            securityUtils.validateEmpresaAccess(rutaAsignada.getEmpresaId(), empresaId, "ruta");

            if (!Boolean.TRUE.equals(rutaAsignada.getActivo())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ruta no válida para la empresa");
            }
        }

        Bus bus = Bus.builder()
                .placa(request.getPlaca().toUpperCase())
                .marca(request.getMarca())
                .modelo(request.getModelo())
                .capacidad(request.getCapacidad())
                .anio(request.getAnio())
                .color(request.getColor())
                .empresaId(empresaId)
                .rutaAsignada(rutaAsignada)
                .estado(request.getEstado() != null ? request.getEstado() : EstadoBus.ACTIVO)
                .activo(true)
                .build();

        Bus savedBus = busRepository.save(bus);
        log.info("Bus guardado en MySQL con ID: {}", savedBus.getId());

        try {
            redisRealtimeService.upsertBus(savedBus);
            if (rutaAsignada != null) {
                redisRealtimeService.upsertBusToRutaIndex(savedBus);
            }
            log.info("Bus {} registrado en Redis Realtime exitosamente", savedBus.getId());
        } catch (Exception e) {
            log.error("Error al registrar bus en Redis Realtime: {}", e.getMessage());
        }

        return BusResponse.from(savedBus);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BusResponse> getBusesByEmpresa(Long empresaId, Pageable pageable) {
        return busRepository.findByEmpresaIdAndActivoTrueWithRoute(empresaId, pageable)
                .map(BusResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BusResponse> searchBuses(Long empresaId, String search, EstadoBus estado, Pageable pageable) {
        return busRepository.searchBusesByEmpresa(empresaId, search, estado, pageable)
                .map(BusResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BusResponse> getBusesByRuta(Long rutaId, Long empresaId, Pageable pageable) {
        Route ruta = routeRepository.findById(rutaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ruta no encontrada"));

        securityUtils.validateEmpresaAccess(ruta.getEmpresaId(), empresaId, "ruta");

        if (!Boolean.TRUE.equals(ruta.getActivo())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tiene permisos para acceder a esta ruta");
        }

        return busRepository.findByRutaAsignadaAndActivoTrue(ruta, pageable)
                .map(BusResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BusResponse> getBusesByRuta(Long rutaId, Long empresaId) {
        Route ruta = routeRepository.findById(rutaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ruta no encontrada"));

        securityUtils.validateEmpresaAccess(ruta.getEmpresaId(), empresaId, "ruta");

        if (!Boolean.TRUE.equals(ruta.getActivo())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tiene permisos para acceder a esta ruta");
        }

        return busRepository.findByRutaAsignadaAndActivoTrue(ruta, Pageable.unpaged())
                .getContent()
                .stream()
                .map(BusResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BusResponse> getBusesByEstado(Long empresaId, EstadoBus estado) {
        return busRepository.findByEmpresaIdAndEstadoAndActivoTrueWithRoute(empresaId, estado)
                .stream().map(BusResponse::from).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BusResponse getBusById(Long busId, Long empresaId) {
        Bus bus = busRepository.findByIdWithRoute(busId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bus no encontrado"));

        securityUtils.validateEmpresaAccess(bus.getEmpresaId(), empresaId, "bus");

        if (!Boolean.TRUE.equals(bus.getActivo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bus no disponible");
        }

        return BusResponse.from(bus);
    }

    @Override
    @Transactional
    public BusResponse updateBus(Long busId, UpdateBusRequest request, Long empresaId) {
        log.info("Actualizando bus: {}", busId);

        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bus no encontrado"));

        securityUtils.validateEmpresaAccess(bus.getEmpresaId(), empresaId, "bus");

        if (!Boolean.TRUE.equals(bus.getActivo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede modificar un bus inactivo");
        }

        if (request.getMarca() != null) bus.setMarca(request.getMarca());
        if (request.getModelo() != null) bus.setModelo(request.getModelo());
        if (request.getCapacidad() != null) bus.setCapacidad(request.getCapacidad());
        if (request.getAnio() != null) bus.setAnio(request.getAnio());
        if (request.getColor() != null) bus.setColor(request.getColor());
        if (request.getEstado() != null) bus.setEstado(request.getEstado());

        if (request.getRutaId() != null) {
            Route nuevaRuta = routeRepository.findById(request.getRutaId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ruta no encontrada"));

            securityUtils.validateEmpresaAccess(nuevaRuta.getEmpresaId(), empresaId, "ruta");

            if (!Boolean.TRUE.equals(nuevaRuta.getActivo())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ruta no válida para la empresa");
            }
            bus.setRutaAsignada(nuevaRuta);
        }

        Bus updatedBus = busRepository.save(bus);
        log.info("Bus actualizado en MySQL: {}", updatedBus.getId());

        try {
            redisRealtimeService.upsertBus(updatedBus);
            log.info("Bus {} actualizado en Redis Realtime exitosamente", updatedBus.getId());
        } catch (Exception e) {
            log.error("Error al actualizar bus en Redis Realtime: {}", e.getMessage());
        }

        return BusResponse.from(updatedBus);
    }

    @Override
    @Transactional
    public BusResponse asignarRuta(Long busId, Long rutaId, Long empresaId) {
        log.info("Asignando ruta {} al bus: {}", rutaId, busId);

        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bus no encontrado"));

        securityUtils.validateEmpresaAccess(bus.getEmpresaId(), empresaId, "bus");

        if (!Boolean.TRUE.equals(bus.getActivo())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tiene permisos para modificar este bus");
        }

        Route ruta = routeRepository.findById(rutaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ruta no encontrada"));

        securityUtils.validateEmpresaAccess(ruta.getEmpresaId(), empresaId, "ruta");

        if (!Boolean.TRUE.equals(ruta.getActivo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ruta no válida para la empresa");
        }

        bus.setRutaAsignada(ruta);
        Bus updatedBus = busRepository.save(bus);
        log.info("Ruta asignada en MySQL: Bus {} -> Ruta {}", updatedBus.getId(), rutaId);

        try {
            redisRealtimeService.upsertBus(updatedBus);
            if (ruta != null) {
                redisRealtimeService.upsertBusToRutaIndex(updatedBus);
            }
            log.info("Bus {} con ruta {} actualizado en Redis Realtime exitosamente", updatedBus.getId(), rutaId);
        } catch (Exception e) {
            log.error("Error al actualizar ruta del bus en Redis Realtime: {}", e.getMessage());
        }

        return BusResponse.from(updatedBus);
    }

    @Override
    @Transactional
    public BusResponse removerRuta(Long busId, Long empresaId) {
        log.info("Removiendo ruta del bus: {}", busId);

        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bus no encontrado"));

        securityUtils.validateEmpresaAccess(bus.getEmpresaId(), empresaId, "bus");

        if (!Boolean.TRUE.equals(bus.getActivo())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tiene permisos para modificar este bus");
        }

        Long rutaAnteriorId = bus.getRutaAsignada() != null ? bus.getRutaAsignada().getId() : null;
        bus.setRutaAsignada(null);
        Bus updatedBus = busRepository.save(bus);
        log.info("Ruta removida en MySQL del bus: {}", updatedBus.getId());

        try {
            if (rutaAnteriorId != null) {
                redisRealtimeService.removeBusFromRutaIndex(rutaAnteriorId, busId);
            }
            redisRealtimeService.upsertBus(updatedBus);
            log.info("Ruta removida del bus {} en Redis Realtime exitosamente", updatedBus.getId());
        } catch (Exception e) {
            log.error("Error al remover ruta del bus en Redis Realtime: {}", e.getMessage());
        }

        return BusResponse.from(updatedBus);
    }

    @Override
    @Transactional
    public void deleteBus(Long busId, Long empresaId) {
        log.info("Eliminando bus: {}", busId);

        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bus no encontrado"));

        securityUtils.validateEmpresaAccess(bus.getEmpresaId(), empresaId, "bus");

        Long rutaAnteriorId = bus.getRutaAsignada() != null ? bus.getRutaAsignada().getId() : null;
        bus.setActivo(false);
        Bus deletedBus = busRepository.save(bus);
        log.info("Bus eliminado en MySQL: {}", deletedBus.getId());

        try {
            if (rutaAnteriorId != null) {
                redisRealtimeService.removeBusFromRutaIndex(rutaAnteriorId, busId);
            }
            redisRealtimeService.removeBus(empresaId, busId);
            log.info("Bus {} desactivado/eliminado de Redis Realtime exitosamente", busId);
        } catch (Exception e) {
            log.error("Error al desactivar bus en Redis Realtime: {}", e.getMessage());
        }
    }

    @Override
    @Transactional
    public BusResponse changeEstadoBus(Long busId, EstadoBus nuevoEstado, Long empresaId) {
        log.info("Cambiando estado del bus {} a: {}", busId, nuevoEstado);

        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bus no encontrado"));

        securityUtils.validateEmpresaAccess(bus.getEmpresaId(), empresaId, "bus");

        if (!Boolean.TRUE.equals(bus.getActivo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede cambiar el estado de un bus inactivo");
        }

        bus.setEstado(nuevoEstado);
        Bus updatedBus = busRepository.save(bus);
        log.info("Estado del bus actualizado en MySQL: {} -> {}", updatedBus.getId(), nuevoEstado);

        try {
            redisRealtimeService.upsertBus(updatedBus);
            log.info("Estado del bus {} actualizado a {} en Redis Realtime exitosamente", updatedBus.getId(), nuevoEstado);
        } catch (Exception e) {
            log.error("Error al actualizar estado del bus en Redis Realtime: {}", e.getMessage());
        }

        return BusResponse.from(updatedBus);
    }

    @Override
    @Transactional(readOnly = true)
    public BusStatsResponse getBusStats(Long empresaId) {
        List<Object[]> estadisticas = busRepository.findBusStatsByEmpresaId(empresaId);
        Long totalBuses = busRepository.countByEmpresaIdAndActivoTrue(empresaId);
        Long busesConRuta = busRepository.countByEmpresaIdAndActivoTrueAndRutaAsignadaIsNotNull(empresaId);
        Long busesSinRuta = busRepository.countByEmpresaIdAndActivoTrueAndRutaAsignadaIsNull(empresaId);

        Map<String, Long> estadoPorCantidad = new HashMap<>();
        Long activos = 0L, inactivos = 0L, enRuta = 0L, enMantenimiento = 0L;

        for (Object[] stat : estadisticas) {
            EstadoBus estado = (EstadoBus) stat[0];
            Long cantidad = (Long) stat[1];
            estadoPorCantidad.put(estado.name(), cantidad);

            switch (estado) {
                case ACTIVO -> activos = cantidad;
                case INACTIVO -> inactivos = cantidad;
                case EN_RUTA -> enRuta = cantidad;
                case MANTENIMIENTO -> enMantenimiento = cantidad;
            }
        }

        return BusStatsResponse.builder()
                .totalBuses(totalBuses)
                .busesActivos(activos)
                .busesInactivos(inactivos)
                .busesEnRuta(enRuta)
                .busesEnMantenimiento(enMantenimiento)
                .estadoPorCantidad(estadoPorCantidad)
                .busesConRuta(busesConRuta)
                .busesSinRuta(busesSinRuta)
                .conectados(0L)
                .enMovimiento(0L)
                .detenidos(0L)
                .sinConexion(0L)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Bus getBusEntity(Long busId, Long empresaId) {
        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Bus no encontrado"
                ));

        securityUtils.validateEmpresaAccess(bus.getEmpresaId(), empresaId, "bus");

        if (!Boolean.TRUE.equals(bus.getActivo())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Bus no disponible"
            );
        }

        return bus;
    }

    @Override
    @Transactional
    public void syncLocationSnapshot(Long busId, Double latitud, Double longitud, Double velocidad) {
        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Bus no encontrado"
                ));

        bus.setLatitud(latitud);
        bus.setLongitud(longitud);
        bus.setVelocidad(velocidad);
        bus.setUltimaUbicacion(LocalDateTime.now());

        Bus updatedBus = busRepository.save(bus);

        try {
            redisRealtimeService.upsertBus(updatedBus);
            if (updatedBus.getRutaAsignada() != null) {
                redisRealtimeService.upsertBusToRutaIndex(updatedBus);
            }
            log.debug("Ubicación del bus {} actualizada en Redis Realtime", busId);
        } catch (Exception e) {
            log.error("Error al actualizar ubicación del bus en Redis Realtime: {}", e.getMessage());
        }

        log.debug("Snapshot de ubicación sincronizado para bus {}", busId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BusUbicacionResponse> getUbicacionesBusesPorRuta(Long rutaId, Long empresaId) {
        Route ruta = routeRepository.findById(rutaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ruta no encontrada"));

        securityUtils.validateEmpresaAccess(ruta.getEmpresaId(), empresaId, "ruta");

        List<Bus> buses = busRepository.findByRutaAsignadaAndActivoTrue(ruta);

        return buses.stream()
                .filter(b -> b.getLatitud() != null && b.getLongitud() != null)
                .map(bus -> BusUbicacionResponse.builder()
                        .busId(bus.getId())
                        .placa(bus.getPlaca())
                        .latitud(bus.getLatitud())
                        .longitud(bus.getLongitud())
                        .velocidad(bus.getVelocidad())
                        .estado(bus.getEstado() != null ? bus.getEstado().name() : null)
                        .estadoSenal(bus.getEstadoSenal())
                        .ultimaUbicacion(bus.getUltimaUbicacion())
                        .rutaId(ruta.getId())
                        .nombreRuta(ruta.getNombre())
                        .build())
                .collect(Collectors.toList());
    }
}
