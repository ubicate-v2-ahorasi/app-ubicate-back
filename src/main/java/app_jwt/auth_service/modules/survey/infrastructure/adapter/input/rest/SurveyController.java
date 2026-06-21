package app_jwt.auth_service.modules.survey.infrastructure.adapter.input.rest;

import app_jwt.auth_service.modules.survey.domain.port.input.UncertaintySurveyService;
import app_jwt.auth_service.modules.survey.infrastructure.adapter.input.rest.dto.CreateSurveyRequest;
import app_jwt.auth_service.modules.survey.infrastructure.adapter.input.rest.dto.SurveyResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Cuestionario de incertidumbre (anonimo). Esta bajo /api/public porque la app
 * lo envia sin sesion.
 */
@RestController
@RequestMapping("/api/public/encuestas/incertidumbre")
@RequiredArgsConstructor
public class SurveyController {

    private final UncertaintySurveyService surveyService;

    @PostMapping
    public ResponseEntity<SurveyResponse> guardar(@Valid @RequestBody CreateSurveyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(surveyService.save(request));
    }

    @GetMapping
    public ResponseEntity<List<SurveyResponse>> listar() {
        return ResponseEntity.ok(surveyService.findAll());
    }
}
