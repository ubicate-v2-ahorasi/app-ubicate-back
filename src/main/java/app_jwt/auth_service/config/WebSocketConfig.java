package app_jwt.auth_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

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
                .setAllowedOrigins("http://localhost:4200", "http://localhost:8100", "https://ubicate.codlyp.website")
                .withSockJS();

        registry.addEndpoint("/ws-tracking")
                .setAllowedOrigins("http://localhost:4200", "http://localhost:8100", "https://ubicate.codlyp.website");
    }
}
