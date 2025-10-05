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
import java.io.IOException;
import java.util.Base64;

@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.credentials.base64:}")
    private String credentialsBase64;

    @Value("${firebase.database.url}")
    private String databaseUrl;

    @Bean
    public FirebaseApp initializeFirebase() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {

            if (credentialsBase64 == null || credentialsBase64.trim().isEmpty()) {
                log.warn("Firebase credentials not found - Firebase will not be initialized");
                return null;
            }

            try {
                // Decodificar las credenciales desde base64
                byte[] decodedCredentials = Base64.getDecoder().decode(credentialsBase64);
                ByteArrayInputStream credentialsStream = new ByteArrayInputStream(decodedCredentials);

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(credentialsStream))
                        .setDatabaseUrl(databaseUrl)
                        .build();

                FirebaseApp app = FirebaseApp.initializeApp(options);
                log.info("Firebase inicializado correctamente");
                return app;

            } catch (Exception e) {
                log.error("Error al inicializar Firebase: {}", e.getMessage());
                throw new IOException("Failed to initialize Firebase", e);
            }
        }
        return FirebaseApp.getInstance();
    }

    @Bean
    public FirebaseDatabase firebaseDatabase(FirebaseApp firebaseApp) {
        if (firebaseApp == null) {
            log.warn("FirebaseApp is null - FirebaseDatabase will not be available");
            return null;
        }
        return FirebaseDatabase.getInstance(firebaseApp);
    }
}