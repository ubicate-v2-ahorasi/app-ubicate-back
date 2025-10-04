package app_jwt.auth_service.domain.dtos.empresa;

import app_jwt.auth_service.domain.entity.Empresa;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmpresaPublicResponse {
    private Long id;
    private String nombre;
    private String logo;
    private String telefono;

    public static EmpresaPublicResponse from(Empresa empresa) {
        return EmpresaPublicResponse.builder()
                .id(empresa.getId())
                .nombre(empresa.getNombre())
                .logo(empresa.getLogo())
                .telefono(empresa.getTelefono())
                .build();
    }
}