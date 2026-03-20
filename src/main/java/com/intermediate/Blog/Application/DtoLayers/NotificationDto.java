package com.intermediate.Blog.Application.DtoLayers;

import com.intermediate.Blog.Application.Models.Notification;
import com.intermediate.Blog.Application.Models.NotificationType;

import java.time.LocalDateTime;

public class NotificationDto {


    private Long id;
    private NotificationType type;
    private boolean read;
    private LocalDateTime createdAt;

//    About the actor ( Who triggered the notification)
    private Long actorId;
    private String actorUsername;
    private String actorProfilePicture;

//    Target Post (if applicable)
    private Long postId;
    private String postTitle;
    private String commentText;

//    Follow related
    private boolean canFollowBack;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getActorId() {
        return actorId;
    }

    public void setActorId(Long actorId) {
        this.actorId = actorId;
    }

    public String getActorUsername() {
        return actorUsername;
    }

    public void setActorUsername(String actorUsername) {
        this.actorUsername = actorUsername;
    }

    public String getActorProfilePicture() {
        return actorProfilePicture;
    }

    public void setActorProfilePicture(String actorProfilePicture) {
        this.actorProfilePicture = actorProfilePicture;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public String getPostTitle() {
        return postTitle;
    }

    public void setPostTitle(String postTitle) {
        this.postTitle = postTitle;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public boolean isCanFollowBack() {
        return canFollowBack;
    }

    public void setCanFollowBack(boolean canFollowBack) {
        this.canFollowBack = canFollowBack;
    }
}
