package com.intermediate.Blog.Application.Controllers;

import com.intermediate.Blog.Application.DtoLayers.ConversationDto;
import com.intermediate.Blog.Application.DtoLayers.MessageDto;
import com.intermediate.Blog.Application.DtoLayers.MessageResponse;
import com.intermediate.Blog.Application.DtoLayers.SendMessageRequest;
import com.intermediate.Blog.Application.ServiceLayer.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @GetMapping("/conversations")
    public ResponseEntity<Page<ConversationDto>> listConversations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(messageService.listConversations(pageable));
    }

    @GetMapping("/conversations/{conversationId}")
    public ResponseEntity<Page<MessageDto>> listMessages(
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(messageService.listMessages(conversationId, pageable));
    }

    @PostMapping("/to/{userId}")
    public ResponseEntity<MessageDto> sendMessage(
            @PathVariable Long userId,
            @RequestBody SendMessageRequest request
    ) {
        MessageDto created = messageService.sendMessageToUser(userId, request.getContent());
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PostMapping("/conversations/{conversationId}/read")
    public ResponseEntity<MessageResponse> markRead(@PathVariable Long conversationId) {
        messageService.markConversationRead(conversationId);
        return ResponseEntity.ok(new MessageResponse("Conversation marked as read"));
    }

    @DeleteMapping("/conversations/{conversationId}")
    public ResponseEntity<MessageResponse> deleteConversation(@PathVariable Long conversationId) {
        messageService.deleteConversation(conversationId);
        return ResponseEntity.ok(new MessageResponse("Conversation deleted"));
    }
}