package app_jwt.auth_service.domain.service;

import app_jwt.auth_service.domain.dtos.conductor.*;
import app_jwt.auth_service.domain.entity.*;
import app_jwt.auth_service.domain.enums.CategoriaLicencia;
import app_jwt.auth_service.domain.enums.EstadoConductor;
import app_jwt.auth_service.domain.enums.Role;
// ❌ REMOVIDO: import app_jwt.auth_service.domain.enums.TurnoConductor;
import app_jwt.auth_service.infra.repository.BusRepository;
import app_jwt.auth_service.infra.repository.ConductorRepository;
import app_jwt.auth_service.infra.repository.EmpresaRepository;
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
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConductorService {

    private final ConductorRepository conductorRepository;
    private final UsuarioRepository usuarioRepository;
    private final BusRepository busRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtils securityUtils;
    private final EmpresaRepository empresaRepository;

    @Transactional
    public ConductorCreatedResponse createConductor(CreateConductorRequest request, Long empresaId) {

        if (empresaId == null) {
            throw new RuntimeException("ID de empresa es requerido");
        }

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));

        if (!empresa.getActivo()) {
            throw new RuntimeException("La empresa no está activa");
        }

        String email = request.getEmail().toLowerCase(Locale.ROOT).trim();
        String telefono = request.getTelefono().toLowerCase(Locale.ROOT).trim();
        String dni = request.getDni().toLowerCase(Locale.ROOT).trim();

        if (usuarioRepository.findByCorreo(email).isPresent()) {
            throw new RuntimeException("El email ya está registrado");
        }
        if (conductorRepository.existsByNumeroLicenciaAndActivoTrue(request.getNumeroLicencia())) {
            throw new RuntimeException("Ya existe un conductor con ese número de licencia");
        }
        if (usuarioRepository.findByTelefono(telefono).isPresent()){
            throw new RuntimeException("Ya existe un conductor con ese número de telefono");
        }
        if (usuarioRepository.findByDni(dni).isPresent()){
            throw new RuntimeException("Ya existe un conductor con ese DNI");
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

        if (conductorRepository.existsByUsuarioIdAndActivoTrue(savedUsuario.getId())) {
            throw new RuntimeException("El usuario ya es conductor");
        }

        Bus busAsignado = null;
        if (request.getBusAsignadoId() != null) {
            busAsignado = busRepository.findById(request.getBusAsignadoId())
                    .orElseThrow(() -> new RuntimeException("Bus no encontrado"));

            if (!busAsignado.getEmpresaId().equals(empresaId)) {
                throw new RuntimeException("El bus no pertenece a esta empresa");
            }

            if (conductorRepository.existsByBusAsignadoIdAndActivoTrue(request.getBusAsignadoId())) {
                throw new RuntimeException("El bus ya está asignado a otro conductor");
            }
        }

        Conductor conductor = Conductor.builder()
                .usuario(savedUsuario)
                .numeroLicencia(request.getNumeroLicencia().toUpperCase(Locale.ROOT).trim())
                .categoriaLicencia(request.getCategoriaLicencia())
                .fechaVencimientoLicencia(request.getFechaVencimientoLicencia())
                // ❌ REMOVIDO: .turno(request.getTurno())
                .estado(EstadoConductor.ACTIVO)
                .busAsignado(busAsignado)
                .empresaId(empresaId)
                .fechaIngreso(LocalDate.now())
                .activo(true)
                .build();

        Conductor savedConductor = conductorRepository.save(conductor);

        ConductorResponse conductorResponse = ConductorResponse.fromWithEmpresa(savedConductor, empresa.getNombre());

        return ConductorCreatedResponse.builder()
                .conductor(conductorResponse)
                .username(username)
                .tempPassword(tempPasswordRaw)
                .build();
    }

    @Transactional(readOnly = true)
    public Page<ConductorResponse> getConductores(Long empresaId, String searchTerm, EstadoConductor estado, CategoriaLicencia categoria, Pageable pageable) {
        // Pasar todos los parámetros al repositorio
        return conductorRepository.searchConductores(empresaId, searchTerm, estado, categoria, pageable)
                .map(conductor -> ConductorResponse.from(conductor));
    }


    @Transactional(readOnly = true)
    public Page<ConductorResponse> searchConductores(Long empresaId, String searchTerm, Pageable pageable) {
        Page<Conductor> conductores = conductorRepository.searchConductores(empresaId, searchTerm, null, null, pageable);
        return conductores.map(conductor -> ConductorResponse.from(conductor));
    }

    @Transactional(readOnly = true)
    public ConductorResponse getConductorById(Long conductorId, Long empresaId) {
        Conductor conductor = conductorRepository.findById(conductorId)
                .orElseThrow(() -> new RuntimeException("Conductor no encontrado"));

        if (!conductor.getEmpresaId().equals(empresaId)) {
            throw new RuntimeException("No tiene permisos para acceder a este conductor");
        }

        if (!conductor.getActivo()) {
            throw new RuntimeException("Conductor no disponible");
        }

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));

        return ConductorResponse.fromWithEmpresa(conductor, empresa.getNombre());
    }

    @Transactional
    public ConductorResponse updateConductor(Long conductorId, UpdateConductorRequest request, Long empresaId) {
        Conductor conductor = conductorRepository.findById(conductorId)
                .orElseThrow(() -> new RuntimeException("Conductor no encontrado"));

        if (!conductor.getEmpresaId().equals(empresaId)) {
            throw new RuntimeException("No tiene permisos para modificar este conductor");
        }

        if (!conductor.getActivo()) {
            throw new RuntimeException("No puede modificar este conductor");
        }

        if (request.getTelefono() != null) {
            conductor.getUsuario().setTelefono(request.getTelefono().trim());
        }
        if (request.getFechaVencimientoLicencia() != null) {
            conductor.setFechaVencimientoLicencia(request.getFechaVencimientoLicencia());
        }
        // ❌ REMOVIDO: if (request.getTurno() != null) { conductor.setTurno(request.getTurno()); }
        if (request.getEstado() != null) {
            conductor.setEstado(request.getEstado());
        }
        if (request.getBusAsignadoId() != null) {
            Bus bus = busRepository.findById(request.getBusAsignadoId())
                    .orElseThrow(() -> new RuntimeException("Bus no encontrado"));

            if (!bus.getEmpresaId().equals(empresaId)) {
                throw new RuntimeException("El bus no pertenece a esta empresa");
            }

            conductor.setBusAsignado(bus);
        }

        Conductor updatedConductor = conductorRepository.save(conductor);

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));

        return ConductorResponse.fromWithEmpresa(updatedConductor, empresa.getNombre());
    }

    @Transactional
    public void deleteConductor(Long conductorId, Long empresaId) {
        Conductor conductor = conductorRepository.findById(conductorId)
                .orElseThrow(() -> new RuntimeException("Conductor no encontrado"));

        if (!conductor.getEmpresaId().equals(empresaId)) {
            throw new RuntimeException("No tiene permisos para eliminar este conductor");
        }

        conductor.setActivo(false);
        conductorRepository.save(conductor);
    }

    @Transactional
    public ConductorResponse cambiarEstado(Long conductorId, EstadoConductor estado, Long empresaId) {
        Conductor conductor = conductorRepository.findById(conductorId)
                .orElseThrow(() -> new RuntimeException("Conductor no encontrado"));

        if (!conductor.getEmpresaId().equals(empresaId)) {
            throw new RuntimeException("No tiene permisos para modificar este conductor");
        }

        if (!conductor.getActivo()) {
            throw new RuntimeException("No tiene permisos para modificar este conductor");
        }

        conductor.setEstado(estado);
        Conductor updatedConductor = conductorRepository.save(conductor);

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));

        return ConductorResponse.fromWithEmpresa(updatedConductor, empresa.getNombre());
    }

    @Transactional
    public ConductorResponse asignarBus(Long conductorId, Long busId, Long empresaId) {
        Conductor conductor = conductorRepository.findById(conductorId)
                .orElseThrow(() -> new RuntimeException("Conductor no encontrado"));

        if (!conductor.getEmpresaId().equals(empresaId)) {
            throw new RuntimeException("No tiene permisos para modificar este conductor");
        }

        if (!conductor.getActivo()) {
            throw new RuntimeException("No tiene permisos para modificar este conductor");
        }

        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new RuntimeException("Bus no encontrado"));

        if (!bus.getEmpresaId().equals(empresaId)) {
            throw new RuntimeException("El bus no pertenece a esta empresa");
        }

        Optional<Conductor> conductorConBus = conductorRepository.findByBusAsignadoIdAndActivoTrue(busId);
        if (conductorConBus.isPresent() && !conductorConBus.get().getId().equals(conductorId)) {
            throw new RuntimeException("El bus ya está asignado a otro conductor");
        }

        if (conductor.getBusAsignado() != null && !conductor.getBusAsignado().getId().equals(busId)) {
            throw new RuntimeException("El conductor ya tiene un bus asignado. Debe removerlo primero");
        }

        conductor.setBusAsignado(bus);
        Conductor updatedConductor = conductorRepository.save(conductor);

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));

        return ConductorResponse.fromWithEmpresa(updatedConductor, empresa.getNombre());
    }

    @Transactional
    public ConductorResponse removerBus(Long conductorId, Long empresaId) {
        Conductor conductor = conductorRepository.findById(conductorId)
                .orElseThrow(() -> new RuntimeException("Conductor no encontrado"));

        if (!conductor.getEmpresaId().equals(empresaId)) {
            throw new RuntimeException("No tiene permisos para modificar este conductor");
        }

        if (!conductor.getActivo()) {
            throw new RuntimeException("No tiene permisos para modificar este conductor");
        }

        conductor.setBusAsignado(null);
        Conductor updatedConductor = conductorRepository.save(conductor);

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));

        return ConductorResponse.fromWithEmpresa(updatedConductor, empresa.getNombre());
    }

    @Transactional(readOnly = true)
    public List<ConductorResponse> getConductoresByEstado(Long empresaId, EstadoConductor estado) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));

        List<Conductor> conductores = conductorRepository.findByEmpresaIdAndEstadoAndActivoTrue(empresaId, estado);
        return conductores.stream()
                .map(conductor -> ConductorResponse.fromWithEmpresa(conductor, empresa.getNombre()))
                .collect(Collectors.toList());
    }

    // ❌ MÉTODO COMPLETO REMOVIDO: getConductoresByTurno()

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

    @Transactional(readOnly = true)
    public DriverAssignmentResponse getMyAssignment(Long usuarioId) {
        Conductor c = conductorRepository.findByUsuarioIdAndActivoTrue(usuarioId)
                .orElseThrow(() -> new NoSuchElementException("Conductor no encontrado"));
        Bus bus = c.getBusAsignado();
        Route ruta = bus != null ? bus.getRutaAsignada() : null;
        return DriverAssignmentResponse.of(c.getId(), bus, ruta);
    }
}