package app_jwt.auth_service.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.FirebaseDatabase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.credentials.path:}")
    private String credentialsPath;

    @Value("${firebase.credentials.base64:}")
    private String credentialsBase64;

    @Value("${firebase.database.url}")
    private String databaseUrl;

    @Bean
    public FirebaseApp initializeFirebase() {
        try {
            log.info("🔥 INICIANDO CONFIGURACIÓN FIREBASE");
            log.info("🔥 Database URL: {}", databaseUrl);

            GoogleCredentials credentials;

            // 🚀 PRIORIDAD 1: Base64 (HEROKU/PRODUCCIÓN)
            if (credentialsBase64 != null && !credentialsBase64.trim().isEmpty()) {
                log.info("✅ Usando credenciales desde Base64 (Heroku)");
                byte[] decodedBytes = Base64.getDecoder().decode(credentialsBase64);
                log.info("📄 Credenciales decodificadas. Tamaño: {} bytes", decodedBytes.length);

                credentials = GoogleCredentials.fromStream(
                        new ByteArrayInputStream(decodedBytes)
                );
            }
            // 🏠 PRIORIDAD 2: Archivo JSON (LOCAL)
            else if (credentialsPath != null && !credentialsPath.trim().isEmpty()) {
                log.info("✅ Usando credenciales desde archivo: {}", credentialsPath);
                credentials = GoogleCredentials.fromStream(
                        new FileInputStream(credentialsPath)
                );
            }
            else {
                throw new IllegalStateException(
                        "❌ No se encontraron credenciales de Firebase. " +
                                "Configura FIREBASE_CREDENTIALS_BASE64 (Heroku) o firebase.credentials.path (Local)"
                );
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .setDatabaseUrl(databaseUrl)
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp app = FirebaseApp.initializeApp(options);
                log.info("✅ Firebase inicializado correctamente");
                log.info("✅ Proyecto: {}", app.getOptions().getProjectId());
                log.info("✅ Database URL: {}", app.getOptions().getDatabaseUrl());
                return app;
            } else {
                log.warn("⚠️ FirebaseApp ya estaba inicializado");
                return FirebaseApp.getInstance();
            }

        } catch (Exception e) {
            log.error("❌ Error al inicializar Firebase: {}", e.getMessage(), e);
            throw new RuntimeException("Error al inicializar Firebase", e);
        }
    }

    @Bean
    public FirebaseDatabase firebaseDatabase(FirebaseApp firebaseApp) {
        try {
            FirebaseDatabase database = FirebaseDatabase.getInstance(firebaseApp);
            log.info("✅ FirebaseDatabase creado exitosamente");
            return database;
        } catch (Exception e) {
            log.error("❌ Error al crear FirebaseDatabase: {}", e.getMessage(), e);
            throw new RuntimeException("Error al crear FirebaseDatabase", e);
        }
    }
}