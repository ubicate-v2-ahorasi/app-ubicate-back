package app_jwt.auth_service.modules.survey.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Respuesta del cuestionario de incertidumbre del transporte publico.
 * Cada fila es un envio anonimo del formulario de la app movil.
 */
@Entity
@Table(name = "encuestas_incertidumbre")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class UncertaintySurvey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pregunta1", nullable = false)
    private Integer pregunta1;

    @Column(name = "pregunta2", nullable = false)
    private Integer pregunta2;

    @Column(name = "pregunta3", nullable = false)
    private Integer pregunta3;

    @Column(name = "pregunta4", nullable = false)
    private Integer pregunta4;

    @Column(name = "promedio", nullable = false)
    private Double promedio;

    @Column(name = "nivel", nullable = false, length = 10)
    private String nivel;

    @Column(name = "fecha_creacion", nullable = false)
    @Builder.Default
    private LocalDateTime fechaCreacion = LocalDateTime.now();
}
