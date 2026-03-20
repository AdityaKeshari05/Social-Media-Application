package com.intermediate.Blog.Application.ServiceLayer;

import com.intermediate.Blog.Application.DtoLayers.ConversationDto;
import com.intermediate.Blog.Application.DtoLayers.MessageDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface MessageService {
    Page<ConversationDto> listConversations(Pageable pageable);
    Page<MessageDto> listMessages(Long conversationId, Pageable pageable);

    MessageDto sendMessageToUser(Long recipientUserId, String content);
    MessageDto sendMessageToUserAsEmail(String senderEmail, Long recipientUserId, String content);

    void markConversationRead(Long conversationId);

    void markConversationReadAsEmail(String readerEmail, Long conversationId);

    void deleteConversation(Long conversationId);
    void markDelivered(Long messageId, LocalDateTime deliveredAt);
}