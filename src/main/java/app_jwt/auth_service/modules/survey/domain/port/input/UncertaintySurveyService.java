package app_jwt.auth_service.modules.survey.domain.port.input;

import app_jwt.auth_service.modules.survey.infrastructure.adapter.input.rest.dto.CreateSurveyRequest;
import app_jwt.auth_service.modules.survey.infrastructure.adapter.input.rest.dto.SurveyResponse;

import java.util.List;

public interface UncertaintySurveyService {

    SurveyResponse save(CreateSurveyRequest request);

    List<SurveyResponse> findAll();
}
