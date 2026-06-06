package app_jwt.auth_service.config;

import app_jwt.auth_service.shared.domain.model.Empresa;
import app_jwt.auth_service.shared.domain.model.Usuario;
import app_jwt.auth_service.shared.enums.Role;
import app_jwt.auth_service.shared.infrastructure.persistence.EmpresaRepository;
import app_jwt.auth_service.shared.infrastructure.persistence.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("!prod")
@RequiredArgsConstructor
@Slf4j
public class DefaultDataSeeder implements CommandLineRunner {

    private static final Long DEFAULT_EMPRESA_ID = 1L;
    private static final String DEFAULT_EMAIL = "yuyu@gmail.com";
    private static final String DEFAULT_PASSWORD = "Pablo123*";

    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (usuarioRepository.findByCorreo(DEFAULT_EMAIL).isPresent()) {
            log.info("Usuario semilla ya existe: {}", DEFAULT_EMAIL);
            return;
        }

        Empresa empresa = empresaRepository.findById(DEFAULT_EMPRESA_ID)
                .orElseGet(() -> empresaRepository.save(Empresa.builder()
                        .id(DEFAULT_EMPRESA_ID)
                        .nombre("Empresa Demo")
                        .ruc("00000000001")
                        .telefono("900000000")
                        .direccion("Trujillo")
                        .email(DEFAULT_EMAIL)
                        .activo(true)
                        .build()));

        Usuario usuario = Usuario.builder()
                .empresaId(empresa.getId())
                .nombre("Admin")
                .apellido("Demo")
                .dni("00000000")
                .telefono("900000000")
                .correo(DEFAULT_EMAIL)
                .username(DEFAULT_EMAIL)
                .password(passwordEncoder.encode(DEFAULT_PASSWORD))
                .role(Role.EMPRESA)
                .build();

        usuarioRepository.save(usuario);
        log.info("Usuario semilla creado: {} / {}", DEFAULT_EMAIL, DEFAULT_PASSWORD);
    }
}
