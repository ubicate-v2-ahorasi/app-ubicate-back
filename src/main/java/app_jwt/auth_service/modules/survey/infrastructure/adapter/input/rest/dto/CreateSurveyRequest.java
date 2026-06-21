package app_jwt.auth_service.modules.survey.infrastructure.adapter.input.rest.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Cuerpo del envio del cuestionario de incertidumbre desde la app.
 * Las claves coinciden con las que ya usa la app: question1..question4.
 */
@Data
public class CreateSurveyRequest {

    @NotNull
    @Min(1)
    @Max(5)
    private Integer question1;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer question2;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer question3;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer question4;
}
