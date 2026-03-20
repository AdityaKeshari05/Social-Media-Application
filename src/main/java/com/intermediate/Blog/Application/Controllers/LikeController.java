package com.intermediate.Blog.Application.Controllers;


import com.intermediate.Blog.Application.DtoLayers.MessageResponse;
import com.intermediate.Blog.Application.DtoLayers.UserDto;
import com.intermediate.Blog.Application.Exception.AccessControlException;
import com.intermediate.Blog.Application.Exception.ResourceNotFoundException;
import com.intermediate.Blog.Application.Models.Like;
import com.intermediate.Blog.Application.Models.Post;
import com.intermediate.Blog.Application.Models.User;
import com.intermediate.Blog.Application.Repositories.PostRepository;
import com.intermediate.Blog.Application.ServiceLayer.AccountService;
import com.intermediate.Blog.Application.ServiceLayer.CurrentUserService;
import com.intermediate.Blog.Application.ServiceLayer.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/likes")
public class LikeController {

    @Autowired
    private LikeService likeService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private AccountService accountService;

    @Autowired
    private CurrentUserService currentUserService;

    @PostMapping("/posts/{postId}/like")
    public ResponseEntity<Like> likePost(@PathVariable long postId ){
        Post post = postRepository.findById(postId).orElseThrow(()-> new ResourceNotFoundException("Post" , "id" , postId));
        if(!accountService.canViewPosts(currentUserService.getCurrentUser(), post.getUser())){
            throw new AccessControlException("You cannot like this post");
        }
        return ResponseEntity.ok(likeService.likePost(postId, currentUserService.getCurrentUser().getId()));
    }

    @DeleteMapping("/posts/{postId}/like")
    public ResponseEntity<MessageResponse> unlikePost(@PathVariable long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
        if (!accountService.canViewPosts(currentUserService.getCurrentUser(), post.getUser())) {
            throw new AccessControlException("You cannot like this post");
        }
        likeService.unlikePost(postId, currentUserService.getCurrentUser().getId());
        return ResponseEntity.ok(new MessageResponse("Post unliked successfully"));
    }

    @GetMapping("/posts/{postId}/likes/count")
    public ResponseEntity<Long> getLikesCount(@PathVariable long postId){
        return ResponseEntity.ok(likeService.getLikesCount(postId));
    }

    @GetMapping("/posts/{postId}/likes/hasLiked")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Boolean> hasUserLiked(@PathVariable long postId){
        User user = currentUserService.getCurrentUser();
        return ResponseEntity.ok(likeService.hasUserLiked(postId, user.getId()));
    }
    @GetMapping("/posts/{postId}/likes/users")
    public ResponseEntity<List<UserDto>> getUsersWhoLikedPosts(@PathVariable long postId){
        return ResponseEntity.ok(likeService.getUsersWhoLikedPost(postId));
    }
}
