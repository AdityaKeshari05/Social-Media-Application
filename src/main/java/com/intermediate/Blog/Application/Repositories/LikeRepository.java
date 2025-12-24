package com.intermediate.Blog.Application.Repositories;

import com.intermediate.Blog.Application.Models.Like;
import com.intermediate.Blog.Application.Models.Post;
import com.intermediate.Blog.Application.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.parameters.P;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like , Long> {
    Optional<Like> findByUserAndPost(User user , Post post);
    boolean existsByUserAndPost(User user , Post post);
    Long countByPost(Post post);
    List<Like> findAllByPost(Post post);


}
