package com.intermediate.Blog.Application.DtoLayers;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * User preview with flag indicating whether the current user follows this user.
 */
public class UserPreviewWithFollowStatus {
    private long id;
    private String username;
    private String profilePicture;

    @JsonProperty("following")
    private boolean following; // true if current user follows this user

    public UserPreviewWithFollowStatus() {}

    public UserPreviewWithFollowStatus(long id, String username, String profilePicture, boolean following) {
        this.id = id;
        this.username = username;
        this.profilePicture = profilePicture;
        this.following = following;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public boolean isFollowing() {
        return following;
    }

    public void setFollowing(boolean following) {
        this.following = following;
    }
}
