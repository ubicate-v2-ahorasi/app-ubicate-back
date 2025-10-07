package app_jwt.auth_service.domain.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.database.*;
import app_jwt.auth_service.domain.entity.Bus;
import app_jwt.auth_service.domain.entity.Usuario;
import app_jwt.auth_service.domain.entity.Route;
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
        String token = FirebaseAuth.getInstance().createCustomToken(String.valueOf(usuario.getId()), claims);
        log.info("Token Firebase generado para usuario: {}", usuario.getId());
        return token;
    }

    private DatabaseReference busRef(Long empresaId, Long busId) {
        return firebaseDatabase.getReference()
                .child("empresas")
                .child(String.valueOf(empresaId))
                .child("buses")
                .child(String.valueOf(busId));
    }

    public void upsertBus(Bus bus) {
        try {
            DatabaseReference ref = busRef(bus.getEmpresaId(), bus.getId());
            Map<String, Object> busData = new HashMap<>();
            busData.put("id", bus.getId());
            busData.put("placa", bus.getPlaca());
            busData.put("modelo", bus.getModelo());
            busData.put("estado", bus.getEstado().name());
            busData.put("rutaId", bus.getRutaAsignada() != null ? bus.getRutaAsignada().getId() : null);
            busData.put("rutaNombre", bus.getRutaAsignada() != null ? bus.getRutaAsignada().getNombre() : null);
            busData.put("latitud", bus.getLatitud());
            busData.put("longitud", bus.getLongitud());
            busData.put("velocidad", bus.getVelocidad() != null ? bus.getVelocidad() : 0.0);
            busData.put("timestamp", System.currentTimeMillis());
            busData.put("activo", Boolean.TRUE.equals(bus.getActivo()));
            ref.updateChildren(busData, (error, r) -> {
                if (error != null) log.error("Error upsert bus {} en Firebase: {}", bus.getId(), error.getMessage());
                else log.info("Upsert bus {} OK en Firebase", bus.getId());
            });
        } catch (Exception e) {
            log.error("Excepción upsertBus Firebase: {}", e.getMessage(), e);
        }
    }

    public void updateBusEstado(Long empresaId, Long busId, String nuevoEstado, boolean activo) {
        DatabaseReference ref = busRef(empresaId, busId);
        Map<String, Object> updates = new HashMap<>();
        updates.put("estado", nuevoEstado);
        updates.put("activo", activo);
        updates.put("timestamp", System.currentTimeMillis());
        ref.updateChildren(updates, (error, r) -> {
            if (error != null) log.error("Error update estado bus {} en Firebase: {}", busId, error.getMessage());
            else log.info("Estado bus {} -> {} (activo={}) OK en Firebase", busId, nuevoEstado, activo);
        });
    }

    public void updateBusRuta(Long empresaId, Long busId, Long rutaId) {
        DatabaseReference ref = busRef(empresaId, busId);
        Map<String, Object> updates = new HashMap<>();
        updates.put("rutaId", rutaId);
        updates.put("rutaNombre", rutaId != null ? "Ruta nombre aquí" : null);
        updates.put("timestamp", System.currentTimeMillis());
        ref.updateChildren(updates, (error, r) -> {
            if (error != null) log.error("Error update ruta bus {} Firebase: {}", busId, error.getMessage());
            else log.info("Ruta bus {} actualizada en Firebase", busId);
        });
    }


    public void updateBusConductor(Bus bus) {
        DatabaseReference ref = busRef(bus.getEmpresaId(), bus.getId());
        Map<String, Object> updates = new HashMap<>();
        if (bus.getConductorAsignado() != null && Boolean.TRUE.equals(bus.getConductorAsignado().getActivo())) {
            Map<String, Object> conductor = new HashMap<>();
            conductor.put("id", bus.getConductorAsignado().getId());
            conductor.put("usuarioId", bus.getConductorAsignado().getUsuario().getId());
            conductor.put("nombre", bus.getConductorAsignado().getUsuario().getNombre() + " " +
                    bus.getConductorAsignado().getUsuario().getApellido());
            conductor.put("numeroLicencia", bus.getConductorAsignado().getNumeroLicencia());
            updates.put("conductor", conductor);
        } else {
            updates.put("conductor", null);
        }
        updates.put("timestamp", System.currentTimeMillis());
        ref.updateChildren(updates, (error, r) -> {
            if (error != null) log.error("Error update conductor bus {} Firebase: {}", bus.getId(), error.getMessage());
            else log.info("Conductor de bus {} actualizado en Firebase", bus.getId());
        });
    }

    public void initializeBusLocation(Bus bus) { upsertBus(bus); }

    public void deactivateBusLocation(Long empresaId, Long busId) {
        updateBusEstado(empresaId, busId, "INACTIVO", false);
    }

    public void removeBusFromFirebase(Long empresaId, Long busId) {
        busRef(empresaId, busId).removeValue((error, r) -> {
            if (error != null) log.error("Error eliminando bus {} de Firebase: {}", busId, error.getMessage());
            else log.info("Bus {} eliminado de Firebase", busId);
        });
    }

    public CompletableFuture<Map<String, Object>> getBusLocation(Long empresaId, Long busId) {
        DatabaseReference ref = busRef(empresaId, busId);
        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> data = (Map<String, Object>) snapshot.getValue();
                    future.complete(data);
                } else future.complete(null);
            }
            @Override public void onCancelled(DatabaseError error) {
                log.error("Error obteniendo ubicación de Firebase: {}", error.getMessage());
                future.completeExceptionally(new RuntimeException(error.getMessage()));
            }
        });
        return future;
    }
}
