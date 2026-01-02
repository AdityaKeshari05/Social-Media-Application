package com.intermediate.Blog.Application.Repositories;


import com.intermediate.Blog.Application.Models.FollowRelation;
import com.intermediate.Blog.Application.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FollowerRepository extends JpaRepository<FollowRelation, Long> {

    boolean existsByFollowerAndFollowing(User follower , User following);
    void deleteByFollowerAndFollowing(User follower , User following);

    List<FollowRelation> findByFollowing(User user);
    List<FollowRelation> findByFollower(User user);



}
