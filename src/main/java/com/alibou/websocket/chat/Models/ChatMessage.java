package com.alibou.websocket.chat.Models;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;
    private String sender;
    private String recipient;

    @Column(name = "date", columnDefinition = "DATE")
    private LocalDate date;

    @Column(name = "time", columnDefinition = "TIME")
    private LocalTime time;

    @Column(nullable = false)
    private String conversationId;
//définis les propriétés d'un message chat
    public ChatMessage(String sender, String recipient, String content, LocalDate date, LocalTime time) {
        this.sender = sender;
        this.recipient = recipient;
        this.content = content;
        this.date = date;
        this.time = time;
        this.conversationId = generateConversationId(sender, recipient);
    }
// génère une conversation Id pour chaque message
    private String generateConversationId(String sender, String recipient) {
        // Ensure consistent ordering to guarantee a unique ID
        return (sender.compareToIgnoreCase(recipient) < 0) ? sender + "_" + recipient : recipient + "_" + sender;
    }
}
