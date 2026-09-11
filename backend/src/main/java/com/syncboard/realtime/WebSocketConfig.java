// Stage 6 — Cache + realtime
package com.syncboard.realtime;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.syncboard.config.SyncBoardProperties;

/**
 * STOMP wiring for realtime board/comment events (01-CONTRACT.md §4). Endpoint, broker
 * prefix, app prefix and allowed origins all come from {@code syncboard.*} — never
 * hardcoded — so the frontend and backend stay driven by the same {@code application.yml}.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final SyncBoardProperties properties;
    private final ObjectMapper objectMapper;

    public WebSocketConfig(SyncBoardProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker(properties.realtime().brokerPrefix());
        registry.setApplicationDestinationPrefixes(properties.realtime().appPrefix());
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(properties.realtime().stompEndpoint())
                .setAllowedOrigins(properties.corsAllowedOrigins().toArray(String[]::new))
                .withSockJS();
    }

    /**
     * Reuses the app's shared {@link ObjectMapper} bean instead of a fresh default one, so
     * the {@code TicketStatus}/{@code Priority}/{@code LinkPlatform} wire-value
     * (de)serializers A3 registers via {@code api.jackson.DomainEnumModule} apply to STOMP
     * payloads too — REST, GraphQL and WebSocket JSON all stay identical.
     */
    @Override
    public boolean configureMessageConverters(List<MessageConverter> messageConverters) {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(objectMapper);
        messageConverters.add(converter);
        return false;
    }
}
