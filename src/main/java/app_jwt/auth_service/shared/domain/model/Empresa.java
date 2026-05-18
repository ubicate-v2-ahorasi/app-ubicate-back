package app_jwt.auth_service.shared.domain.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "empresas")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Empresa {

    @Id
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 11)
    private String ruc;

    @Column(length = 255)
    private String logo;

    @Column(length = 20)
    private String telefono;

    @Column(length = 200)
    private String direccion;

    @Builder.Default
    private Boolean activo = true;

    @Column(name = "fecha_creacion")
    @Builder.Default
    private LocalDateTime fechaCreacion = LocalDateTime.now();
}
