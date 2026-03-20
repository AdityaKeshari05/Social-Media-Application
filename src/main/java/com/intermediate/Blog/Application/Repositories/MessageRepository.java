package com.intermediate.Blog.Application.Repositories;

import com.intermediate.Blog.Application.Models.Conversation;
import com.intermediate.Blog.Application.Models.Message;
import com.intermediate.Blog.Application.Models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message , Long> {

    Page<Message> findByConversationOrderByCreatedAtAsc(
            Conversation conversation,
            Pageable pageable
    );

    long countByConversationAndSenderNotAndReadAtIsNull(
            Conversation conversation,
            User recipient
    );

    @Modifying
    @Query("""
            Update Message m
              set m.readAt = :readAt
            where m.conversation = :conversation
              and m.sender <> :reader
              and m.readAt is null
            """)
        int markConversationRead(
            @Param("conversation") Conversation conversation,
            @Param("reader") User reader,
            @Param("readAt") LocalDateTime readAt
            );

    Optional<Message> findFirstByConversationOrderByCreatedAtDesc(Conversation conversation);

    @Modifying
    @Query("update Message m set m.deliveredAt = :deliveredAt where m.id = :messageId and m.deliveredAt is null")
    int markDelivered(@Param("messageId") Long messageId, @Param("deliveredAt") LocalDateTime deliveredAt);

    void deleteByConversation(Conversation conversation);
}
