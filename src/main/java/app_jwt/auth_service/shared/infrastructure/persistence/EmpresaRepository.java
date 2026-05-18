package app_jwt.auth_service.shared.infrastructure.persistence;

import app_jwt.auth_service.shared.domain.model.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
    Optional<Empresa> findByRuc(String ruc);
    List<Empresa> findByActivoTrue();
    boolean existsByIdAndActivoTrue(Long id);
}
