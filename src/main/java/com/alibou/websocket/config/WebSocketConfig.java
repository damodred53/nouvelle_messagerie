package com.alibou.websocket.config;

import java.time.LocalDateTime;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.alibou.websocket.chat.Models.Log;
import com.alibou.websocket.chat.Repository.LogRepository;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // private LogRepository logRepository;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").withSockJS();

        // Log de connexion WebSocket
        // Log log = Log.builder()
        // .level("INFO")
        // .logger("WebSocketConfig")
        // .message("WebSocket connection established at /ws")
        // .timestamp(LocalDateTime.now())
        // .build();
        // logRepository.save(log);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");
        // Autoriser la diffusion vers des topics privés spécifiques à chaque
        // conversation
        registry.enableSimpleBroker("/topic");
    }
}
