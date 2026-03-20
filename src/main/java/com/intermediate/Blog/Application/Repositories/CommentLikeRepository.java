package com.intermediate.Blog.Application.Repositories;

import com.intermediate.Blog.Application.Models.Comment;
import com.intermediate.Blog.Application.Models.CommentLike;
import com.intermediate.Blog.Application.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    boolean existsByUserAndComment(User user, Comment comment);

    Optional<CommentLike> findByUserAndComment(User user, Comment comment);

    long countByComment(Comment comment);

    List<CommentLike> findAllByComment(Comment comment);

    void deleteAllByComment(Comment comment);
}

