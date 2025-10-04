package app_jwt.auth_service.domain.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.database.*;
import app_jwt.auth_service.domain.entity.Bus;
import app_jwt.auth_service.domain.entity.Usuario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class FirebaseTrackingService {

    private final FirebaseDatabase firebaseDatabase;

    public String generateFirebaseToken(Usuario usuario) throws FirebaseAuthException {
        Map<String, Object> claims = new HashMap<>();
        claims.put("empresaId", usuario.getEmpresaId());
        claims.put("role", usuario.getRole().name());
        claims.put("userId", usuario.getId());

        String token = FirebaseAuth.getInstance()
                .createCustomToken(String.valueOf(usuario.getId()), claims);

        log.info("Token Firebase generado para usuario: {}", usuario.getId());
        return token;
    }

    public void initializeBusLocation(Bus bus) {
        DatabaseReference ref = firebaseDatabase.getReference()
                .child("empresas")
                .child(String.valueOf(bus.getEmpresaId()))
                .child("buses")
                .child(String.valueOf(bus.getId()));

        Map<String, Object> busData = new HashMap<>();
        busData.put("placa", bus.getPlaca());
        busData.put("modelo", bus.getModelo());
        busData.put("estado", bus.getEstado().name());
        busData.put("rutaId", bus.getRutaAsignada() != null ? bus.getRutaAsignada().getId() : null);
        busData.put("rutaNombre", bus.getRutaAsignada() != null ? bus.getRutaAsignada().getNombre() : null);
        busData.put("latitud", bus.getLatitud());
        busData.put("longitud", bus.getLongitud());
        busData.put("velocidad", 0.0);
        busData.put("timestamp", System.currentTimeMillis());
        busData.put("activo", true);

        ref.setValue(busData, (error, ref1) -> {
            if (error != null) {
                log.error("Error inicializando bus {} en Firebase: {}", bus.getId(), error.getMessage());
            } else {
                log.info("Bus {} inicializado en Firebase", bus.getId());
            }
        });
    }

    public void deactivateBusLocation(Long empresaId, Long busId) {
        DatabaseReference ref = firebaseDatabase.getReference()
                .child("empresas")
                .child(String.valueOf(empresaId))
                .child("buses")
                .child(String.valueOf(busId));

        Map<String, Object> updates = new HashMap<>();
        updates.put("activo", false);
        updates.put("timestamp", System.currentTimeMillis());

        ref.updateChildren(updates, (error, ref1) -> {
            if (error != null) {
                log.error("Error desactivando bus {} en Firebase: {}", busId, error.getMessage());
            } else {
                log.info("Bus {} marcado como inactivo en Firebase", busId);
            }
        });
    }

    public void removeBusFromFirebase(Long empresaId, Long busId) {
        DatabaseReference ref = firebaseDatabase.getReference()
                .child("empresas")
                .child(String.valueOf(empresaId))
                .child("buses")
                .child(String.valueOf(busId));

        ref.removeValue((error, ref1) -> {
            if (error != null) {
                log.error("Error eliminando bus {} de Firebase: {}", busId, error.getMessage());
            } else {
                log.info("Bus {} eliminado de Firebase", busId);
            }
        });
    }

    public CompletableFuture<Map<String, Object>> getBusLocation(Long empresaId, Long busId) {
        DatabaseReference ref = firebaseDatabase.getReference()
                .child("empresas")
                .child(String.valueOf(empresaId))
                .child("buses")
                .child(String.valueOf(busId));

        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> data = (Map<String, Object>) snapshot.getValue();
                    future.complete(data);
                } else {
                    future.complete(null);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                log.error("Error obteniendo ubicación de Firebase: {}", error.getMessage());
                future.completeExceptionally(new RuntimeException(error.getMessage()));
            }
        });

        return future;
    }
}