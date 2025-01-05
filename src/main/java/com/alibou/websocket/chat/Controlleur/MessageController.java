
package com.alibou.websocket.chat.Controlleur;

import com.alibou.websocket.chat.Models.ChatMessage;
import com.alibou.websocket.chat.Repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private MessageRepository messageRepository;

    // Endpoint pour récupérer les messages par conversationId
    @GetMapping("/by-conversation")
    public ResponseEntity<List<ChatMessage>> getMessagesByConversationId(
            @RequestParam("conversationId") String conversationId) {
        // Récupérer les messages en utilisant le repository
        List<ChatMessage> messages = messageRepository.findByConversationIdOrderByDateAscTimeAsc(conversationId);

        // Vérifier si des messages ont été trouvés
        if (messages.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(messages);
    }

}
