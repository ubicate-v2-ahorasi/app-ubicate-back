package app_jwt.auth_service.modules.auth.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Formato de email inválido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    @JsonProperty("email")
    private String email;

    @NotBlank(message = "El nombre del representante es obligatorio")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    @JsonProperty("nombre")
    private String nombre;

    @NotBlank(message = "El apellido del representante es obligatorio")
    @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
    @JsonProperty("apellido")
    private String apellido;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^[+]?[0-9]{9,15}$", message = "Formato de teléfono inválido")
    @JsonProperty("telefono")
    private String telefono;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "La contraseña debe contener al menos: 1 mayúscula, 1 minúscula, 1 número y 1 carácter especial")
    @JsonProperty("password")
    private String password;

    @NotBlank(message = "El DNI del representante es obligatorio")
    @Pattern(regexp = "\\d{8}", message = "El DNI debe tener 8 dígitos")
    @JsonProperty("dni")
    private String dni;

    @NotBlank(message = "El nombre de la empresa es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre de la empresa debe tener entre 3 y 100 caracteres")
    @JsonProperty("nombreEmpresa")
    private String nombreEmpresa;

    @NotBlank(message = "El RUC es obligatorio")
    @Pattern(regexp = "\\d{11}", message = "El RUC debe tener 11 dígitos")
    @JsonProperty("ruc")
    private String ruc;

    @Size(max = 200, message = "La dirección no puede exceder 200 caracteres")
    @JsonProperty("direccion")
    private String direccion;

    @Size(max = 255, message = "La URL del logo no puede exceder 255 caracteres")
    @JsonProperty("logo")
    private String logo;
}
