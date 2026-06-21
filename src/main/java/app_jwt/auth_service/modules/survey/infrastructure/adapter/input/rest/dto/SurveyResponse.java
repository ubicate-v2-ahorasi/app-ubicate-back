package app_jwt.auth_service.modules.survey.infrastructure.adapter.input.rest.dto;

import app_jwt.auth_service.modules.survey.domain.model.UncertaintySurvey;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SurveyResponse {

    private Long id;
    private Integer question1;
    private Integer question2;
    private Integer question3;
    private Integer question4;
    private Double average;
    private String level;
    private LocalDateTime fechaCreacion;

    public static SurveyResponse from(UncertaintySurvey survey) {
        return SurveyResponse.builder()
                .id(survey.getId())
                .question1(survey.getPregunta1())
                .question2(survey.getPregunta2())
                .question3(survey.getPregunta3())
                .question4(survey.getPregunta4())
                .average(survey.getPromedio())
                .level(survey.getNivel())
                .fechaCreacion(survey.getFechaCreacion())
                .build();
    }
}
