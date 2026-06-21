package app_jwt.auth_service.modules.survey.application.service;

import app_jwt.auth_service.modules.survey.domain.model.UncertaintySurvey;
import app_jwt.auth_service.modules.survey.domain.port.input.UncertaintySurveyService;
import app_jwt.auth_service.modules.survey.infrastructure.adapter.input.rest.dto.CreateSurveyRequest;
import app_jwt.auth_service.modules.survey.infrastructure.adapter.input.rest.dto.SurveyResponse;
import app_jwt.auth_service.modules.survey.infrastructure.adapter.output.persistence.UncertaintySurveyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UncertaintySurveyServiceImpl implements UncertaintySurveyService {

    private final UncertaintySurveyRepository repository;

    @Override
    @Transactional
    public SurveyResponse save(CreateSurveyRequest request) {
        double promedio = (request.getQuestion1()
                + request.getQuestion2()
                + request.getQuestion3()
                + request.getQuestion4()) / 4.0;

        UncertaintySurvey survey = UncertaintySurvey.builder()
                .pregunta1(request.getQuestion1())
                .pregunta2(request.getQuestion2())
                .pregunta3(request.getQuestion3())
                .pregunta4(request.getQuestion4())
                .promedio(promedio)
                .nivel(calcularNivel(promedio))
                .fechaCreacion(LocalDateTime.now())
                .build();

        UncertaintySurvey saved = repository.save(survey);
        log.info("Encuesta de incertidumbre guardada id={} nivel={}", saved.getId(), saved.getNivel());
        return SurveyResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SurveyResponse> findAll() {
        return repository.findAllByOrderByFechaCreacionDesc()
                .stream()
                .map(SurveyResponse::from)
                .toList();
    }

    /** Mismo criterio que la app: <=2 Bajo, <=3.5 Medio, resto Alto. */
    private String calcularNivel(double promedio) {
        if (promedio <= 2) return "Bajo";
        if (promedio <= 3.5) return "Medio";
        return "Alto";
    }
}
