package com.intermediate.Blog.Application.ServiceLayer;

import com.intermediate.Blog.Application.Exception.ResourceNotFoundException;
import com.intermediate.Blog.Application.Models.Comment;
import com.intermediate.Blog.Application.Models.CommentLike;
import com.intermediate.Blog.Application.Models.User;
import com.intermediate.Blog.Application.Repositories.CommentLikeRepository;
import com.intermediate.Blog.Application.Repositories.CommentRepository;
import com.intermediate.Blog.Application.Repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommentLikeServiceImpl implements CommentLikeService {

    @Autowired
    private CommentLikeRepository commentLikeRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private NotificationService notificationService;

    @Override
    public void likeComment(long commentId, long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));

        if (commentLikeRepository.existsByUserAndComment(user, comment)) {
            return;
        }

        CommentLike like = new CommentLike();
        like.setUser(user);
        like.setComment(comment);
        commentLikeRepository.save(like);

        notificationService.createCommentLikeNotification(comment, user);
    }

    @Override
    public void unlikeComment(long commentId, long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));

        CommentLike like = commentLikeRepository.findByUserAndComment(user, comment)
                .orElseThrow(() -> new ResourceNotFoundException("CommentLike", "id", commentId));
        commentLikeRepository.delete(like);
    }

    @Override
    public long getLikesCount(long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));
        return commentLikeRepository.countByComment(comment);
    }

    @Override
    public boolean hasUserLiked(long commentId, long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));
        return commentLikeRepository.existsByUserAndComment(user, comment);
    }
}

