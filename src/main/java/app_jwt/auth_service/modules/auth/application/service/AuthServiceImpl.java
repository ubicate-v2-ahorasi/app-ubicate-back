package app_jwt.auth_service.modules.auth.application.service;

import app_jwt.auth_service.modules.auth.application.dto.AuthResponse;
import app_jwt.auth_service.modules.auth.application.dto.LoginRequest;
import app_jwt.auth_service.modules.auth.application.dto.RegisterRequest;
import app_jwt.auth_service.modules.auth.application.dto.UserResponse;
import app_jwt.auth_service.modules.auth.application.port.input.AuthUseCase;
import app_jwt.auth_service.modules.auth.domain.exception.*;
import app_jwt.auth_service.modules.auth.domain.port.*;
import app_jwt.auth_service.modules.bus.domain.model.Bus;
import app_jwt.auth_service.modules.conductor.domain.model.Conductor;
import app_jwt.auth_service.shared.domain.model.Empresa;
import app_jwt.auth_service.shared.domain.model.Usuario;
import app_jwt.auth_service.shared.enums.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthUseCase {

    private final AuthUserRepositoryPort usuarioRepository;
    private final AuthEmpresaRepositoryPort empresaRepository;
    private final AuthConductorRepositoryPort conductorRepository;
    private final AuthSecurityPort securityPort;
    private final AuthCachePort cachePort;

    @Override
    @Transactional
    public AuthResponse registerEmpresa(RegisterRequest request) {
        log.info("🔵 Iniciando registro de empresa para: {}", request.getEmail());
        validateRegisterRequest(request);

        Long nuevaEmpresaId = generateUniqueEmpresaId();

        Empresa empresa = Empresa.builder()
                .id(nuevaEmpresaId)
                .nombre(request.getNombreEmpresa() != null ?
                        request.getNombreEmpresa() :
                        request.getNombre() + " " + request.getApellido())
                .ruc(request.getRuc()).telefono(request.getTelefono())
                .direccion(request.getDireccion()).activo(true).build();

        Empresa empresaGuardada = empresaRepository.save(empresa);
        log.info("✅ Empresa guardada con ID: {}", empresaGuardada.getId());

        cachePort.upsertEmpresa(empresaGuardada);

        Usuario usuario = Usuario.builder()
                .username(request.getEmail()).correo(request.getEmail())
                .nombre(request.getNombre()).apellido(request.getApellido())
                .telefono(request.getTelefono()).dni(request.getDni())
                .password(securityPort.encodePassword(request.getPassword()))
                .role(Role.EMPRESA).empresaId(nuevaEmpresaId).build();

        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        String token = securityPort.generateToken(usuarioGuardado);
        UserResponse userResponse = createUserResponseWithBusInfo(usuarioGuardado);
        log.info("✅ Registro de empresa completado");
        return AuthResponse.success(token, userResponse);
    }

    @Override
    @Transactional
    public AuthResponse registerChofer(RegisterRequest request) {
        throw new RoleNotAllowedException("Los choferes deben ser creados desde el panel de administración de la empresa.");
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("🔵 Intento de login para: {}", request.getEmail());
        Usuario usuario = usuarioRepository.findByCorreo(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));
        validateUserForLogin(usuario);
        
        securityPort.authenticate(usuario.getUsername(), request.getPassword());
        
        String token = securityPort.generateToken(usuario);
        UserResponse userResponse = createUserResponseWithBusInfo(usuario);
        log.info("✅ Login exitoso para: {}", request.getEmail());
        return AuthResponse.success(token, userResponse);
    }

    private UserResponse createUserResponseWithBusInfo(Usuario usuario) {
        UserResponse.UserResponseBuilder builder = UserResponse.builder()
                .id(usuario.getId()).empresaId(usuario.getEmpresaId())
                .nombre(usuario.getNombre()).apellido(usuario.getApellido())
                .dni(usuario.getDni()).telefono(usuario.getTelefono())
                .correo(usuario.getCorreo()).username(usuario.getUsername())
                .role(usuario.getRole());

        if (usuario.getRole() == Role.CHOFER) {
            try {
                Optional<Conductor> conductor = conductorRepository.findByUsuarioIdAndActivoTrue(usuario.getId());
                if (conductor.isPresent() && conductor.get().getBusAsignado() != null) {
                    Bus bus = conductor.get().getBusAsignado();
                    builder.busId(bus.getId().toString()).busPlate(bus.getPlaca()).busNumber(bus.getPlaca());
                }
            } catch (Exception e) {
                log.warn("⚠️ No se pudo obtener bus para chofer: {}", usuario.getId());
            }
        }
        return builder.build();
    }

    private void validateRegisterRequest(RegisterRequest request) {
        if (usuarioRepository.findByCorreo(request.getEmail()).isPresent())
            throw new UserAlreadyExistsException("El email ya está registrado");
        if (usuarioRepository.findByUsername(request.getEmail()).isPresent())
            throw new UserAlreadyExistsException("El usuario ya existe");
    }

    private void validateUserForLogin(Usuario usuario) {
        if (usuario.getEmpresaId() == null)
            throw new RoleNotAllowedException("Usuario sin empresa asignada");
    }

    private Long generateUniqueEmpresaId() {
        long timestamp = System.currentTimeMillis();
        int hashCode = UUID.randomUUID().toString().hashCode();
        long empresaId = Math.abs(timestamp + hashCode);
        int intentos = 0;
        while ((empresaRepository.existsById(empresaId) ||
                usuarioRepository.existsByEmpresaId(empresaId)) && intentos < 10) {
            empresaId = Math.abs(System.currentTimeMillis() + UUID.randomUUID().toString().hashCode());
            intentos++;
        }
        if (intentos >= 10)
            throw new AuthException("No se pudo generar ID único para la empresa");
        return empresaId;
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario getUsuarioByEmail(String email) {
        return usuarioRepository.findByCorreo(email)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario getUsuarioByUsername(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));
    }
}
