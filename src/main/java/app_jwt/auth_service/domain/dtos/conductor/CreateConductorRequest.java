package app_jwt.auth_service.domain.dtos.conductor;

import app_jwt.auth_service.domain.enums.CategoriaLicencia;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateConductorRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @JsonProperty("nombre")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @JsonProperty("apellido")
    private String apellido;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Email inválido")
    @JsonProperty("email")
    private String email;

    @Pattern(regexp = "\\d{8}", message = "DNI debe tener 8 dígitos")
    @JsonProperty("dni")
    private String dni;

    @NotBlank(message = "El teléfono es obligatorio")
    @JsonProperty("telefono")
    private String telefono;

    @NotBlank(message = "El número de licencia es obligatorio")
    @JsonProperty("numeroLicencia")
    private String numeroLicencia;

    @NotNull(message = "La categoría es obligatoria")
    @JsonProperty("categoriaLicencia")
    private CategoriaLicencia categoriaLicencia;

    @NotNull(message = "La fecha de vencimiento es obligatoria")
    @Future(message = "La fecha debe ser futura")
    @JsonProperty("fechaVencimientoLicencia")
    private LocalDate fechaVencimientoLicencia;

    @JsonProperty("busAsignadoId")
    private Long busAsignadoId;
}