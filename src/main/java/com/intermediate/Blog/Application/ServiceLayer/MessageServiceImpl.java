package com.intermediate.Blog.Application.ServiceLayer;

import com.intermediate.Blog.Application.DtoLayers.ConversationDto;
import com.intermediate.Blog.Application.DtoLayers.MessageDto;
import com.intermediate.Blog.Application.DtoLayers.UserPreview;
import com.intermediate.Blog.Application.Exception.AccessControlException;
import com.intermediate.Blog.Application.Exception.ResourceNotFoundException;
import com.intermediate.Blog.Application.Models.Conversation;
import com.intermediate.Blog.Application.Models.Message;
import com.intermediate.Blog.Application.Models.User;
import com.intermediate.Blog.Application.Repositories.ConversationRepository;
import com.intermediate.Blog.Application.Repositories.MessageRepository;
import com.intermediate.Blog.Application.Repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private CurrentUserService currentUserService;

    @Override
    public Page<ConversationDto> listConversations(Pageable pageable) {
        User me = currentUserService.getCurrentUser();

        Page<Conversation> conversations =
                conversationRepository.findByParticipantsContainingOrderByLastMessageAtDesc(me, pageable);

        return conversations.map(c -> toConversationDto(c, me));
    }

    @Override
    public Page<MessageDto> listMessages(Long conversationId, Pageable pageable) {
        User me = currentUserService.getCurrentUser();

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation", "id", conversationId));

        ensureParticipant(conversation, me);

        return messageRepository.findByConversationOrderByCreatedAtAsc(conversation, pageable)
                .map(this::toMessageDto);
    }

    @Override
    @Transactional
    public MessageDto sendMessageToUser(Long recipientUserId, String content) {
        User me = currentUserService.getCurrentUser();
        return sendMessageInternal(me, recipientUserId, content);
    }

    @Override
    @Transactional
    public MessageDto sendMessageToUserAsEmail(String senderEmail, Long recipientUserId, String content) {
        if (senderEmail == null || senderEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("senderEmail is required");
        }
        User me = (User) userRepo.findByEmail(senderEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", senderEmail));
        return sendMessageInternal(me, recipientUserId, content);
    }

    private MessageDto sendMessageInternal(User me, Long recipientUserId, String content) {
        if (recipientUserId == null) {
            throw new IllegalArgumentException("recipientUserId is required");
        }
        if (me.getId() == recipientUserId) {
            throw new IllegalArgumentException("You cannot message yourself");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Message content cannot be empty");
        }
        if (content.length() > 2000) {
            throw new IllegalArgumentException("Message content too long (max 2000)");
        }

        User recipient = userRepo.findById(recipientUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", recipientUserId));

        Conversation conversation = conversationRepository
                .findDirectConversationBetween(me, recipient)
                .orElseGet(() -> {
                    Conversation c = new Conversation();
                    c.getParticipants().add(me);
                    c.getParticipants().add(recipient);
                    return conversationRepository.save(c);
                });

        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(me);
        message.setContent(content.trim());

        Message saved = messageRepository.save(message);

        conversation.setLastMessageAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        return toMessageDto(saved);
    }

    @Override
    @Transactional
    public void markConversationRead(Long conversationId) {
        User me = currentUserService.getCurrentUser();

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation", "id", conversationId));

        ensureParticipant(conversation, me);

        messageRepository.markConversationRead(conversation, me, LocalDateTime.now());
    }

    @Override
    @Transactional
    public void markConversationReadAsEmail(String readerEmail, Long conversationId) {
        if (readerEmail == null || readerEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("readerEmail is required");
        }
        User me = (User) userRepo.findByEmail(readerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", readerEmail));
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation", "id", conversationId));
        ensureParticipant(conversation, me);
        messageRepository.markConversationRead(conversation, me, LocalDateTime.now());

    }

    @Override
    @Transactional
    public void markDelivered(Long messageId, LocalDateTime deliveredAt) {
        if (messageId == null) throw new IllegalArgumentException("messageId is required");
        if (deliveredAt == null) throw new IllegalArgumentException("deliveredAt is required");
        messageRepository.markDelivered(messageId, deliveredAt);
    }

    @Override
    @Transactional
    public void deleteConversation(Long conversationId) {
        if (conversationId == null || conversationId <= 0) {
            throw new IllegalArgumentException("conversationId is required");
        }

        User me = currentUserService.getCurrentUser();

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation", "id", conversationId));

        ensureParticipant(conversation, me);

        // Remove messages first to avoid FK constraint issues.
        messageRepository.deleteByConversation(conversation);
        conversationRepository.delete(conversation);
    }

    private void ensureParticipant(Conversation conversation, User user) {
        boolean isParticipant = conversation.getParticipants().stream().anyMatch(u -> u.getId() == user.getId());
        if (!isParticipant) {
            throw new AccessControlException("Access denied");
        }
    }

    private ConversationDto toConversationDto(Conversation c, User me) {
        User other = c.getParticipants().stream()
                .filter(u -> u.getId() != me.getId())
                .findFirst()
                .orElse(null);

        UserPreview otherPreview = null;
        if (other != null) {
            String profilePicture = other.getUserProfile() != null ? other.getUserProfile().getProfilePicture() : null;
            otherPreview = new UserPreview(other.getId(), other.getUsername(), profilePicture);
        }

        String lastMessage = messageRepository.findFirstByConversationOrderByCreatedAtDesc(c)
                .map(Message::getContent)
                .orElse(null);

        long unread = messageRepository.countByConversationAndSenderNotAndReadAtIsNull(c, me);

        ConversationDto dto = new ConversationDto();
        dto.setId(c.getId());
        dto.setOtherUser(otherPreview);
        dto.setLastMessage(lastMessage);
        dto.setLastMessageAt(c.getLastMessageAt());
        dto.setUnreadCount(unread);
        return dto;
    }

    private MessageDto toMessageDto(Message m) {
        MessageDto dto = new MessageDto();
        dto.setId(m.getId());
        dto.setConversationId(m.getConversation().getId());
        dto.setSenderId(m.getSender().getId());
        dto.setSenderUsername(m.getSender().getUsername());
        dto.setContent(m.getContent());
        dto.setCreatedAt(m.getCreatedAt());
        dto.setDeliveredAt(m.getDeliveredAt());
        dto.setReadAt(m.getReadAt());
        return dto;
    }
}