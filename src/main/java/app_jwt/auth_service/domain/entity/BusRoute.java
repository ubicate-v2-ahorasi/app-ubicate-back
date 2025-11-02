package app_jwt.auth_service.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "bus_routes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String routeName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String polyline;

    @Column(nullable = false)
    private String routeId;

    @Column
    private Double averageSpeed;

    @Column
    private Boolean isActive = true;
}