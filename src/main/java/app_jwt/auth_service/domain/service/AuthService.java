package app_jwt.auth_service.domain.service;

import app_jwt.auth_service.domain.dtos.auth.AuthResponse;
import app_jwt.auth_service.domain.dtos.auth.LoginRequest;
import app_jwt.auth_service.domain.dtos.auth.RegisterRequest;
import app_jwt.auth_service.domain.dtos.auth.UserResponse;
import app_jwt.auth_service.domain.entity.Bus;
import app_jwt.auth_service.domain.entity.Conductor;
import app_jwt.auth_service.domain.entity.Empresa;
import app_jwt.auth_service.domain.entity.Usuario;
import app_jwt.auth_service.domain.enums.Role;
import app_jwt.auth_service.infra.repository.ConductorRepository;
import app_jwt.auth_service.infra.repository.EmpresaRepository;
import app_jwt.auth_service.infra.repository.UsuarioRepository;
import app_jwt.auth_service.infra.security.JwtService;
import lombok.RequiredArgsConstructor;
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
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final ConductorRepository conductorRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse registerEmpresa(RegisterRequest request) {
        validateRegisterRequest(request);

        Long nuevaEmpresaId = generateUniqueEmpresaId();

        Empresa empresa = Empresa.builder()
                .id(nuevaEmpresaId)
                .nombre(request.getNombreEmpresa() != null ?
                        request.getNombreEmpresa() :
                        request.getNombre() + " " + request.getApellido())
                .ruc(request.getRuc())
                .telefono(request.getTelefono())
                .direccion(request.getDireccion())
                .activo(true)
                .build();

        empresaRepository.save(empresa);

        Usuario usuario = Usuario.builder()
                .username(request.getEmail())
                .correo(request.getEmail())
                .nombre(request.getNombre())
                .apellido(request.getApellido())
                .telefono(request.getTelefono())
                .dni(request.getDni())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.EMPRESA)
                .empresaId(nuevaEmpresaId)
                .build();

        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        String token = jwtService.getToken(usuarioGuardado, usuarioGuardado);
        UserResponse userResponse = createUserResponseWithBusInfo(usuarioGuardado);

        return AuthResponse.success(token, userResponse);
    }

    @Transactional
    public AuthResponse registerChofer(RegisterRequest request) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Los choferes deben ser creados desde el panel de administración de la empresa. " +
                        "Contacte a su administrador para obtener acceso.");
    }

    public AuthResponse login(LoginRequest request) {
        try {
            Usuario usuario = usuarioRepository.findByCorreo(request.getEmail())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

            validateUserForLogin(usuario);

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            usuario.getUsername(),
                            request.getPassword()
                    )
            );

            String token = jwtService.getToken(usuario, usuario);
            UserResponse userResponse = createUserResponseWithBusInfo(usuario);

            return AuthResponse.success(token, userResponse);

        } catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }
    }

    private UserResponse createUserResponseWithBusInfo(Usuario usuario) {
        UserResponse.UserResponseBuilder builder = UserResponse.builder()
                .id(usuario.getId())
                .empresaId(usuario.getEmpresaId())
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .dni(usuario.getDni())
                .telefono(usuario.getTelefono())
                .correo(usuario.getCorreo())
                .username(usuario.getUsername())
                .role(usuario.getRole());

        if (usuario.getRole() == Role.CHOFER) {
            try {
                Optional<Conductor> conductor = conductorRepository.findByUsuarioIdAndActivoTrue(usuario.getId());
                if (conductor.isPresent() && conductor.get().getBusAsignado() != null) {
                    Bus bus = conductor.get().getBusAsignado();
                    builder.busId(bus.getId().toString())
                            .busPlate(bus.getPlaca())
                            .busNumber(bus.getPlaca());
                }
            } catch (Exception e) {
                // Continúa sin datos del bus en caso de error
            }
        }

        return builder.build();
    }

    private void validateRegisterRequest(RegisterRequest request) {
        if (usuarioRepository.findByCorreo(request.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El email ya está registrado");
        }
        if (usuarioRepository.findByUsername(request.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El usuario ya existe");
        }
    }

    private void validateUserForLogin(Usuario usuario) {
        if (usuario.getEmpresaId() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario sin empresa asignada");
        }
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

        if (intentos >= 10) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "No se pudo generar un ID único para la empresa");
        }

        return empresaId;
    }

    @Transactional(readOnly = true)
    public Usuario getUsuarioByEmail(String email) {
        return usuarioRepository.findByCorreo(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuario no encontrado"
                ));
    }

    @Transactional(readOnly = true)
    public Usuario getUsuarioByUsername(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuario no encontrado"
                ));
    }
}