package app_jwt.auth_service.modules.survey.infrastructure.adapter.output.persistence;

import app_jwt.auth_service.modules.survey.domain.model.UncertaintySurvey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UncertaintySurveyRepository extends JpaRepository<UncertaintySurvey, Long> {

    List<UncertaintySurvey> findAllByOrderByFechaCreacionDesc();
}
