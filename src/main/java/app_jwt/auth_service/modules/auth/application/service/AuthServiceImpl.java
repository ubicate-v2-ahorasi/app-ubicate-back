package app_jwt.auth_service.modules.auth.application.service;

import app_jwt.auth_service.modules.auth.infrastructure.adapter.input.rest.dto.AuthResponse;
import app_jwt.auth_service.modules.auth.infrastructure.adapter.input.rest.dto.LoginRequest;
import app_jwt.auth_service.modules.auth.infrastructure.adapter.input.rest.dto.RegisterRequest;
import app_jwt.auth_service.modules.auth.infrastructure.adapter.input.rest.dto.UserResponse;
import app_jwt.auth_service.modules.bus.domain.model.Bus;
import app_jwt.auth_service.modules.conductor.domain.model.Conductor;
import app_jwt.auth_service.modules.conductor.infrastructure.adapter.output.persistence.ConductorRepository;
import app_jwt.auth_service.shared.domain.model.Empresa;
import app_jwt.auth_service.shared.domain.model.Usuario;
import app_jwt.auth_service.shared.enums.Role;
import app_jwt.auth_service.shared.infrastructure.persistence.EmpresaRepository;
import app_jwt.auth_service.shared.infrastructure.persistence.UsuarioRepository;
import app_jwt.auth_service.shared.service.RedisRealtimeService;
import app_jwt.auth_service.shared.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl {

    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final ConductorRepository conductorRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final RedisRealtimeService redisRealtimeService;

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

        try {
            redisRealtimeService.upsertEmpresa(empresaGuardada);
        } catch (Exception e) {
            log.error("❌ Error Redis Realtime (continuando): {}", e.getMessage());
        }

        Usuario usuario = Usuario.builder()
                .username(request.getEmail()).correo(request.getEmail())
                .nombre(request.getNombre()).apellido(request.getApellido())
                .telefono(request.getTelefono()).dni(request.getDni())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.EMPRESA).empresaId(nuevaEmpresaId).build();

        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        String token = jwtService.getToken(usuarioGuardado, usuarioGuardado);
        UserResponse userResponse = createUserResponseWithBusInfo(usuarioGuardado);
        log.info("✅ Registro de empresa completado");
        return AuthResponse.success(token, userResponse);
    }

    @Transactional
    public AuthResponse registerChofer(RegisterRequest request) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Los choferes deben ser creados desde el panel de administración de la empresa.");
    }

    public AuthResponse login(LoginRequest request) {
        try {
            log.info("🔵 Intento de login para: {}", request.getEmail());
            Usuario usuario = usuarioRepository.findByCorreo(request.getEmail())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
            validateUserForLogin(usuario);
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(usuario.getUsername(), request.getPassword()));
            String token = jwtService.getToken(usuario, usuario);
            UserResponse userResponse = createUserResponseWithBusInfo(usuario);
            log.info("✅ Login exitoso para: {}", request.getEmail());
            return AuthResponse.success(token, userResponse);
        } catch (AuthenticationException e) {
            log.error("❌ Login fallido para: {}", request.getEmail());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }
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
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El email ya está registrado");
        if (usuarioRepository.findByUsername(request.getEmail()).isPresent())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El usuario ya existe");
    }

    private void validateUserForLogin(Usuario usuario) {
        if (usuario.getEmpresaId() == null)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario sin empresa asignada");
    }

    private Long generateUniqueEmpresaId() {
        long timestamp = System.currentTimeMillis();
        int hashCode = UUID.randomUUID().toString().hashCode();
        long empresaId = Math.abs(timestamp + hashCode);
        int intentos = 0;
        while ((empresaRepository.existsById(empresaId) ||
                usuarioRepository.findByEmpresaId(empresaId).isPresent()) && intentos < 10) {
            empresaId = Math.abs(System.currentTimeMillis() + UUID.randomUUID().toString().hashCode());
            intentos++;
        }
        if (intentos >= 10)
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo generar ID único");
        return empresaId;
    }

    @Transactional(readOnly = true)
    public Usuario getUsuarioByEmail(String email) {
        return usuarioRepository.findByCorreo(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    @Transactional(readOnly = true)
    public Usuario getUsuarioByUsername(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }
}
