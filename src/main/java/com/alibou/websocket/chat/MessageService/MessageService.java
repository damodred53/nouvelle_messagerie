package com.alibou.websocket.chat.MessageService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibou.websocket.chat.Models.ChatMessage;
import com.alibou.websocket.chat.Repository.MessageRepository;

@Service
public class MessageService {
    @Autowired
    private MessageRepository messageRepository;

    public List<ChatMessage> getConversationMessages(String user1, String user2) {
        // Générer l'ID unique de la conversation
        String conversationId = (user1.compareTo(user2) < 0) ? user1 + "_" + user2 : user2 + "_" + user1;
        return messageRepository.findByConversationIdOrderByDateAscTimeAsc(conversationId);
    }
}
