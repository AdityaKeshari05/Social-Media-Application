package com.intermediate.Blog.Application.ServiceLayer;


import com.intermediate.Blog.Application.DtoLayers.CommentDto;
import com.intermediate.Blog.Application.DtoLayers.UserDto;
import com.intermediate.Blog.Application.Exception.ResourceNotFoundException;
import com.intermediate.Blog.Application.Models.Comment;
import com.intermediate.Blog.Application.Models.Post;
import com.intermediate.Blog.Application.Models.User;
import com.intermediate.Blog.Application.Repositories.CommentLikeRepository;
import com.intermediate.Blog.Application.Repositories.CommentRepository;
import com.intermediate.Blog.Application.Repositories.PostRepository;
import com.intermediate.Blog.Application.Repositories.UserRepo;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentRepository commentRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PostRepository postRepo;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommentLikeRepository commentLikeRepository;

    @Autowired
    private CurrentUserService currentUserService;

    @Override
    public CommentDto createComment(Long userId , Long postId , CommentDto commentDto){
        User user = userRepo.findById(userId)
                .orElseThrow(()-> new ResourceNotFoundException("User","id",userId));

        Post post = postRepo.findById(postId)
                .orElseThrow(()-> new ResourceNotFoundException("Post","id",postId));

        Comment comment = modelMapper.map(commentDto,Comment.class);
        comment.setPost(post);
        comment.setUser(user);

        if (commentDto.getParentCommentId() != null) {
            Comment parent = commentRepo.findById(commentDto.getParentCommentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentDto.getParentCommentId()));
            comment.setParent(parent);
        }

        Comment savedComment = commentRepo.save(comment);
        notificationService.createCommentNotification(post , user , comment.getText());
        if (comment.getParent() != null) {
            notificationService.createCommentReplyNotification(comment.getParent(), user, comment.getText());
        }
        return commentToDto(savedComment);
    }

    @Override
    public void deleteComment(Long commentId){
        Comment comment = commentRepo.findById(commentId)
                .orElseThrow(()-> new ResourceNotFoundException("Comment","id",commentId));

        // Delete likes on this comment
        commentLikeRepository.deleteAllByComment(comment);

        // Delete direct replies and their likes
        List<Comment> replies = commentRepo.findByParent(comment);
        for (Comment reply : replies) {
            commentLikeRepository.deleteAllByComment(reply);
            commentRepo.delete(reply);
        }

        // Finally delete the original comment
        commentRepo.delete(comment);
    }

    @Override
    public List<CommentDto> getCommentsByPost(Long postId) {
        Post post = postRepo.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

        return commentRepo.findByPost(post)
                .stream()
                .map(this::commentToDtoWithLikes)
                .collect(Collectors.toList());
    }

    private CommentDto commentToDto(Comment comment) {
        CommentDto dto = modelMapper.map(comment, CommentDto.class);
        if (comment.getUser() != null) {
            User u = comment.getUser();
            String profilePicture = null;
            if (u.getUserProfile() != null) {
                profilePicture = u.getUserProfile().getProfilePicture();
            }
            UserDto userDto = new UserDto(u.getId(), u.getUsername(), u.getEmail(), u.getAccountVisibility(), profilePicture);
            dto.setUser(userDto);
        }
        if (comment.getParent() != null) {
            dto.setParentCommentId(comment.getParent().getId());
        }
        return dto;
    }

    private CommentDto commentToDtoWithLikes(Comment comment) {
        CommentDto dto = commentToDto(comment);
        long count = commentLikeRepository.countByComment(comment);
        dto.setLikesCount(count);
        try {
            User current = currentUserService.getCurrentUser();
            boolean liked = commentLikeRepository.existsByUserAndComment(current, comment);
            // if needed on frontend we can infer from likesCount and this, but for now count is enough
        } catch (Exception ignored) {
            // unauthenticated requests just get count
        }
        return dto;
    }

}
