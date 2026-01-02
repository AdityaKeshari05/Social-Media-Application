package com.intermediate.Blog.Application.DtoLayers;

public class UserPreview {
    private long id;
    private String username;
    private String profilePicture;


    public UserPreview(long id, String username, String profilePicture) {
        this.id = id;
        this.username = username;
        this.profilePicture = profilePicture;
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
}
