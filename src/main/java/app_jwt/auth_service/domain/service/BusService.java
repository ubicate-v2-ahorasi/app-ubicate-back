package app_jwt.auth_service.domain.service;

import app_jwt.auth_service.domain.dtos.bus.*;
import app_jwt.auth_service.domain.entity.Bus;
import app_jwt.auth_service.domain.entity.Route;
import app_jwt.auth_service.domain.enums.EstadoBus;
import app_jwt.auth_service.infra.repository.BusRepository;
import app_jwt.auth_service.infra.repository.RouteRepository;
import app_jwt.auth_service.infra.security.SecurityUtils;
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
public class BusService {

    private final BusRepository busRepository;
    private final RouteRepository routeRepository;
    private final SecurityUtils securityUtils;

    @Transactional
    public BusResponse createBus(CreateBusRequest request, Long empresaId) {
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
                .modelo(request.getModelo())
                .capacidad(request.getCapacidad())
                .anio(request.getAnio())
                .color(request.getColor())
                .empresaId(empresaId)
                .rutaAsignada(rutaAsignada)
                .estado(EstadoBus.INACTIVO)
                .activo(true)
                .build();

        return BusResponse.from(busRepository.save(bus));
    }

    @Transactional(readOnly = true)
    public Page<BusResponse> getBusesByEmpresa(Long empresaId, Pageable pageable) {
        return busRepository.findByEmpresaIdAndActivoTrueWithRoute(empresaId, pageable)
                .map(BusResponse::from);
    }

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

    @Transactional(readOnly = true)
    public List<BusResponse> getBusesByEstado(Long empresaId, EstadoBus estado) {
        return busRepository.findByEmpresaIdAndEstadoAndActivoTrueWithRoute(empresaId, estado)
                .stream().map(BusResponse::from).collect(Collectors.toList());
    }

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

    @Transactional
    public BusResponse updateBus(Long busId, UpdateBusRequest request, Long empresaId) {
        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bus no encontrado"));

        securityUtils.validateEmpresaAccess(bus.getEmpresaId(), empresaId, "bus");

        if (!Boolean.TRUE.equals(bus.getActivo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede modificar un bus inactivo");
        }

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

        return BusResponse.from(busRepository.save(bus));
    }

    @Transactional
    public BusResponse asignarRuta(Long busId, Long rutaId, Long empresaId) {
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
        return BusResponse.from(busRepository.save(bus));
    }

    @Transactional
    public BusResponse removerRuta(Long busId, Long empresaId) {
        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bus no encontrado"));

        securityUtils.validateEmpresaAccess(bus.getEmpresaId(), empresaId, "bus");

        if (!Boolean.TRUE.equals(bus.getActivo())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tiene permisos para modificar este bus");
        }

        bus.setRutaAsignada(null);
        return BusResponse.from(busRepository.save(bus));
    }

    @Transactional
    public void deleteBus(Long busId, Long empresaId) {
        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bus no encontrado"));

        securityUtils.validateEmpresaAccess(bus.getEmpresaId(), empresaId, "bus");

        bus.setActivo(false);
        busRepository.save(bus);
    }

    @Transactional
    public BusResponse changeEstadoBus(Long busId, EstadoBus nuevoEstado, Long empresaId) {
        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bus no encontrado"));

        securityUtils.validateEmpresaAccess(bus.getEmpresaId(), empresaId, "bus");

        if (!Boolean.TRUE.equals(bus.getActivo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede cambiar el estado de un bus inactivo");
        }

        bus.setEstado(nuevoEstado);
        return BusResponse.from(busRepository.save(bus));
    }

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

    // Métodos para Firebase
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

        busRepository.save(bus);

        log.debug("Snapshot de ubicación sincronizado para bus {}", busId);
    }
}