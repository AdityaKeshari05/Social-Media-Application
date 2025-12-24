package com.intermediate.Blog.Application.ServiceLayer;

import com.intermediate.Blog.Application.DtoLayers.UserDto;
import com.intermediate.Blog.Application.Models.Like;

import java.util.List;

public interface LikeService {
    Like likePost(long postId , long userId);
    void unlikePost(long postId , long userId);
    Long getLikesCount(long postId);
    boolean hasUserLiked(long postId , long userId);
    List<UserDto> getUsersWhoLikedPost(long postId);
}
