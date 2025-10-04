package app_jwt.auth_service.domain.entity;

import app_jwt.auth_service.domain.enums.EstadoRuta;
import app_jwt.auth_service.infra.security.SecurityUtils.EmpresaAware;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rutas", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"codigo", "empresa_id"})
})
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Route implements EmpresaAware {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(length = 200)
    private String descripcion;

    @Column(nullable = false, length = 20)
    private String codigo;

    @Column(length = 80)
    private String origen;

    @Column(length = 80)
    private String destino;

    private String colorHex;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String polyline;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EstadoRuta estado = EstadoRuta.ACTIVA;

    @Builder.Default
    private Boolean activo = true;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Column(name = "fecha_creacion")
    @Builder.Default
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @OneToMany(mappedBy = "rutaAsignada", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<Bus> buses = new ArrayList<>();

    @PreUpdate
    private void preUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    @Override
    public Long getEmpresaId() {
        return this.empresaId;
    }
}