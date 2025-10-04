package app_jwt.auth_service.infra.repository;

import app_jwt.auth_service.domain.entity.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
    List<Empresa> findByActivoTrue();
    boolean existsByIdAndActivoTrue(Long id);
}