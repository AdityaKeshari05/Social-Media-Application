package com.intermediate.Blog.Application.DtoLayers;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ProfileDto {

    private Long userId;
    private String username;
    private String name;
    private String website;
    private String link2;
    private String bio;
    private String profilePicture;
    private Long noOfPosts;
    private List<PostDto> posts;
    private Long followersCount;
    private Long followingCount;

    @JsonProperty("isPrivate")
    private boolean isPrivate;

    /** True when the current viewer is following this profile's user. */
    @JsonProperty("isFollowing")
    private boolean isFollowing;

    /** True when the current viewer has a pending follow request sent to this user. */
    @JsonProperty("isFollowRequested")
    private boolean isFollowRequested;

    /** Up to 2 mutual followers (followers of this profile that the viewer also follows). Shown as "Followed by X, Y and more". */
    private List<UserPreview> mutualPreview;
    /** Total count of mutual followers (so frontend can show "and N others"). */
    private Integer mutualCount;

    public ProfileDto(String username, String bio, String profilePicture, Long noOfPosts , List<PostDto> posts) {
        this.username = username;
        this.bio = bio;
        this.profilePicture = profilePicture;
        this.noOfPosts = noOfPosts;
        this.posts = posts;

    }
    public ProfileDto() {}

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getLink2() {
        return link2;
    }

    public void setLink2(String link2) {
        this.link2 = link2;
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

    public Long getFollowersCount() {
        return followersCount;
    }

    public void setFollowersCount(Long followersCount) {
        this.followersCount = followersCount;
    }

    public Long getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(Long followingCount) {
        this.followingCount = followingCount;
    }

    @JsonProperty("isPrivate")
    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }

    @JsonProperty("isFollowing")
    public boolean isFollowing() {
        return isFollowing;
    }

    public void setFollowing(boolean following) {
        isFollowing = following;
    }

    @JsonProperty("isFollowRequested")
    public boolean isFollowRequested() {
        return isFollowRequested;
    }

    public void setFollowRequested(boolean followRequested) {
        isFollowRequested = followRequested;
    }

    public List<UserPreview> getMutualPreview() {
        return mutualPreview;
    }

    public void setMutualPreview(List<UserPreview> mutualPreview) {
        this.mutualPreview = mutualPreview;
    }

    public Integer getMutualCount() {
        return mutualCount;
    }

    public void setMutualCount(Integer mutualCount) {
        this.mutualCount = mutualCount;
    }

    /** ID of the current viewer (when viewing someone else's profile). Used to hide Follow/Following for self in lists. */
    private Long currentUserId;

    public Long getCurrentUserId() {
        return currentUserId;
    }

    public void setCurrentUserId(Long currentUserId) {
        this.currentUserId = currentUserId;
    }
}
