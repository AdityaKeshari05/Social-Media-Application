package com.intermediate.Blog.Application.Repositories;

import com.intermediate.Blog.Application.Models.PasswordReset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetRepository extends JpaRepository<PasswordReset , Long> {
    Optional<PasswordReset> findByToken(String token);
}
