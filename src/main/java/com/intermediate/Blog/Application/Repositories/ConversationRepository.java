package com.intermediate.Blog.Application.Repositories;

import com.intermediate.Blog.Application.Models.Conversation;
import com.intermediate.Blog.Application.Models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation  , Long> {
    Page<Conversation> findByParticipantsContainsOrderByLastMessageAtDesc(
            User participant,
            Pageable pageable
    );

    @Query("""
            select c from Conversation c
                join c.participants p1
                join c.participants p2
            where p1 = :user1 and p2 = :user2
                and size(c.participants) = 2
            """)
    Optional<Conversation> findDirectConversationBetween(
            @Param("user1") User user1 ,
            @Param("user2") User user2
    );

    Page<Conversation> findByParticipantsContainingOrderByLastMessageAtDesc(User me, Pageable pageable);
}
