package app_jwt.auth_service.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.FirebaseDatabase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;

@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.credentials.base64:}")
    private String credentialsBase64;

    @Value("${firebase.credentials.path:}")
    private String credentialsPath;

    @Value("${firebase.database.url}")
    private String databaseUrl;

    @Bean
    public FirebaseApp initializeFirebase() {
        log.info("🔥 INICIANDO CONFIGURACIÓN FIREBASE");
        log.info("🔥 databaseUrl: {}", databaseUrl);

        try {
            if (FirebaseApp.getApps().isEmpty()) {
                InputStream serviceAccount = null;

                // PRIORIDAD 1: Base64 (HEROKU)
                if (credentialsBase64 != null && !credentialsBase64.isEmpty()) {
                    try {
                        byte[] decodedBytes = Base64.getDecoder().decode(credentialsBase64);
                        serviceAccount = new ByteArrayInputStream(decodedBytes);
                        log.info("✅ Credenciales cargadas desde Base64 (Heroku)");
                    } catch (Exception e) {
                        log.error("❌ Error decodificando Base64: {}", e.getMessage());
                    }
                }

                // PRIORIDAD 2: Classpath (LOCAL)
                if (serviceAccount == null) {
                    try {
                        serviceAccount = new ClassPathResource("firebase-service-account.json").getInputStream();
                        log.info("✅ Credenciales cargadas desde classpath (Local)");
                    } catch (Exception e) {
                        log.warn("⚠️ No se encontró firebase-service-account.json en classpath");
                    }
                }

                // Si no hay credenciales, devolver null
                if (serviceAccount == null) {
                    log.warn("❌ No se encontraron credenciales de Firebase");
                    log.warn("🔄 Continuando sin Firebase");
                    return null;
                }

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .setDatabaseUrl(databaseUrl)
                        .build();

                FirebaseApp app = FirebaseApp.initializeApp(options);
                log.info("✅ Firebase inicializado correctamente");
                return app;
            }
            return FirebaseApp.getInstance();
        } catch (Exception e) {
            log.error("❌ Error al inicializar Firebase: {}", e.getMessage());
            log.warn("🔄 Continuando sin Firebase");
            return null;
        }
    }

    @Bean
    public FirebaseDatabase firebaseDatabase(FirebaseApp firebaseApp) {
        if (firebaseApp == null) {
            log.warn("❌ FirebaseApp es null - FirebaseDatabase NO disponible");
            return null;
        }

        try {
            FirebaseDatabase db = FirebaseDatabase.getInstance(firebaseApp);
            log.info("✅ FirebaseDatabase creado exitosamente");
            return db;
        } catch (Exception e) {
            log.error("❌ Error creando FirebaseDatabase: {}", e.getMessage());
            return null;
        }
    }
}