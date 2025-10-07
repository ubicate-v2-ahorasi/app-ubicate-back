// src/main/java/app_jwt/auth_service/controller/FirebaseQuickTestController.java
package app_jwt.auth_service.controller;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/public") // ya está permitido en tu SecurityConfig
@RequiredArgsConstructor
@Slf4j
public class FirebaseQuickTestController {

    private final FirebaseDatabase firebaseDatabase; // inyecta la DB configurada

    @PostMapping("/firebase-test")
    public ResponseEntity<?> writeSimple(@RequestParam(defaultValue = "postman") String by) {
        if (firebaseDatabase == null) {
            return ResponseEntity.status(503).body(Map.of(
                    "ok", false,
                    "error", "Firebase no inicializado (FirebaseDatabase == null)"
            ));
        }

        String id = UUID.randomUUID().toString();
        DatabaseReference ref = firebaseDatabase.getReference("test/simple/" + id);

        Map<String, Object> payload = Map.of(
                "id", id,
                "by", by,
                "ts", System.currentTimeMillis()
        );

        // Escritura real
        ref.setValue(payload, (err, _r) -> {
            if (err != null) log.error("Error Firebase: {}", err.getMessage());
            else log.info("Escrito en /test/simple/{}", id);
        });

        return ResponseEntity.ok(Map.of(
                "ok", true,
                "path", "/test/simple/" + id,
                "payload", payload
        ));
    }
}
