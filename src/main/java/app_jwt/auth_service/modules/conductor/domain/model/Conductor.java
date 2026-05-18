package app_jwt.auth_service.modules.conductor.domain.model;

import app_jwt.auth_service.modules.conductor.domain.model.CategoriaLicencia;
import app_jwt.auth_service.modules.conductor.domain.model.EstadoConductor;
import app_jwt.auth_service.modules.conductor.domain.model.TurnoConductor;
import app_jwt.auth_service.shared.domain.model.Usuario;
import app_jwt.auth_service.modules.bus.domain.model.Bus;
import app_jwt.auth_service.shared.utils.EmpresaAware;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "conductores", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"numero_licencia"}),
        @UniqueConstraint(columnNames = {"usuario_id"})
})
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Conductor implements EmpresaAware {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "numero_licencia", unique = true, nullable = false, length = 20)
    private String numeroLicencia;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria_licencia", nullable = false)
    private CategoriaLicencia categoriaLicencia;

    @Column(name = "fecha_vencimiento_licencia", nullable = false)
    private LocalDate fechaVencimientoLicencia;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TurnoConductor turno = TurnoConductor.MAÑANA;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EstadoConductor estado = EstadoConductor.ACTIVO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bus_asignado_id")
    private Bus busAsignado;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Builder.Default
    private Boolean activo = true;

    @Column(name = "fecha_ingreso")
    private LocalDate fechaIngreso;

    @Column(name = "fecha_creacion")
    @Builder.Default
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Column(name = "experiencia_años")
    private Integer experienciaAnios;

    @Column(name = "observaciones", length = 500)
    private String observaciones;

    @PreUpdate
    private void preUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    public boolean isLicenciaVencida() {
        return fechaVencimientoLicencia.isBefore(LocalDate.now());
    }

    public boolean isLicenciaPorVencer() {
        return fechaVencimientoLicencia.isBefore(LocalDate.now().plusDays(30));
    }

    @Override
    public Long getEmpresaId() {
        return this.empresaId;
    }
}
