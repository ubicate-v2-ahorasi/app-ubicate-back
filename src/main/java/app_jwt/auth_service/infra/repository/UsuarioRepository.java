package app_jwt.auth_service.infra.repository;

import app_jwt.auth_service.domain.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);
    Optional<Usuario> findByCorreo(String correo);
    Optional<Usuario> findByTelefono(String telefono);
    Optional<Usuario> findByDni(String telefono);
    Optional<Usuario> findByEmpresaId(Long empresaId);
    Long countByEmpresaId(Long empresaId);
}