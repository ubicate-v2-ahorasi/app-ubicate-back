package app_jwt.auth_service.domain.dtos.auth;

import app_jwt.auth_service.domain.entity.Usuario;
import app_jwt.auth_service.domain.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private Long id;
    private Long empresaId;
    private String nombre;
    private String apellido;
    private String dni;
    private String telefono;
    private String correo;
    private String username;
    private Role role;
    private String busId;
    private String busPlate;
    private String busNumber;

    public static UserResponse from(Usuario usuario) {
        return UserResponse.builder()
                .id(usuario.getId())
                .empresaId(usuario.getEmpresaId())
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .dni(usuario.getDni())
                .telefono(usuario.getTelefono())
                .correo(usuario.getCorreo())
                .username(usuario.getUsername())
                .role(usuario.getRole())
                .build();
    }
}