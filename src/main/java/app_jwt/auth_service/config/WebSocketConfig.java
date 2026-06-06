package app_jwt.auth_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final String[] ALLOWED_ORIGINS = {
            "http://localhost:4200",
            "http://localhost:8100",
            "http://10.0.2.2:8080",
            "https://geo.ubicate.page",
            "https://ubicate.codlyp.website"
    };

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Habilitar un broker simple para destinos prefijados con /topic
        config.enableSimpleBroker("/topic");
        // Prefijo para los mensajes que van de cliente a servidor (@MessageMapping)
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-tracking")
                .setAllowedOrigins(ALLOWED_ORIGINS)
                .withSockJS();

        registry.addEndpoint("/ws-tracking")
                .setAllowedOrigins(ALLOWED_ORIGINS);
    }
}
