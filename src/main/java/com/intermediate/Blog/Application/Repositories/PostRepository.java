package com.intermediate.Blog.Application.Repositories;

import com.intermediate.Blog.Application.Models.Category;
import com.intermediate.Blog.Application.Models.Post;
import com.intermediate.Blog.Application.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findByUser(User user);

    List<Post> findByCategory(Category category);

    Long countByUser(User user);
}
