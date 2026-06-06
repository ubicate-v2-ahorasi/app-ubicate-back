package app_jwt.auth_service.modules.conductor.application.service;

import app_jwt.auth_service.shared.domain.model.Empresa;
import app_jwt.auth_service.shared.domain.model.Usuario;
import app_jwt.auth_service.modules.conductor.domain.model.CategoriaLicencia;
import app_jwt.auth_service.modules.conductor.domain.model.EstadoConductor;
import app_jwt.auth_service.shared.enums.Role;
import app_jwt.auth_service.shared.infrastructure.persistence.EmpresaRepository;
import app_jwt.auth_service.shared.infrastructure.persistence.UsuarioRepository;
import app_jwt.auth_service.modules.bus.domain.model.Bus;
import app_jwt.auth_service.modules.bus.infrastructure.adapter.output.persistence.BusRepository;
import app_jwt.auth_service.modules.conductor.domain.model.Conductor;
import app_jwt.auth_service.modules.conductor.domain.port.input.ConductorService;
import app_jwt.auth_service.modules.conductor.infrastructure.adapter.input.rest.dto.*;
import app_jwt.auth_service.modules.conductor.infrastructure.adapter.output.persistence.ConductorRepository;
import app_jwt.auth_service.modules.route.domain.model.Route;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConductorServiceImpl implements ConductorService {

    private final ConductorRepository conductorRepository;
    private final UsuarioRepository usuarioRepository;
    private final BusRepository busRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmpresaRepository empresaRepository;

    @Override
    @Transactional
    public ConductorCreatedResponse createConductor(CreateConductorRequest request, Long empresaId) {
        if (empresaId == null) throw new RuntimeException("ID de empresa es requerido");

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));
        if (!empresa.getActivo()) throw new RuntimeException("La empresa no está activa");

        String email    = request.getEmail().toLowerCase(Locale.ROOT).trim();
        String telefono = request.getTelefono().toLowerCase(Locale.ROOT).trim();
        String dni      = request.getDni().toLowerCase(Locale.ROOT).trim();

        if (usuarioRepository.findByCorreo(email).isPresent())
            throw new RuntimeException("El email ya está registrado");
        if (conductorRepository.existsByNumeroLicenciaAndActivoTrue(request.getNumeroLicencia()))
            throw new RuntimeException("Ya existe un conductor con ese número de licencia");
        if (usuarioRepository.findByTelefono(telefono).isPresent())
            throw new RuntimeException("Ya existe un conductor con ese número de telefono");
        if (usuarioRepository.findByDni(dni).isPresent())
            throw new RuntimeException("Ya existe un conductor con ese DNI");

        String username        = generateUsername(email);
        String tempPasswordRaw = generatePasswordFromDniOrRandom(request.getDni());
        String encodedPassword = passwordEncoder.encode(tempPasswordRaw);

        Usuario usuario = Usuario.builder()
                .username(username).correo(email)
                .nombre(request.getNombre().trim()).apellido(request.getApellido().trim())
                .telefono(request.getTelefono().trim()).dni(request.getDni())
                .password(encodedPassword).role(Role.CHOFER).empresaId(empresaId)
                .build();
        Usuario savedUsuario = usuarioRepository.save(usuario);

        if (conductorRepository.existsByUsuarioIdAndActivoTrue(savedUsuario.getId()))
            throw new RuntimeException("El usuario ya es conductor");

        Bus busAsignado = null;
        if (request.getBusAsignadoId() != null) {
            busAsignado = busRepository.findById(request.getBusAsignadoId())
                    .orElseThrow(() -> new RuntimeException("Bus no encontrado"));
            if (!busAsignado.getEmpresaId().equals(empresaId))
                throw new RuntimeException("El bus no pertenece a esta empresa");
            if (conductorRepository.existsByBusAsignadoIdAndActivoTrue(request.getBusAsignadoId()))
                throw new RuntimeException("El bus ya está asignado a otro conductor");
        }

        Conductor conductor = Conductor.builder()
                .usuario(savedUsuario)
                .numeroLicencia(request.getNumeroLicencia().toUpperCase(Locale.ROOT).trim())
                .categoriaLicencia(request.getCategoriaLicencia())
                .fechaVencimientoLicencia(request.getFechaVencimientoLicencia())
                .estado(EstadoConductor.ACTIVO)
                .busAsignado(busAsignado)
                .empresaId(empresaId)
                .fechaIngreso(LocalDate.now())
                .activo(true)
                .build();

        Conductor saved = conductorRepository.save(conductor);
        ConductorResponse conductorResponse = ConductorResponse.fromWithEmpresa(saved, empresa.getNombre());
        return ConductorCreatedResponse.builder()
                .conductor(conductorResponse).username(username).tempPassword(tempPasswordRaw).build();
    }

    @Override @Transactional(readOnly = true)
    public Page<ConductorResponse> getConductores(Long empresaId, String searchTerm, EstadoConductor estado, CategoriaLicencia categoria, Pageable pageable) {
        return conductorRepository.searchConductores(empresaId, searchTerm, estado, categoria, pageable)
                .map(ConductorResponse::from);
    }

    @Override @Transactional(readOnly = true)
    public Page<ConductorResponse> searchConductores(Long empresaId, String searchTerm, Pageable pageable) {
        return conductorRepository.searchConductores(empresaId, searchTerm, null, null, pageable)
                .map(ConductorResponse::from);
    }

    @Override @Transactional(readOnly = true)
    public ConductorResponse getConductorById(Long conductorId, Long empresaId) {
        Conductor c = conductorRepository.findById(conductorId)
                .orElseThrow(() -> new RuntimeException("Conductor no encontrado"));
        if (!c.getEmpresaId().equals(empresaId)) throw new RuntimeException("No tiene permisos");
        if (!c.getActivo()) throw new RuntimeException("Conductor no disponible");
        Empresa empresa = empresaRepository.findById(empresaId).orElseThrow();
        return ConductorResponse.fromWithEmpresa(c, empresa.getNombre());
    }

    @Override @Transactional
    public ConductorResponse updateConductor(Long conductorId, UpdateConductorRequest request, Long empresaId) {
        Conductor c = conductorRepository.findById(conductorId)
                .orElseThrow(() -> new RuntimeException("Conductor no encontrado"));
        if (!c.getEmpresaId().equals(empresaId)) throw new RuntimeException("No tiene permisos");
        if (!c.getActivo()) throw new RuntimeException("No puede modificar este conductor");

        if (request.getTelefono() != null) c.getUsuario().setTelefono(request.getTelefono().trim());
        if (request.getFechaVencimientoLicencia() != null) c.setFechaVencimientoLicencia(request.getFechaVencimientoLicencia());
        if (request.getEstado() != null) c.setEstado(request.getEstado());
        if (request.getBusAsignadoId() != null) {
            Bus bus = busRepository.findById(request.getBusAsignadoId())
                    .orElseThrow(() -> new RuntimeException("Bus no encontrado"));
            if (!bus.getEmpresaId().equals(empresaId)) throw new RuntimeException("El bus no pertenece a esta empresa");
            c.setBusAsignado(bus);
        }
        Conductor updated = conductorRepository.save(c);
        Empresa empresa = empresaRepository.findById(empresaId).orElseThrow();
        return ConductorResponse.fromWithEmpresa(updated, empresa.getNombre());
    }

    @Override
    @Transactional
    public void changePassword(Long conductorId, String newPassword, Long empresaId) {
        Conductor c = conductorRepository.findById(conductorId)
                .orElseThrow(() -> new RuntimeException("Conductor no encontrado"));
        if (!c.getEmpresaId().equals(empresaId)) throw new RuntimeException("No tiene permisos");
        if (!c.getActivo()) throw new RuntimeException("Conductor no disponible");

        Usuario usuario = c.getUsuario();
        usuario.setPassword(passwordEncoder.encode(newPassword));
        usuarioRepository.save(usuario);
    }

    @Override @Transactional
    public void deleteConductor(Long conductorId, Long empresaId) {
        Conductor c = conductorRepository.findById(conductorId)
                .orElseThrow(() -> new RuntimeException("Conductor no encontrado"));
        if (!c.getEmpresaId().equals(empresaId)) throw new RuntimeException("No tiene permisos");
        c.setActivo(false);
        conductorRepository.save(c);
    }

    @Override @Transactional
    public ConductorResponse cambiarEstado(Long conductorId, EstadoConductor estado, Long empresaId) {
        Conductor c = conductorRepository.findById(conductorId)
                .orElseThrow(() -> new RuntimeException("Conductor no encontrado"));
        if (!c.getEmpresaId().equals(empresaId) || !c.getActivo()) throw new RuntimeException("No tiene permisos");
        c.setEstado(estado);
        Conductor updated = conductorRepository.save(c);
        Empresa empresa = empresaRepository.findById(empresaId).orElseThrow();
        return ConductorResponse.fromWithEmpresa(updated, empresa.getNombre());
    }

    @Override @Transactional
    public ConductorResponse asignarBus(Long conductorId, Long busId, Long empresaId) {
        Conductor c = conductorRepository.findById(conductorId)
                .orElseThrow(() -> new RuntimeException("Conductor no encontrado"));
        if (!c.getEmpresaId().equals(empresaId) || !c.getActivo()) throw new RuntimeException("No tiene permisos");

        Bus bus = busRepository.findById(busId).orElseThrow(() -> new RuntimeException("Bus no encontrado"));
        if (!bus.getEmpresaId().equals(empresaId)) throw new RuntimeException("El bus no pertenece a esta empresa");

        Optional<Conductor> conductorConBus = conductorRepository.findByBusAsignadoIdAndActivoTrue(busId);
        if (conductorConBus.isPresent() && !conductorConBus.get().getId().equals(conductorId))
            throw new RuntimeException("El bus ya está asignado a otro conductor");
        if (c.getBusAsignado() != null && !c.getBusAsignado().getId().equals(busId))
            throw new RuntimeException("El conductor ya tiene un bus asignado. Debe removerlo primero");

        c.setBusAsignado(bus);
        Conductor updated = conductorRepository.save(c);
        Empresa empresa = empresaRepository.findById(empresaId).orElseThrow();
        return ConductorResponse.fromWithEmpresa(updated, empresa.getNombre());
    }

    @Override @Transactional
    public ConductorResponse removerBus(Long conductorId, Long empresaId) {
        Conductor c = conductorRepository.findById(conductorId)
                .orElseThrow(() -> new RuntimeException("Conductor no encontrado"));
        if (!c.getEmpresaId().equals(empresaId) || !c.getActivo()) throw new RuntimeException("No tiene permisos");
        c.setBusAsignado(null);
        Conductor updated = conductorRepository.save(c);
        Empresa empresa = empresaRepository.findById(empresaId).orElseThrow();
        return ConductorResponse.fromWithEmpresa(updated, empresa.getNombre());
    }

    @Override @Transactional(readOnly = true)
    public List<ConductorResponse> getConductoresByEstado(Long empresaId, EstadoConductor estado) {
        Empresa empresa = empresaRepository.findById(empresaId).orElseThrow();
        return conductorRepository.findByEmpresaIdAndEstadoAndActivoTrue(empresaId, estado)
                .stream().map(c -> ConductorResponse.fromWithEmpresa(c, empresa.getNombre()))
                .collect(Collectors.toList());
    }

    @Override @Transactional(readOnly = true)
    public ConductorStatsResponse getStats(Long empresaId) {
        return ConductorStatsResponse.builder()
                .totalConductores(conductorRepository.countByEmpresaIdAndActivoTrue(empresaId))
                .conductoresActivos(conductorRepository.countByEmpresaIdAndEstadoAndActivoTrue(empresaId, EstadoConductor.ACTIVO))
                .conductoresInactivos(conductorRepository.countByEmpresaIdAndEstadoAndActivoTrue(empresaId, EstadoConductor.INACTIVO))
                .conductoresVacaciones(conductorRepository.countByEmpresaIdAndEstadoAndActivoTrue(empresaId, EstadoConductor.VACACIONES))
                .conductoresSuspendidos(conductorRepository.countByEmpresaIdAndEstadoAndActivoTrue(empresaId, EstadoConductor.SUSPENDIDO))
                .conductoresConBus(conductorRepository.countByEmpresaIdAndBusAsignadoIsNotNullAndActivoTrue(empresaId))
                .conductoresSinBus(conductorRepository.countByEmpresaIdAndBusAsignadoIsNullAndActivoTrue(empresaId))
                .licenciasVencidas(conductorRepository.countLicenciasVencidas(empresaId))
                .licenciasPorVencer(conductorRepository.countLicenciasPorVencer(empresaId, LocalDate.now().plusDays(30)))
                .build();
    }

    @Override @Transactional(readOnly = true)
    public DriverAssignmentResponse getMyAssignment(Long usuarioId) {
        Conductor c = conductorRepository.findByUsuarioIdAndActivoTrue(usuarioId)
                .orElseThrow(() -> new NoSuchElementException("Conductor no encontrado"));
        Bus bus = c.getBusAsignado();
        Route ruta = bus != null ? bus.getRutaAsignada() : null;
        return DriverAssignmentResponse.of(c.getId(), bus, ruta);
    }

    private String generateUsername(String email) {
        String base = email.split("@")[0], candidate = base;
        int i = 1;
        while (usuarioRepository.findByUsername(candidate).isPresent()) candidate = base + i++;
        return candidate;
    }

    private String generatePasswordFromDniOrRandom(String dni) {
        if (dni != null && dni.matches("\\d{8}"))
            return dni.substring(0, 4) + dni.substring(6, 8) + "!";
        return randomAlnum(10);
    }

    private String randomAlnum(int len) {
        final String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
        SecureRandom r = new SecureRandom();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) sb.append(chars.charAt(r.nextInt(chars.length())));
        return sb.toString();
    }
}
