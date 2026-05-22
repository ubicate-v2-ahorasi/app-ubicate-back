package app_jwt.auth_service.modules.bus.infrastructure.adapter.input.rest;

import app_jwt.auth_service.modules.bus.infrastructure.adapter.output.persistence.NotificacionSenalRepository;
import app_jwt.auth_service.modules.bus.infrastructure.adapter.output.persistence.entity.NotificacionSenal;
import app_jwt.auth_service.shared.dto.ApiResponse;
import app_jwt.auth_service.shared.utils.AuthUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/senal")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('EMPRESA')")
public class SenalNotificationController {

    private final NotificacionSenalRepository notificacionSenalRepository;
    private final AuthUtils authUtils;

    @GetMapping("/notificaciones")
    public ResponseEntity<Map<String, Object>> getNotificaciones(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Long empresaId = authUtils.getEmpresaId(authentication);
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificacionSenal> notificacionesPage = notificacionSenalRepository
                .findByEmpresaIdOrderByTimestampDesc(empresaId, pageable);

        List<Map<String, Object>> notificaciones = notificacionesPage.getContent().stream()
                .map(this::toMap)
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("notificaciones", notificaciones);
        response.put("currentPage", notificacionesPage.getNumber());
        response.put("totalItems", notificacionesPage.getTotalElements());
        response.put("totalPages", notificacionesPage.getTotalPages());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/notificaciones/no-leidas")
    public ResponseEntity<List<Map<String, Object>>> getNotificacionesNoLeidas(Authentication authentication) {
        Long empresaId = authUtils.getEmpresaId(authentication);
        List<NotificacionSenal> notificaciones = notificacionSenalRepository
                .findByEmpresaIdAndLeidaFalseOrderByTimestampDesc(empresaId);

        List<Map<String, Object>> response = notificaciones.stream()
                .map(this::toMap)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/notificaciones/count")
    public ResponseEntity<Map<String, Long>> getCountNoLeidas(Authentication authentication) {
        Long empresaId = authUtils.getEmpresaId(authentication);
        Long count = notificacionSenalRepository.countByEmpresaIdAndLeidaFalse(empresaId);

        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/notificaciones/{id}/leida")
    public ResponseEntity<ApiResponse> markAsRead(
            @PathVariable Long id,
            Authentication authentication) {

        Long empresaId = authUtils.getEmpresaId(authentication);
        int updated = notificacionSenalRepository.markAsRead(id, empresaId);

        if (updated > 0) {
            return ResponseEntity.ok(new ApiResponse("Notificación marcada como leída", true));
        } else {
            return ResponseEntity.ok(new ApiResponse("Notificación no encontrada", false));
        }
    }

    @PatchMapping("/notificaciones/leer-todas")
    public ResponseEntity<ApiResponse> markAllAsRead(Authentication authentication) {
        Long empresaId = authUtils.getEmpresaId(authentication);
        int updated = notificacionSenalRepository.markAllAsRead(empresaId);

        return ResponseEntity.ok(new ApiResponse("Se marcaron " + updated + " notificaciones como leídas", true));
    }

    private Map<String, Object> toMap(NotificacionSenal n) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", n.getId());
        map.put("busId", n.getBusId());
        map.put("placa", n.getPlaca());
        map.put("tipo", n.getTipo());
        map.put("mensaje", n.getMensaje());
        map.put("latitud", n.getLatitud());
        map.put("longitud", n.getLongitud());
        map.put("timestamp", n.getTimestamp());
        map.put("leida", n.getLeida());
        return map;
    }
}