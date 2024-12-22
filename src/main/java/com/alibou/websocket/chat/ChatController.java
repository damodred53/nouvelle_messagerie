package com.alibou.websocket.chat;

import java.time.LocalDate;
import java.time.LocalTime;
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

import com.alibou.websocket.chat.Models.ChatMessage;
import com.alibou.websocket.chat.Models.Utilisateur;
import com.alibou.websocket.chat.Repository.MessageRepository;
import com.alibou.websocket.chat.Repository.UtilisateurRepository;

@Controller
public class ChatController {

    // Injection de SimpMessagingTemplate pour envoyer des messages via WebSocket
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

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
            System.out.println("recipient: " + recipient);
            saveMessage(chatMessage);
            // Envoyer le message uniquement au destinataire
            String destination = "/queue/private/" + recipient;
            messagingTemplate.convertAndSendToUser(recipient, destination, chatMessage);
        }
    }

    // Sauvegarde d'un message dans la base de données
    private void saveMessage(ChatMessage chatMessage) {
        // Construire l'entité Message à partir de ChatMessage
        ChatMessage message = new ChatMessage(
                chatMessage.getSender(),
                chatMessage.getRecipient(),
                chatMessage.getContent(),
                LocalDate.now(), // Date du message
                LocalTime.now() // Heure du message
        );
        // Sauvegarder dans la base de données
        messageRepository.save(message);
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(
            @Payload ChatMessage chatMessage,
            SimpMessageHeaderAccessor headerAccessor) {
        // Ajouter l'utilisateur à la session et à la liste des utilisateurs actifs
        String username = chatMessage.getSender();
        LocalDate dateUserName = chatMessage.getDate();
        LocalTime timeUserName = chatMessage.getTime();
        headerAccessor.getSessionAttributes().put("username", username);
        activeUsers.add(username);

        saveUser(username);
        // Retourner un message de bienvenue
        return ChatMessage.builder()
                .type(MessageType.JOIN)
                .sender(username)
                .date(dateUserName)
                .time(timeUserName)
                .content("est dans la conversation !!")
                .build();
    }

    private void saveUser(String username) {

        Utilisateur userToSave = new Utilisateur();
        userToSave.setUsername(username);

        utilisateurRepository.save(userToSave);
        // Construire l'entité Message à partir de ChatMessage

    }

    @MessageMapping("/chat.removeUser")
    @SendTo("/topic/public")
    public ChatMessage removeUser(
            @Payload ChatMessage chatMessage,
            SimpMessageHeaderAccessor headerAccessor) {
        System.out.println(chatMessage);
        // Supprimer l'utilisateur de la liste des utilisateurs actifs
        String username = chatMessage.getSender();
        LocalDate dateUserName = chatMessage.getDate();
        LocalTime timeUserName = chatMessage.getTime();
        activeUsers.remove(username);

        // Retourner un message indiquant que l'utilisateur a quitté
        return ChatMessage.builder()
                .type(MessageType.LEAVE)
                .sender(username)
                .date(dateUserName)
                .time(timeUserName)
                .content("has left the chat!")
                .build();
    }
}
