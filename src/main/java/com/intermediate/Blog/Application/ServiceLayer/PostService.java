package com.intermediate.Blog.Application.ServiceLayer;

import com.intermediate.Blog.Application.DtoLayers.PostDto;
import com.intermediate.Blog.Application.Models.Post;

import java.util.List;

public interface PostService {
    PostDto createPost(PostDto post , Long userId);
    PostDto updatePost(PostDto postDto , Long postId);
    void deletePost(Long postId);
    PostDto getPostById(Long postId);
    List<PostDto> getAllPosts();
    List<PostDto> getPostByUser(Long userId);

    List<PostDto> getPostByCategoryId(Long id);

    PostDto updatePostImage(Long postId , String imagePath);




}
