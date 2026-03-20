package com.intermediate.Blog.Application.Controllers;

import com.intermediate.Blog.Application.DtoLayers.MessageResponse;
import com.intermediate.Blog.Application.DtoLayers.UserPreview;
import com.intermediate.Blog.Application.DtoLayers.UserPreviewWithFollowStatus;
import com.intermediate.Blog.Application.Exception.AccessControlException;
import com.intermediate.Blog.Application.Exception.ResourceNotFoundException;
import com.intermediate.Blog.Application.Models.AccountVisibility;
import com.intermediate.Blog.Application.Models.FollowRequest;
import com.intermediate.Blog.Application.Models.User;
import com.intermediate.Blog.Application.Repositories.FollowRequestRepository;
import com.intermediate.Blog.Application.Repositories.FollowerRepository;
import com.intermediate.Blog.Application.Repositories.UserRepo;
import com.intermediate.Blog.Application.ServiceLayer.AccountService;
import com.intermediate.Blog.Application.ServiceLayer.CurrentUserService;
import com.intermediate.Blog.Application.ServiceLayer.FollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @Autowired
    private AccountService accountService;

    @Autowired
    private CurrentUserService currentUserService;

    @PostMapping("/follow/{userId}")
    public ResponseEntity<MessageResponse> followUser(@PathVariable Long userId) {
        User targetUser = userRepo.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        followService.followUser(userId);
        if (targetUser.getAccountVisibility() == AccountVisibility.PRIVATE) {
            return ResponseEntity.ok(new MessageResponse("Follow request sent"));
        }
        return ResponseEntity.ok(new MessageResponse("You started following " + targetUser.getUsername()));
    }

    @DeleteMapping("/unfollow/{userId}")
    public ResponseEntity<MessageResponse> unfollowUser(@PathVariable Long userId) {
        User targetUser = userRepo.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        followService.unfollowUser(userId);
        return ResponseEntity.ok(new MessageResponse("You unfollowed " + targetUser.getUsername()));
    }

    @DeleteMapping("/removeFollower/{followerId}")
    public ResponseEntity<MessageResponse> removeFollower(@PathVariable Long followerId) {
        User follower = userRepo.findById(followerId).orElseThrow(() -> new ResourceNotFoundException("User", "id", followerId));
        followService.removeFollower(followerId);
        return ResponseEntity.ok(new MessageResponse("Removed " + follower.getUsername() + " from followers"));
    }

    @DeleteMapping("/cancelFollowRequest/{userId}")
    public ResponseEntity<MessageResponse> cancelFollowRequest(@PathVariable Long userId) {
        User targetUser = userRepo.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        followService.cancelFollowRequest(userId);
        return ResponseEntity.ok(new MessageResponse("Follow request to " + targetUser.getUsername() + " was cancelled"));
    }

    @GetMapping("/followers")
    public ResponseEntity<List<UserPreview>> getFollowers() {
        return ResponseEntity.ok(followService.getFollowers());
    }

    @GetMapping("/following")
    public ResponseEntity<List<UserPreview>> getFollowing() {
        return ResponseEntity.ok(followService.getFollowing());
    }

    @GetMapping("/users/{userId}/followers")
    public ResponseEntity<List<UserPreviewWithFollowStatus>> getFollowersOfUser(@PathVariable Long userId) {
        User owner = userRepo.findById(userId).orElseThrow(()-> new ResourceNotFoundException("User" , "id" , userId));
        User currentUser = currentUserService.getCurrentUser();
        if(!accountService.canViewFollowList(currentUser ,  owner)){
            throw new AccessControlException("You cannot view the follower list of this user");
        }
        return ResponseEntity.ok(followService.getFollowersOfUserWithFollowStatus(userId));
    }

    @GetMapping("/users/{userId}/following")
    public ResponseEntity<List<UserPreviewWithFollowStatus>> getFollowingOfUser(@PathVariable Long userId) {
        User owner = userRepo.findById(userId).orElseThrow(()-> new ResourceNotFoundException("User" , "id" , userId));
        User currentUser = currentUserService.getCurrentUser();
        if(!accountService.canViewFollowList(currentUser ,  owner)){
            throw new AccessControlException("You cannot view the following list of this user");
        }
        return ResponseEntity.ok(followService.getFollowingOfUserWithFollowStatus(userId));
    }

    @GetMapping("/users/{userId}/mutual-followers")
    public ResponseEntity<List<UserPreviewWithFollowStatus>> getMutualFollowers(@PathVariable Long userId) {
        User owner = userRepo.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        User currentUser = currentUserService.getCurrentUser();
        if (!accountService.canViewFollowList(currentUser, owner)) {
            throw new AccessControlException("You cannot view the mutual list of this user");
        }
        return ResponseEntity.ok(followService.getMutualFollowers(userId));
    }

    @PostMapping("/acceptFollowRequest/{requesterId}")
    public ResponseEntity<MessageResponse> acceptFollowRequest(@PathVariable Long requesterId) {
        return ResponseEntity.ok(new MessageResponse(followService.acceptFollowRequest(requesterId)));
    }

    @PostMapping("/rejectFollowRequest/{requesterId}")
    public ResponseEntity<MessageResponse> rejectFollowRequest(@PathVariable Long requesterId) {
        return ResponseEntity.ok(new MessageResponse(followService.rejectFollowRequest(requesterId)));
    }
}
