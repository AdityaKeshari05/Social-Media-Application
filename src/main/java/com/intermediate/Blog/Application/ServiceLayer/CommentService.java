package com.intermediate.Blog.Application.ServiceLayer;

import com.intermediate.Blog.Application.DtoLayers.CommentDto;
import com.intermediate.Blog.Application.Models.Comment;

import java.util.List;

public interface CommentService {
    CommentDto createComment(Long userId , Long postId , CommentDto commentDto);
    void deleteComment(Long commentId);
    List<CommentDto> getCommentsByPost(Long postId);
}
