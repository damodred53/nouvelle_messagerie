// package com.alibou.websocket.chat.Controlleur;

// import java.util.List;
// import java.util.stream.Collectors;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.RequestParam;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;

// import com.alibou.websocket.chat.Models.ChatMessage;
// import com.alibou.websocket.chat.Repository.ChatMessageRepository;
// import com.alibou.websocket.chat.Response.ChatMessageResponse;

// import lombok.RequiredArgsConstructor;
// import lombok.extern.log4j.Log4j2;

// @RestController
// @RequestMapping("/api/chat")
// @RequiredArgsConstructor
// @Log4j2
// public class MessageControlleur {

// @Autowired
// private MessageRepository MessageRepository;

// /**
// * Retrieve all chat messages for a given conversationId.
// *
// * @param conversationId The unique identifier of the conversation.
// * @return List of chat messages as responses.
// */
// @GetMapping("/messages")
// public List<ChatMessageResponse> getMessagesByConversationId(@RequestParam
// String conversationId) {
// log.debug("Fetching messages for conversationId: {}", conversationId);

// // Retrieve messages from the repository
// List<ChatMessage> messages =
// chatMessageRepository.findByConversationId(conversationId);

// // Convert to response objects
// return messages.stream()
// .map(this::convertToResponse)
// .collect(Collectors.toList());
// }

// private ChatMessageResponse convertToResponse(ChatMessage message) {
// return ChatMessageResponse.builder()
// .id(message.getId())
// .content(message.getContent())
// .sender(message.getSender())
// .recipient(message.getRecipient())
// .date(message.getDate())
// .time(message.getTime())
// .conversationId(message.getConversationId())
// .build();
// }
// }
