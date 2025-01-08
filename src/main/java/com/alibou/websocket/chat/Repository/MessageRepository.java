package com.alibou.websocket.chat.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.alibou.websocket.chat.Models.ChatMessage;
// Simplifie les requêtes
@Repository
public interface MessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByConversationIdOrderByDateAscTimeAsc(String conversationId);
}
