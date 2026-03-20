package com.intermediate.Blog.Application.Repositories;

import com.intermediate.Blog.Application.Models.Category;
import com.intermediate.Blog.Application.Models.Post;
import com.intermediate.Blog.Application.Models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findByUser(User user);

    List<Post> findByCategory(Category category);

    Long countByUser(User user);

    @Query(value = "SELECT p.* FROM posts p " +
            "INNER JOIN users u ON p.user_id = u.id " +
            "LEFT JOIN followers f ON f.following_id = u.id AND f.follower_id = :currentUserId " +
            "WHERE u.account_visibility = 'PUBLIC' " +
            "   OR u.id = :currentUserId " +
            "   OR (u.account_visibility = 'PRIVATE' AND f.id IS NOT NULL) " +
            "ORDER BY CRC32(CONCAT(CAST(p.id AS CHAR), :seed))",
            countQuery = "SELECT COUNT(*) FROM posts p " +
                    "INNER JOIN users u ON p.user_id = u.id " +
                    "LEFT JOIN followers f ON f.following_id = u.id AND f.follower_id = :currentUserId " +
                    "WHERE u.account_visibility = 'PUBLIC' " +
                    "   OR u.id = :currentUserId " +
                    "   OR (u.account_visibility = 'PRIVATE' AND f.id IS NOT NULL)",
            nativeQuery = true)
    Page<Post> findViewablePostsForUser(@Param("currentUserId") Long currentUserId,
                                        @Param("seed") String seed,
                                        Pageable pageable);
}
