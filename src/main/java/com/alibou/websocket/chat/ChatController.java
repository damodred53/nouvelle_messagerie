package com.alibou.websocket.chat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    // Injection de SimpMessagingTemplate pour envoyer des messages via WebSocket
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private Set<String> activeUsers = ConcurrentHashMap.newKeySet();

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(
            @Payload ChatMessage chatMessage) {
        System.out.println(chatMessage);
        return chatMessage;
    }

    @MessageMapping("/chat.getContacts")
    @SendTo("/topic/contacts")
    public List<String> getContacts(SimpMessageHeaderAccessor headerAccessor) {
        // Récupérer la liste des utilisateurs connectés (contacts)
        List<String> contacts = activeUsers.stream().collect(Collectors.toList());
        return contacts;
    }

    @MessageMapping("/chat.sendPrivateMessage")
    public void sendPrivateMessage(
            @Payload ChatMessage chatMessage,
            SimpMessageHeaderAccessor headerAccessor) {
        // Envoyer le message directement au destinataire via WebSocket
        String recipient = chatMessage.getRecipient();
        if (recipient != null) {
            // Envoyer le message uniquement au destinataire
            String destination = "/queue/private/" + recipient;
            messagingTemplate.convertAndSendToUser(recipient, destination, chatMessage);
        }
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(
            @Payload ChatMessage chatMessage,
            SimpMessageHeaderAccessor headerAccessor) {
        // Ajouter l'utilisateur à la session et à la liste des utilisateurs actifs
        String username = chatMessage.getSender();
        headerAccessor.getSessionAttributes().put("username", username);
        activeUsers.add(username);

        // Retourner un message de bienvenue
        return ChatMessage.builder()
                .type(MessageType.JOIN)
                .sender(username)
                .content("has joined the chat!")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @MessageMapping("/chat.removeUser")
    @SendTo("/topic/public")
    public ChatMessage removeUser(
            @Payload ChatMessage chatMessage,
            SimpMessageHeaderAccessor headerAccessor) {
        // Supprimer l'utilisateur de la liste des utilisateurs actifs
        String username = chatMessage.getSender();
        activeUsers.remove(username);

        // Retourner un message indiquant que l'utilisateur a quitté
        return ChatMessage.builder()
                .type(MessageType.LEAVE)
                .sender(username)
                .content("has left the chat!")
                .timestamp(LocalDateTime.now())
                .build();
    }
}
