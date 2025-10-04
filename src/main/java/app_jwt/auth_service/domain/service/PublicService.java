package app_jwt.auth_service.domain.service;

import app_jwt.auth_service.domain.dtos.bus.BusLocationResponse;
import app_jwt.auth_service.domain.dtos.empresa.EmpresaPublicResponse;
import app_jwt.auth_service.domain.dtos.route.RouteResponse;
import app_jwt.auth_service.domain.entity.Bus;
import app_jwt.auth_service.domain.entity.Route;
import app_jwt.auth_service.domain.enums.EstadoBus;
import app_jwt.auth_service.domain.enums.EstadoRuta;
import app_jwt.auth_service.infra.repository.BusRepository;
import app_jwt.auth_service.infra.repository.EmpresaRepository;
import app_jwt.auth_service.infra.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PublicService {

    private final BusRepository busRepository;
    private final RouteRepository routeRepository;
    private final EmpresaRepository empresaRepository;

    @Transactional(readOnly = true)
    public List<EmpresaPublicResponse> getAllEmpresas() {
        return empresaRepository.findByActivoTrue()
                .stream()
                .map(EmpresaPublicResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RouteResponse> getRutasByEmpresa(Long empresaId) {
        if (!empresaRepository.existsByIdAndActivoTrue(empresaId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa no encontrada");
        }

        return routeRepository
                .findByEmpresaIdAndActivoTrueAndEstadoWithBuses(empresaId, EstadoRuta.ACTIVA)
                .stream()
                .map(RouteResponse::from)
                .collect(Collectors.toList());
    }

    // OPTIMIZADO: Devuelve DTO ligero con solo datos de ubicación
    @Transactional(readOnly = true)
    public List<BusLocationResponse> getBusesActivosByEmpresa(Long empresaId) {
        if (!empresaRepository.existsByIdAndActivoTrue(empresaId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa no encontrada");
        }

        return busRepository
                .findByEmpresaIdAndActivoTrueAndLatitudIsNotNullAndLongitudIsNotNullWithRoute(empresaId)
                .stream()
                .filter(b -> b.getEstado() == EstadoBus.EN_RUTA || b.getEstado() == EstadoBus.ACTIVO)
                .map(BusLocationResponse::from)
                .collect(Collectors.toList());
    }

    // OPTIMIZADO: Endpoint con filtro de tiempo para obtener solo actualizaciones recientes
    @Transactional(readOnly = true)
    public List<BusLocationResponse> getBusesUpdatedSince(Long empresaId, LocalDateTime since) {
        if (!empresaRepository.existsByIdAndActivoTrue(empresaId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa no encontrada");
        }

        return busRepository
                .findByEmpresaIdAndActivoTrueAndLatitudIsNotNullAndLongitudIsNotNullWithRoute(empresaId)
                .stream()
                .filter(b -> b.getEstado() == EstadoBus.EN_RUTA || b.getEstado() == EstadoBus.ACTIVO)
                .filter(b -> b.getUltimaUbicacion() != null && b.getUltimaUbicacion().isAfter(since))
                .map(BusLocationResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BusLocationResponse getBusUbicacion(Long busId) {
        Bus bus = busRepository.findByIdWithRoute(busId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Bus no encontrado"));

        if (!Boolean.TRUE.equals(bus.getActivo())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Bus no disponible");
        }

        return BusLocationResponse.from(bus);
    }

    @Transactional(readOnly = true)
    public RouteResponse getRutaById(Long rutaId) {
        Route ruta = routeRepository.findByIdWithBuses(rutaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Ruta no encontrada"));

        if (!Boolean.TRUE.equals(ruta.getActivo()) || ruta.getEstado() != EstadoRuta.ACTIVA) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Ruta no disponible");
        }

        return RouteResponse.from(ruta);
    }
}