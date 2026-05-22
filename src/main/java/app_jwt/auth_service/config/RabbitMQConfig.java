package app_jwt.auth_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_GPS = "gps.events";
    public static final String EXCHANGE_NOTIFICATIONS = "notifications.events";

    public static final String QUEUE_LOCATION_HISTORY = "gps.location.history";
    public static final String QUEUE_NOTIFICATIONS = "notifications.push";

    public static final String ROUTING_KEY_LOCATION = "gps.location.update";
    public static final String ROUTING_KEY_HISTORY = "gps.location.history";
    public static final String ROUTING_KEY_NOTIFICATION = "notification.push";

    @Bean
    public TopicExchange gpsExchange() {
        return new TopicExchange(EXCHANGE_GPS);
    }

    @Bean
    public TopicExchange notificationsExchange() {
        return new TopicExchange(EXCHANGE_NOTIFICATIONS);
    }

    @Bean
    public Queue locationHistoryQueue() {
        return QueueBuilder.durable(QUEUE_LOCATION_HISTORY)
                .withArgument("x-message-ttl", 86400000)
                .build();
    }

    @Bean
    public Queue notificationsQueue() {
        return QueueBuilder.durable(QUEUE_NOTIFICATIONS).build();
    }

    @Bean
    public Binding locationHistoryBinding(Queue locationHistoryQueue, TopicExchange gpsExchange) {
        return BindingBuilder.bind(locationHistoryQueue)
                .to(gpsExchange)
                .with("gps.location.*");
    }

    @Bean
    public Binding notificationsBinding(Queue notificationsQueue, TopicExchange notificationsExchange) {
        return BindingBuilder.bind(notificationsQueue)
                .to(notificationsExchange)
                .with("notification.*");
    }

    @Bean
    public Binding senalNotificationsBinding(Queue notificationsQueue, TopicExchange notificationsExchange) {
        return BindingBuilder.bind(notificationsQueue)
                .to(notificationsExchange)
                .with("senial.alerta");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}