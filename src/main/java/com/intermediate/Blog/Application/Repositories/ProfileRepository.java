package com.intermediate.Blog.Application.Repositories;

import com.intermediate.Blog.Application.Models.User;
import com.intermediate.Blog.Application.Models.UserProfile;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.awt.dnd.DropTargetListener;
import java.util.Optional;

public interface ProfileRepository extends JpaRepository<UserProfile , Long> {
    Optional<UserProfile> findByUser(User user);

    Optional<UserProfile> findByUserId(Long userId);

    }

