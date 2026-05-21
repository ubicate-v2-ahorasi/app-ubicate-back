package app_jwt.auth_service.modules.bus.infrastructure.adapter.output.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "bus_location_history", indexes = {
    @Index(name = "idx_bus_id", columnList = "bus_id"),
    @Index(name = "idx_timestamp", columnList = "timestamp"),
    @Index(name = "idx_empresa_timestamp", columnList = "empresa_id, timestamp")
})
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BusLocationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bus_id", nullable = false)
    private Long busId;

    @Column(nullable = false, length = 10)
    private String placa;

    @Column(nullable = false)
    private Double latitud;

    @Column(nullable = false)
    private Double longitud;

    @Column
    private Double velocidad;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Column(name = "ruta_id")
    private Long rutaId;

    @Column(name = "timestamp", nullable = false, length = 30)
    private String timestamp;

    @Column(name = "fecha_registro")
    @Builder.Default
    private LocalDateTime fechaRegistro = LocalDateTime.now();
}