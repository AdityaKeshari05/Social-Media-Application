package com.intermediate.Blog.Application.DtoLayers;

/**
 * Lightweight projection of a user for lists/search.
 * Extra fields (name, followersCount, followedBySummary, following) are optional and may be null
 * depending on the use‑case.
 */
public class UserPreview {
    private long id;
    private String username;
    private String profilePicture;

    // Optional extras
    private String name;
    private Long followersCount;
    /** Example: "Followed by alice and 2 others". */
    private String followedBySummary;
    /** True when the current viewer follows this user (for search results). */
    private Boolean following;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getFollowersCount() {
        return followersCount;
    }

    public void setFollowersCount(Long followersCount) {
        this.followersCount = followersCount;
    }

    public String getFollowedBySummary() {
        return followedBySummary;
    }

    public void setFollowedBySummary(String followedBySummary) {
        this.followedBySummary = followedBySummary;
    }

    public Boolean getFollowing() {
        return following;
    }

    public void setFollowing(Boolean following) {
        this.following = following;
    }
}
