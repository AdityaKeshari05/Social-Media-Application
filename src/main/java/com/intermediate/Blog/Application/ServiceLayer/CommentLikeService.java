package com.intermediate.Blog.Application.ServiceLayer;

public interface CommentLikeService {

    void likeComment(long commentId, long userId);

    void unlikeComment(long commentId, long userId);

    long getLikesCount(long commentId);

    boolean hasUserLiked(long commentId, long userId);
}

