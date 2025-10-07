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

    @Value("${firebase.credentials.path:src/main/resources/firebase-service-account.json}")
    private String credentialsPath;

    @Value("${firebase.database.url:https://ubicate-4271d-default-rtdb.firebaseio.com}")
    private String databaseUrl;

    @Bean
    public FirebaseApp initializeFirebase() {
        log.info("🔥 INICIANDO CONFIGURACIÓN FIREBASE");
        log.info("🔥 credentialsPath: {}", credentialsPath);
        log.info("🔥 databaseUrl: {}", databaseUrl);

        try {
            if (FirebaseApp.getApps().isEmpty()) {
                InputStream serviceAccount;

                try {
                    // Intentar desde classpath primero
                    serviceAccount = new ClassPathResource("firebase-service-account.json").getInputStream();
                    log.info("✅ Credenciales cargadas desde classpath");
                } catch (Exception e) {
                    // Intentar desde ruta absoluta
                    serviceAccount = new FileInputStream(credentialsPath);
                    log.info("✅ Credenciales cargadas desde: {}", credentialsPath);
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
            log.warn("🔄 Continuando sin Firebase - funcionará en modo MySQL solamente");
            return null;
        }
    }

    @Bean
    public FirebaseDatabase firebaseDatabase(FirebaseApp firebaseApp) {
        log.info("🔥 CREANDO FIREBASE DATABASE");

        if (firebaseApp == null) {
            log.warn("❌ FirebaseApp es null - FirebaseDatabase NO estará disponible");
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