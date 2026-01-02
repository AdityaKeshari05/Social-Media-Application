package com.intermediate.Blog.Application.Repositories;


import com.intermediate.Blog.Application.DtoLayers.UserPreview;
import com.intermediate.Blog.Application.Models.Follower;
import com.intermediate.Blog.Application.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FollowerRepository extends JpaRepository<Follower , Long> {

    boolean existsByFollowerAndFollowing(User follower , User following);
    void deleteByFollowerAndFollowing(User follower , User following);

    List<Follower> findByFollowing(User user);
    List<Follower> findByFollower(User user);



}
