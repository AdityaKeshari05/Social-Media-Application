package com.intermediate.Blog.Application.Controllers;

import com.intermediate.Blog.Application.DtoLayers.UserPreview;
import com.intermediate.Blog.Application.Exception.ResourceNotFoundException;
import com.intermediate.Blog.Application.Models.AccountVisibility;
import com.intermediate.Blog.Application.Models.FollowRequest;
import com.intermediate.Blog.Application.Models.User;
import com.intermediate.Blog.Application.Repositories.FollowRequestRepository;
import com.intermediate.Blog.Application.Repositories.FollowerRepository;
import com.intermediate.Blog.Application.Repositories.UserRepo;
import com.intermediate.Blog.Application.ServiceLayer.FollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.config.RepositoryConfigurationSource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.service.annotation.DeleteExchange;

import java.util.List;

@RestController
@RequestMapping("/api/follow-system")
public class FollowController {

    @Autowired
    private FollowService followService;

    @Autowired
    private FollowerRepository followerRepository;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private FollowRequestRepository followRequestRepository;

    @PostMapping("/follow/{userId}")
    public ResponseEntity<String> followUser(@PathVariable  Long userId){
        User targetUser = userRepo.findById(userId).orElseThrow(()-> new ResourceNotFoundException("User", "id",userId));
        followService.followUser(userId);

        if(targetUser.getAccountVisibility() == AccountVisibility.PRIVATE){
            return ResponseEntity.ok("Follow request sent !!");
        }

        return ResponseEntity.ok("You started following "+targetUser.getUsername());
    }

    @DeleteMapping("/unfollow/{userId}")
    public ResponseEntity<String> unfollowUser(@PathVariable Long userId){
        User targetUser = userRepo.findById(userId).orElseThrow(()-> new ResourceNotFoundException("User", "id",userId));
        followService.unfollowUser(userId);

        return ResponseEntity.ok("You Unfollow " +targetUser.getUsername());
    }

    @GetMapping("/followers")
    public ResponseEntity<List<UserPreview>> getFollowers(){
        return ResponseEntity.ok(followService.getFollower());
    }

    @GetMapping("/following")
    public ResponseEntity<List<UserPreview>> getFollowings(){
        return ResponseEntity.ok(followService.getFollowing());
    }

    @PostMapping("/acceptFollowRequest/{requesterId}")
    public ResponseEntity<String> acceptFollowRequest(@PathVariable Long requesterId){
        return ResponseEntity.ok(followService.acceptFollowRequest(requesterId));
    }






    


    private User getCurrentUser(){
        String email  = SecurityContextHolder.getContext().getAuthentication().getName();
        User user  = (User) userRepo.findByEmail(email).orElseThrow(()-> new ResourceNotFoundException("User"  ,  "email" , email));
        return user;
    }
}
