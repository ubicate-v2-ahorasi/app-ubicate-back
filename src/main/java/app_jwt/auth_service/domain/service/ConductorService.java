package app_jwt.auth_service.domain.service;

import app_jwt.auth_service.domain.dtos.conductor.*;
import app_jwt.auth_service.domain.entity.Bus;
import app_jwt.auth_service.domain.entity.Conductor;
import app_jwt.auth_service.domain.entity.Usuario;
import app_jwt.auth_service.domain.enums.EstadoConductor;
import app_jwt.auth_service.domain.enums.Role;
import app_jwt.auth_service.domain.enums.TurnoConductor;
import app_jwt.auth_service.infra.repository.BusRepository;
import app_jwt.auth_service.infra.repository.ConductorRepository;
import app_jwt.auth_service.infra.repository.UsuarioRepository;
import app_jwt.auth_service.infra.security.SecurityUtils;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConductorService {

    private final ConductorRepository conductorRepository;
    private final UsuarioRepository usuarioRepository;
    private final BusRepository busRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtils securityUtils;

    @Transactional
    public ConductorCreatedResponse createConductor(CreateConductorRequest request, Long empresaId) {
        String email = request.getEmail().toLowerCase(Locale.ROOT).trim();

        if (usuarioRepository.findByCorreo(email).isPresent()) {
            throw new RuntimeException("El email ya está registrado");
        }
        if (conductorRepository.existsByNumeroLicenciaAndActivoTrue(request.getNumeroLicencia())) {
            throw new RuntimeException("Ya existe un conductor con ese número de licencia");
        }

        String username = generateUsername(email);
        String tempPasswordRaw = generatePasswordFromDniOrRandom(request.getDni());
        String encodedPassword = passwordEncoder.encode(tempPasswordRaw);

        Usuario usuario = Usuario.builder()
                .username(username)
                .correo(email)
                .nombre(request.getNombre().trim())
                .apellido(request.getApellido().trim())
                .telefono(request.getTelefono().trim())
                .dni(request.getDni())
                .password(encodedPassword)
                .role(Role.CHOFER)
                .empresaId(empresaId)
                .build();

        Usuario savedUsuario = usuarioRepository.save(usuario);

        Bus busAsignado = null;
        if (request.getBusAsignadoId() != null) {
            busAsignado = busRepository.findById(request.getBusAsignadoId())
                    .orElseThrow(() -> new RuntimeException("Bus no encontrado"));
            securityUtils.validateEmpresaAccess(busAsignado.getEmpresaId(), empresaId, "bus");
        }

        Conductor conductor = Conductor.builder()
                .usuario(savedUsuario)
                .numeroLicencia(request.getNumeroLicencia().toUpperCase(Locale.ROOT).trim())
                .categoriaLicencia(request.getCategoriaLicencia())
                .fechaVencimientoLicencia(request.getFechaVencimientoLicencia())
                .turno(request.getTurno())
                .estado(EstadoConductor.ACTIVO)
                .busAsignado(busAsignado)
                .empresaId(empresaId)
                .fechaIngreso(LocalDate.now())
                .activo(true)
                .build();

        Conductor savedConductor = conductorRepository.save(conductor);

        return ConductorCreatedResponse.builder()
                .conductor(ConductorResponse.from(savedConductor))
                .username(username)
                .tempPassword(tempPasswordRaw)
                .build();
    }

    @Transactional(readOnly = true)
    public Page<ConductorResponse> getConductores(Long empresaId, Pageable pageable) {
        Page<Conductor> conductores = conductorRepository.findByEmpresaIdAndActivoTrue(empresaId, pageable);
        return conductores.map(ConductorResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<ConductorResponse> searchConductores(Long empresaId, String searchTerm, Pageable pageable) {
        Page<Conductor> conductores = conductorRepository.searchConductores(empresaId, searchTerm, pageable);
        return conductores.map(ConductorResponse::from);
    }

    @Transactional(readOnly = true)
    public ConductorResponse getConductorById(Long conductorId, Long empresaId) {
        Conductor conductor = conductorRepository.findById(conductorId)
                .orElseThrow(() -> new RuntimeException("Conductor no encontrado"));
        securityUtils.validateEmpresaAccess(conductor.getEmpresaId(), empresaId, "conductor");
        if (!conductor.getActivo()) {
            throw new RuntimeException("No tiene permisos para acceder a este conductor");
        }
        return ConductorResponse.from(conductor);
    }

    @Transactional
    public ConductorResponse updateConductor(Long conductorId, UpdateConductorRequest request, Long empresaId) {
        Conductor conductor = conductorRepository.findById(conductorId)
                .orElseThrow(() -> new RuntimeException("Conductor no encontrado"));
        securityUtils.validateEmpresaAccess(conductor.getEmpresaId(), empresaId, "conductor");
        if (!conductor.getActivo()) {
            throw new RuntimeException("No tiene permisos para modificar este conductor");
        }

        if (request.getTelefono() != null) {
            conductor.getUsuario().setTelefono(request.getTelefono().trim());
        }
        if (request.getFechaVencimientoLicencia() != null) {
            conductor.setFechaVencimientoLicencia(request.getFechaVencimientoLicencia());
        }
        if (request.getTurno() != null) {
            conductor.setTurno(request.getTurno());
        }
        if (request.getEstado() != null) {
            conductor.setEstado(request.getEstado());
        }
        if (request.getBusAsignadoId() != null) {
            Bus bus = busRepository.findById(request.getBusAsignadoId())
                    .orElseThrow(() -> new RuntimeException("Bus no encontrado"));
            securityUtils.validateEmpresaAccess(bus.getEmpresaId(), empresaId, "bus");
            conductor.setBusAsignado(bus);
        }

        Conductor updatedConductor = conductorRepository.save(conductor);
        return ConductorResponse.from(updatedConductor);
    }

    @Transactional
    public void deleteConductor(Long conductorId, Long empresaId) {
        Conductor conductor = conductorRepository.findById(conductorId)
                .orElseThrow(() -> new RuntimeException("Conductor no encontrado"));
        securityUtils.validateEmpresaAccess(conductor.getEmpresaId(), empresaId, "conductor");
        conductor.setActivo(false);
        conductorRepository.save(conductor);
    }

    @Transactional
    public ConductorResponse cambiarEstado(Long conductorId, EstadoConductor estado, Long empresaId) {
        Conductor conductor = conductorRepository.findById(conductorId)
                .orElseThrow(() -> new RuntimeException("Conductor no encontrado"));
        securityUtils.validateEmpresaAccess(conductor.getEmpresaId(), empresaId, "conductor");
        if (!conductor.getActivo()) {
            throw new RuntimeException("No tiene permisos para modificar este conductor");
        }
        conductor.setEstado(estado);
        Conductor updatedConductor = conductorRepository.save(conductor);
        return ConductorResponse.from(updatedConductor);
    }

    @Transactional
    public ConductorResponse asignarBus(Long conductorId, Long busId, Long empresaId) {
        Conductor conductor = conductorRepository.findById(conductorId)
                .orElseThrow(() -> new RuntimeException("Conductor no encontrado"));
        securityUtils.validateEmpresaAccess(conductor.getEmpresaId(), empresaId, "conductor");
        if (!conductor.getActivo()) {
            throw new RuntimeException("No tiene permisos para modificar este conductor");
        }
        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new RuntimeException("Bus no encontrado"));
        securityUtils.validateEmpresaAccess(bus.getEmpresaId(), empresaId, "bus");
        conductor.setBusAsignado(bus);
        Conductor updatedConductor = conductorRepository.save(conductor);
        return ConductorResponse.from(updatedConductor);
    }

    @Transactional
    public ConductorResponse removerBus(Long conductorId, Long empresaId) {
        Conductor conductor = conductorRepository.findById(conductorId)
                .orElseThrow(() -> new RuntimeException("Conductor no encontrado"));
        securityUtils.validateEmpresaAccess(conductor.getEmpresaId(), empresaId, "conductor");
        if (!conductor.getActivo()) {
            throw new RuntimeException("No tiene permisos para modificar este conductor");
        }
        conductor.setBusAsignado(null);
        Conductor updatedConductor = conductorRepository.save(conductor);
        return ConductorResponse.from(updatedConductor);
    }

    @Transactional(readOnly = true)
    public List<ConductorResponse> getConductoresByEstado(Long empresaId, EstadoConductor estado) {
        List<Conductor> conductores = conductorRepository.findByEmpresaIdAndEstadoAndActivoTrue(empresaId, estado);
        return conductores.stream().map(ConductorResponse::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ConductorResponse> getConductoresByTurno(Long empresaId, TurnoConductor turno) {
        List<Conductor> conductores = conductorRepository.findByEmpresaIdAndTurnoAndActivoTrue(empresaId, turno);
        return conductores.stream().map(ConductorResponse::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ConductorStatsResponse getStats(Long empresaId) {
        Long total = conductorRepository.countByEmpresaIdAndActivoTrue(empresaId);
        Long activos = conductorRepository.countByEmpresaIdAndEstadoAndActivoTrue(empresaId, EstadoConductor.ACTIVO);
        Long inactivos = conductorRepository.countByEmpresaIdAndEstadoAndActivoTrue(empresaId, EstadoConductor.INACTIVO);
        Long vacaciones = conductorRepository.countByEmpresaIdAndEstadoAndActivoTrue(empresaId, EstadoConductor.VACACIONES);
        Long suspendidos = conductorRepository.countByEmpresaIdAndEstadoAndActivoTrue(empresaId, EstadoConductor.SUSPENDIDO);
        Long conBus = conductorRepository.countByEmpresaIdAndBusAsignadoIsNotNullAndActivoTrue(empresaId);
        Long sinBus = conductorRepository.countByEmpresaIdAndBusAsignadoIsNullAndActivoTrue(empresaId);
        Long licenciasVencidas = conductorRepository.countLicenciasVencidas(empresaId);
        Long licenciasPorVencer = conductorRepository.countLicenciasPorVencer(empresaId, LocalDate.now().plusDays(30));

        return ConductorStatsResponse.builder()
                .totalConductores(total)
                .conductoresActivos(activos)
                .conductoresInactivos(inactivos)
                .conductoresVacaciones(vacaciones)
                .conductoresSuspendidos(suspendidos)
                .conductoresConBus(conBus)
                .conductoresSinBus(sinBus)
                .licenciasVencidas(licenciasVencidas)
                .licenciasPorVencer(licenciasPorVencer)
                .build();
    }

    private String generateUsername(String email) {
        String base = email.split("@")[0];
        String candidate = base;
        int i = 1;
        while (usuarioRepository.findByUsername(candidate).isPresent()) {
            candidate = base + i;
            i++;
        }
        return candidate;
    }

    private String generatePasswordFromDniOrRandom(String dni) {
        if (dni != null && dni.matches("\\d{8}")) {
            String first4 = dni.substring(0, 4);
            String last2 = dni.substring(6, 8);
            return first4 + last2 + "!";
        }
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