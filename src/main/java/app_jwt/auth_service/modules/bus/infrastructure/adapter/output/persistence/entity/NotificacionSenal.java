package app_jwt.auth_service.modules.bus.infrastructure.adapter.output.persistence.entity;

import app_jwt.auth_service.modules.bus.domain.model.EstadoSenal;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notificacion_senal", indexes = {
    @Index(name = "idx_empresa_leida", columnList = "empresa_id, leida"),
    @Index(name = "idx_timestamp", columnList = "timestamp")
})
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificacionSenal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bus_id", nullable = false)
    private Long busId;

    @Column(nullable = false, length = 10)
    private String placa;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSenal tipo;

    @Column(nullable = false, length = 500)
    private String mensaje;

    @Column(length = 50)
    private Double latitud;

    @Column(length = 50)
    private Double longitud;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private Boolean leida = false;
}