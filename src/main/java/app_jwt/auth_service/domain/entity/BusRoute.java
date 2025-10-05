package app_jwt.auth_service.model;

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
    private String polyline; // Polyline encoded de Google Maps

    @Column(nullable = false)
    private String routeId; // ID único de la ruta

    @Column
    private Double averageSpeed; // Velocidad promedio en km/h

    @Column
    private Boolean isActive = true;
}