package com.intermediate.Blog.Application.Repositories;

import com.intermediate.Blog.Application.Models.FollowRequest;
import com.intermediate.Blog.Application.Models.Follower;
import com.intermediate.Blog.Application.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FollowRequestRepository extends JpaRepository<FollowRequest , Long> {
    boolean existsByRequesterAndTarget(User requester  , User target);

    List<FollowRequest> findByTarget(User target);

    FollowRequest findByRequesterId(Long requesterId);
}
