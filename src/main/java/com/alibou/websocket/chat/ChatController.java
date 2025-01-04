package com.alibou.websocket.chat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.alibou.websocket.chat.Models.ChatMessage;
import com.alibou.websocket.chat.Models.Log;
import com.alibou.websocket.chat.Models.Utilisateur;
import com.alibou.websocket.chat.Repository.LogRepository;
import com.alibou.websocket.chat.Repository.MessageRepository;
import com.alibou.websocket.chat.Repository.UtilisateurRepository;
import com.alibou.websocket.chat.service.LogService;

@Controller
public class ChatController {

    // Injection de SimpMessagingTemplate pour envoyer des messages via WebSocket
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private LogService logService;

    @Autowired
    private LogRepository logRepository;

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
    public void sendPrivateMessage(@Payload ChatMessage chatMessage) {
        String sender = chatMessage.getSender().toLowerCase();
        String recipient = chatMessage.getRecipient().toLowerCase();

        if (recipient != null && sender != null) {
            // Générer l'ID de la conversation (topic privé)
            String conversationId = generateConversationId(sender, recipient);

            // Logique de débogage pour vérifier l'ID généré
            System.out.println("Sending message to conversationId: " + conversationId);

            // Construire une représentation lisible du message
            String logMessage = String.format("Content: %s",
                    chatMessage.getContent());

            logService.insertLog(logMessage, "INFO", "ChatController", sender);

            // Enregistrer le message dans une autre logique si nécessaire
            saveMessage(chatMessage);

            // Envoi du message au topic spécifique à la conversation
            messagingTemplate.convertAndSend("/topic/" + conversationId, chatMessage);
        }
    }

    private String generateConversationId(String sender, String recipient) {
        // Générer l'ID de la conversation de manière alphabétique pour éviter les
        // doublons
        String conversationId = (sender.compareTo(recipient) < 0) ? sender + "_" + recipient : recipient + "_" + sender;
        return conversationId.toLowerCase();
    }

    // Sauvegarde d'un message dans la base de données
    private void saveMessage(ChatMessage chatMessage) {
        // Construire l'entité Message à partir de ChatMessage
        ChatMessage message = new ChatMessage(
                chatMessage.getSender().toLowerCase(),
                chatMessage.getRecipient().toLowerCase(),
                chatMessage.getContent(),
                LocalDate.now(), // Date du message
                LocalTime.now() // Heure du message
        );
        // Sauvegarder dans la base de données
        messageRepository.save(message);
    }

    @MessageMapping("/chat.addUser/{conversationId}")
    public void addUser(
            @Payload ChatMessage chatMessage,
            @DestinationVariable String conversationId,
            SimpMessageHeaderAccessor headerAccessor) {

        System.out.println(chatMessage);

        // Ajouter l'utilisateur à la session et à la liste des utilisateurs actifs
        String username = chatMessage.getSender();

        LocalDate dateUserName = chatMessage.getDate();
        LocalTime timeUserName = chatMessage.getTime();
        headerAccessor.getSessionAttributes().put("username", username);
        activeUsers.add(username.toLowerCase());

        if (!isUserAlreadySaved(username.toLowerCase())) {

            logService.insertLog("L'utilisateur " + username + " est enregistré en base de données", "INFO",
                    "ChatController", "Controller");

            saveUser(username.toLowerCase());
        } else {

            logService.insertLog(
                    "L'utilisateur " + username + " est déjà présent en base de données, enregistrmeent annulé",
                    "WARNING", "ChatController", "Controller");

            System.out.println("L'utilisateur existe déjà dans la base de données");
        }

        // Construire un message de bienvenue
        ChatMessage welcomeMessage = ChatMessage.builder()
                .type(MessageType.JOIN)
                .sender(username)
                .date(dateUserName)
                .time(timeUserName)
                .content("est dans la conversation !!")
                .build();

        // Envoyer ce message uniquement au topic spécifique à la conversation
        messagingTemplate.convertAndSend("/topic/" + conversationId, welcomeMessage);
    }

    private void saveUser(String username) {

        Utilisateur userToSave = new Utilisateur();
        userToSave.setUsername(username);

        utilisateurRepository.save(userToSave);
        // Construire l'entité Message à partir de ChatMessage

    }

    @MessageMapping("/chat.removeUser/{conversationId}")
    // @SendTo("/topic/public")
    public void removeUser(
            @Payload ChatMessage chatMessage,
            SimpMessageHeaderAccessor headerAccessor,
            @DestinationVariable String conversationId) {
        System.out.println(chatMessage);
        // Supprimer l'utilisateur de la liste des utilisateurs actifs
        String username = chatMessage.getSender();
        LocalDate dateUserName = chatMessage.getDate();
        LocalTime timeUserName = chatMessage.getTime();
        activeUsers.remove(username);

        logService.insertLog("L'utilisateur " + username + " a quitté la conversation " + conversationId, "INFO",
                "ChatController", "Controller");

        // Retourner un message indiquant que l'utilisateur a quitté
        ChatMessage welcomeMessage = ChatMessage.builder()
                .type(MessageType.JOIN)
                .sender(username)
                .date(dateUserName)
                .time(timeUserName)
                .content("a quitté la conversation !!")
                .build();

        // Envoyer ce message uniquement au topic spécifique à la conversation
        messagingTemplate.convertAndSend("/topic/" + conversationId, welcomeMessage);
    }

    private boolean isUserAlreadySaved(String username) {
        // Implémentez une méthode pour vérifier si l'utilisateur existe dans la base de
        // données
        return utilisateurRepository.existsByUsername(username); // Exemple avec un repository JPA
    }
}
