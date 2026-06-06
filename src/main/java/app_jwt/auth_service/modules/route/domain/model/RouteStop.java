package app_jwt.auth_service.modules.route.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "route_stops", indexes = {
        @Index(name = "idx_route_stops_route_order", columnList = "route_id, orden")
})
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class RouteStop {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @Column(length = 120)
    private String nombre;

    @Column(length = 255)
    private String direccion;

    @Column(nullable = false)
    private Double latitud;

    @Column(nullable = false)
    private Double longitud;

    @Column(name = "color_hex", length = 20)
    private String colorHex;

    @Column(nullable = false)
    private Integer orden;

    @Builder.Default
    private Boolean activo = true;

    @Column(name = "fecha_creacion")
    @Builder.Default
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @PreUpdate
    private void preUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}
