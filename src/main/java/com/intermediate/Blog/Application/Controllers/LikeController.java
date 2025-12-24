package com.intermediate.Blog.Application.Controllers;


import com.intermediate.Blog.Application.DtoLayers.UserDto;
import com.intermediate.Blog.Application.Exception.ResourceNotFoundException;
import com.intermediate.Blog.Application.Models.Like;
import com.intermediate.Blog.Application.Models.User;
import com.intermediate.Blog.Application.Repositories.UserRepo;
import com.intermediate.Blog.Application.ServiceLayer.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/likes")
public class LikeController {

    @Autowired
    private LikeService likeService;

    @Autowired
    private UserRepo userRepo;


    @PostMapping("/posts/{postId}/like")
    public ResponseEntity<Like> likePost(@PathVariable long postId ){

        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = (User) userRepo.findByEmail(currentUser).orElseThrow(()-> new ResourceNotFoundException("User" , "email" , currentUser));

        if(!currentUser.equals(user.getEmail())) {
            throw new RuntimeException("Access Denied");
        }
        return ResponseEntity.ok(likeService.likePost(postId,user.getId()));
    }

    @DeleteMapping("/posts/{postId}/like")
    public ResponseEntity<String> unlikePost( @PathVariable long postId ){
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = (User) userRepo.findByEmail(currentUser).orElseThrow(()-> new ResourceNotFoundException("User" , "email" , currentUser));
        if(!currentUser.equals(user.getEmail())){
            throw new RuntimeException("Access Denied");
        }
        likeService.unlikePost(postId,user.getId());
        return ResponseEntity.ok("Post unliked Successfully");
    }

    @GetMapping("/posts/{postId}/likes/count")
    public ResponseEntity<Long> getLikesCount(@PathVariable long postId){
        return ResponseEntity.ok(likeService.getLikesCount(postId));
    }

    @GetMapping("/posts/{postId}/likes/hasLiked")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Boolean> hasUserLiked(@PathVariable long postId){
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = (User) userRepo.findByEmail(currentUser).orElseThrow(()-> new ResourceNotFoundException("User" , "email" , currentUser));
        return ResponseEntity.ok(likeService.hasUserLiked(postId, user.getId()));
    }
    @GetMapping("/posts/{postId}/likes/users")
    public ResponseEntity<List<UserDto>> getUsersWhoLikedPosts(@PathVariable long postId){
        return ResponseEntity.ok(likeService.getUsersWhoLikedPost(postId));
    }
}
