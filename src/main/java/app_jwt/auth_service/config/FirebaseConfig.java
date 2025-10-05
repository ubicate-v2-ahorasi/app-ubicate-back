// src/main/java/app_jwt/auth_service/config/FirebaseConfig.java
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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.credentials.path:}")
    private String credentialsPath;

    @Value("${firebase.database.url}")
    private String databaseUrl;

    @Bean
    public FirebaseApp initializeFirebase() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {

            if (credentialsPath == null || credentialsPath.trim().isEmpty()) {
                log.warn("Firebase credentials not found - Firebase will not be initialized");
                return null;
            }

            try {
                InputStream serviceAccount;

                // Intentar cargar desde classpath primero
                try {
                    serviceAccount = new ClassPathResource("firebase-service-account.json").getInputStream();
                    log.info("Cargando credenciales Firebase desde classpath");
                } catch (Exception e) {
                    // Si no está en classpath, intentar desde ruta absoluta
                    serviceAccount = new FileInputStream(credentialsPath);
                    log.info("Cargando credenciales Firebase desde: {}", credentialsPath);
                }

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .setDatabaseUrl(databaseUrl)
                        .build();

                FirebaseApp app = FirebaseApp.initializeApp(options);
                log.info("Firebase inicializado correctamente");
                return app;

            } catch (Exception e) {
                log.error("Error al inicializar Firebase: {}", e.getMessage());
                log.warn("Firebase will not be available - continuing without Firebase");
                return null;
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