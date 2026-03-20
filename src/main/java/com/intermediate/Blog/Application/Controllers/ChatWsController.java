package com.intermediate.Blog.Application.Controllers;


import com.intermediate.Blog.Application.DtoLayers.MessageDto;
import com.intermediate.Blog.Application.Exception.ResourceNotFoundException;
import com.intermediate.Blog.Application.Models.Conversation;
import com.intermediate.Blog.Application.Models.User;
import com.intermediate.Blog.Application.Repositories.ConversationRepository;
import com.intermediate.Blog.Application.Repositories.UserRepo;
import com.intermediate.Blog.Application.ServiceLayer.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.security.Principal;

@Controller
public class ChatWsController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ConversationRepository conversationRepository;

    public static class SendMessage{
        private Long  recipientId;
        private String content;

        public SendMessage(){       

        }

        public Long getRecipientId() {
            return recipientId;
        }

        public void setRecipientId(Long recipientId) {
            this.recipientId = recipientId;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    @MessageMapping("/chat.send")
    public void send(@Payload SendMessage payload, Principal principal){
        if (principal == null || principal.getName() == null) {
            throw new IllegalStateException("WebSocket user is not authenticated");
        }

        MessageDto saved = messageService.sendMessageToUserAsEmail(
                principal.getName(),
                payload.getRecipientId(),
                payload.getContent()
        );

        User recipient = userRepo.findById(payload.getRecipientId()).orElseThrow(()-> new ResourceNotFoundException("User" , "id" , payload.getRecipientId()));
        // 1) Push to recipient first (this is the "delivery" attempt)
        messagingTemplate.convertAndSendToUser(recipient.getEmail(), "/queue/messages", saved);

        // 2) If push succeeded (no exception), mark delivered in DB and echo updated dto to sender
        LocalDateTime deliveredAt = LocalDateTime.now();
        messageService.markDelivered(saved.getId(), deliveredAt);
        saved.setDeliveredAt(deliveredAt);

        // Sender echo (useful for multi-tab / multi-device)
        messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/messages", saved);
    }

    public static class ReadConversation {
        private Long conversationId;

        public ReadConversation() {}

        public Long getConversationId() { return conversationId; }
        public void setConversationId(Long conversationId) { this.conversationId = conversationId; }
    }
    
    @MessageMapping("/chat.read")
    public void read(@Payload ReadConversation payload, Principal principal) {
        if (principal == null || principal.getName() == null) {
            throw new IllegalStateException("WebSocket user is not authenticated");
        }
        messageService.markConversationReadAsEmail(principal.getName(), payload.getConversationId());

        User reader = (User) userRepo.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", principal.getName()));

        Conversation conversation = conversationRepository.findById(payload.getConversationId())
                .orElseThrow(() -> new ResourceNotFoundException("Conversation", "id", payload.getConversationId()));

        User other = conversation.getParticipants().stream()
                .filter(u -> u.getId() != reader.getId())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Conversation must have 2 participants"));

        ReadReceiptEvent event = new ReadReceiptEvent(conversation.getId(), reader.getId(), LocalDateTime.now());
        messagingTemplate.convertAndSendToUser(other.getEmail(), "/queue/read-receipts", event);
    }

    public static class ReadReceiptEvent {
        private Long conversationId;
        private long readerId;
        private LocalDateTime readAt;

        public ReadReceiptEvent() {}

        public ReadReceiptEvent(Long conversationId, long readerId, LocalDateTime readAt) {
            this.conversationId = conversationId;
            this.readerId = readerId;
            this.readAt = readAt;
        }

        public Long getConversationId() {
            return conversationId;
        }

        public void setConversationId(Long conversationId) {
            this.conversationId = conversationId;
        }

        public long getReaderId() {
            return readerId;
        }

        public void setReaderId(long readerId) {
            this.readerId = readerId;
        }

        public LocalDateTime getReadAt() {
            return readAt;
        }

        public void setReadAt(LocalDateTime readAt) {
            this.readAt = readAt;
        }
    }

}
