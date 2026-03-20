package com.intermediate.Blog.Application.Controllers;


import com.intermediate.Blog.Application.DtoLayers.CommentDto;

import com.intermediate.Blog.Application.Exception.AccessControlException;
import com.intermediate.Blog.Application.Exception.ResourceNotFoundException;
import com.intermediate.Blog.Application.Models.Comment;
import com.intermediate.Blog.Application.Models.Post;
import com.intermediate.Blog.Application.Models.User;
import com.intermediate.Blog.Application.Repositories.CommentRepository;

import com.intermediate.Blog.Application.Repositories.PostRepository;
import com.intermediate.Blog.Application.ServiceLayer.AccountService;
import com.intermediate.Blog.Application.ServiceLayer.CommentLikeService;
import com.intermediate.Blog.Application.ServiceLayer.CommentService;
import com.intermediate.Blog.Application.ServiceLayer.CurrentUserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private AccountService accountService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private CommentLikeService commentLikeService;

    @PostMapping("/post/{postId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentDto> createComment(@PathVariable Long postId, @Valid @RequestBody CommentDto commentDto) {
        Post post =  postRepository.findById(postId).orElseThrow(()-> new ResourceNotFoundException("Post" , "id" , postId));
        if(!accountService.canViewPosts(currentUserService.getCurrentUser(), post.getUser())){
            throw new AccessControlException("You do not have the access to comment on this post");
        }
        CommentDto createdComment = commentService.createComment(currentUserService.getCurrentUser().getId(), postId, commentDto);
        return new ResponseEntity<>(createdComment, HttpStatus.CREATED);
    }

    @DeleteMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> deleteComments(@PathVariable Long commentId){

        Comment comment = commentRepository.findById(commentId).orElseThrow(()->new ResourceNotFoundException("Comment" ,"id" , commentId));

       User owner = comment.getUser();

        if(currentUserService.getCurrentUser().getId() != owner.getId()){
            throw new AccessControlException("Access denied");
        }

        commentService.deleteComment(commentId);
        return new ResponseEntity<>("Comment Deleted Successfully" , HttpStatus.OK);
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<List<CommentDto>> getCommentsByPost(@PathVariable Long postId){
        return ResponseEntity.ok(commentService.getCommentsByPost(postId));
    }

    @PostMapping("/{commentId}/like")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> likeComment(@PathVariable Long commentId) {
        commentLikeService.likeComment(commentId, currentUserService.getCurrentUser().getId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{commentId}/like")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> unlikeComment(@PathVariable Long commentId) {
        commentLikeService.unlikeComment(commentId, currentUserService.getCurrentUser().getId());
        return ResponseEntity.noContent().build();
    }
}
