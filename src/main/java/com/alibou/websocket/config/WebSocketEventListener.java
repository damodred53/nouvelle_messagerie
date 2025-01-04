package com.alibou.websocket.config;

import com.alibou.websocket.chat.Models.ChatMessage;

import com.alibou.websocket.chat.MessageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final SimpMessageSendingOperations messagingTemplate;

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        String conversationId = (String) headerAccessor.getSessionAttributes().get("conversationId"); // ID de la
                                                                                                      // conversation

        if (username != null && conversationId != null) {
            LocalTime formattedTime = LocalTime.now().withSecond(0).withNano(0);
            log.info("User disconnected: {}", username);

            // Créer un message de déconnexion
            var chatMessage = ChatMessage.builder()
                    .type(MessageType.LEAVE)
                    .date(LocalDate.now())
                    .time(formattedTime)
                    .content(username + " a quitté la conversation")
                    .sender(username)
                    .build();

            // Envoyer le message de déconnexion au topic de la conversation spécifique
            messagingTemplate.convertAndSend("/topic/" + conversationId, chatMessage);

        }
    }
}
