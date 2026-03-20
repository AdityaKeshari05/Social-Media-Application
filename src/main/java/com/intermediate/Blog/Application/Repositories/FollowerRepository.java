package com.intermediate.Blog.Application.Repositories;


import com.intermediate.Blog.Application.Models.FollowRelation;
import com.intermediate.Blog.Application.Models.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface FollowerRepository extends JpaRepository<FollowRelation, Long> {

    boolean existsByFollowerAndFollowing(User follower , User following);
    void deleteByFollowerAndFollowing(User follower , User following);

    List<FollowRelation> findByFollowing(User user);
    List<FollowRelation> findByFollower(User user);

    @Query("SELECT COUNT(f) FROM FollowRelation f WHERE f.following.id = :followingId")
    long countFollowers(@Param("followingId") long followingId);

    @Query("SELECT COUNT(f) FROM FollowRelation f WHERE f.follower.id = :followerId")
    long countFollowing(@Param("followerId") long followerId);

    @Modifying
    @Transactional
    @Query("DELETE FROM FollowRelation fr WHERE fr.follower.id = :followerId AND fr.following.id = :followingId")
    int deleteByFollowerIdAndFollowingId(@Param("followerId") long followerId, @Param("followingId") long followingId);


}
