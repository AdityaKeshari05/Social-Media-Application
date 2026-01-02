package com.intermediate.Blog.Application.DtoLayers;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.intermediate.Blog.Application.Models.Post;

import java.util.List;

public class ProfileDto {

    private String username;
    private String bio ;
    private String profilePicture;
    private Long noOfPosts;
    private List<PostDto> posts;
    private boolean isPrivate;

    public ProfileDto(String username, String bio, String profilePicture, Long noOfPosts , List<PostDto> posts) {
        this.username = username;
        this.bio = bio;
        this.profilePicture = profilePicture;
        this.noOfPosts = noOfPosts;
        this.posts = posts;

    }
    public ProfileDto(){}

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public Long getNoOfPosts() {
        return noOfPosts;
    }

    public void setNoOfPosts(Long noOfPosts) {
        this.noOfPosts = noOfPosts;
    }

    public List<PostDto> getPosts() {
        return posts;
    }

    public void setPosts(List<PostDto> posts) {
        this.posts = posts;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }
}
