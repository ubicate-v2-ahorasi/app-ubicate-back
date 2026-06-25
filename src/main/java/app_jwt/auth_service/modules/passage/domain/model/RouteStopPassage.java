package app_jwt.auth_service.modules.passage.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Registro de cuando un bus paso (cruzo) por una parada de su ruta.
 * Se genera en el servidor cuando el bus reporta una ubicacion cercana a la
 * parada (radio configurado).
 */
@Entity
@Table(name = "route_stop_passages", indexes = {
        @Index(name = "idx_passage_ruta_bus", columnList = "ruta_id, bus_id, hora_cruce"),
        @Index(name = "idx_passage_ruta", columnList = "ruta_id, hora_cruce")
})
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class RouteStopPassage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ruta_id", nullable = false)
    private Long rutaId;

    @Column(name = "route_stop_id", nullable = false)
    private Long routeStopId;

    @Column(name = "bus_id", nullable = false)
    private Long busId;

    @Column(name = "placa", length = 20)
    private String placa;

    @Column(name = "orden")
    private Integer orden;

    @Column(name = "nombre_parada", length = 120)
    private String nombreParada;

    @Column(name = "hora_cruce", nullable = false)
    private LocalDateTime horaCruce;

    /** Tiempo (segundos) que tardo desde la parada anterior de este mismo bus. */
    @Column(name = "segundos_desde_anterior")
    private Integer segundosDesdeAnterior;
}
