package com.intermediate.Blog.Application.Repositories;

import com.intermediate.Blog.Application.Models.Notification;
import com.intermediate.Blog.Application.Models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification , Long> {

    Page<Notification> findByRecipientOrderByCreatedAtDesc(User recipient , Pageable pageable);

    long countByRecipientAndReadAtIsNull(User recipient);

    List<Notification> findByRecipientOrderByCreatedAtDesc(User recipient);

    List<Notification> findByRecipientAndReadAtIsNull(User recipient);

    Optional<Notification> findByIdAndRecipient(Long id, User recipient);

}
