package app_jwt.auth_service.domain.entity;

import app_jwt.auth_service.domain.enums.EstadoBus;
import app_jwt.auth_service.infra.security.SecurityUtils.EmpresaAware;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "buses", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"placa"})
})
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Bus implements EmpresaAware {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 10)
    private String placa;

    @Column(nullable = false, length = 50)
    private String modelo;

    @Column(nullable = false)
    private Integer capacidad;

    @Column(length = 4)
    private String anio;

    @Column(length = 50)
    private String color;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EstadoBus estado = EstadoBus.INACTIVO;

    @Builder.Default
    private Boolean activo = true;

    @Column(name = "fecha_creacion")
    @Builder.Default
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ruta_id")
    private Route rutaAsignada;

    @Column(name = "latitud")
    private Double latitud;

    @Column(name = "longitud")
    private Double longitud;

    @Column(name = "velocidad")
    private Double velocidad;

    @Column(name = "ultima_ubicacion")
    private LocalDateTime ultimaUbicacion;

    @OneToOne(mappedBy = "busAsignado", fetch = FetchType.LAZY)
    private Conductor conductorAsignado;

    @PreUpdate
    private void preUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    @Override
    public Long getEmpresaId() {
        return this.empresaId;
    }
}