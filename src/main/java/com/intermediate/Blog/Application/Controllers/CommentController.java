package com.intermediate.Blog.Application.Controllers;


import com.intermediate.Blog.Application.DtoLayers.CommentDto;

import com.intermediate.Blog.Application.DtoLayers.UserDto;
import com.intermediate.Blog.Application.Exception.ResourceNotFoundException;
import com.intermediate.Blog.Application.Models.Comment;
import com.intermediate.Blog.Application.Models.User;
import com.intermediate.Blog.Application.Repositories.CommentRepository;
import com.intermediate.Blog.Application.ServiceLayer.CommentService;
import com.intermediate.Blog.Application.ServiceLayer.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentRepository commentRepository;



    @PostMapping("/post/{postId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentDto>  createComment( @PathVariable Long postId , @RequestBody CommentDto commentDto){

        String currentUser = SecurityContextHolder.getContext()
                .getAuthentication().getName();
      User  user =  userService.getUserByEmail(currentUser);
      Long userId  = user.getId();

        if(!user.getEmail().equals(currentUser)){
            throw new RuntimeException("Access Denied");

        }

        CommentDto  createdComment = commentService.createComment(userId, postId, commentDto);
        return new ResponseEntity<>(createdComment, HttpStatus.CREATED);
    }

    @DeleteMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> deleteComments(@PathVariable Long commentId){
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();


        Comment comment = commentRepository.findById(commentId).orElseThrow(()->new ResourceNotFoundException("Comment" ,"id" , commentId));

        String user = comment.getUser().getEmail();

        if(!currentUser.equals(user)){
            throw new RuntimeException("Access Denied");
        }

        commentService.deleteComment(commentId);
        return new ResponseEntity<>("Comment Deleted Successfully" , HttpStatus.OK);
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<List<CommentDto>> getCommentsByPost(@PathVariable Long postId){
        return ResponseEntity.ok(commentService.getCommentsByPost(postId));
    }
}
