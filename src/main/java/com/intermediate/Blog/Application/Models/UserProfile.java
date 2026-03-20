package com.intermediate.Blog.Application.Models;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.List;

@Entity
public class UserProfile {

    public UserProfile(){}


    public UserProfile(long id, User user, String bio, String profilePicture,  long noOfPosts) {
        this.id = id;
        this.user = user;
        this.bio = bio;
        this.profilePicture = profilePicture;
        this.noOfPosts = noOfPosts;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String bio;
    private String profilePicture;

    @Column(length = 100)
    private String name;

    @Column(length = 500)
    private String website;

    @Column(name = "link2", length = 500)
    private String link2;

    @Transient
    private long noOfPosts;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public long getNoOfPosts() {
        return noOfPosts;
    }

    public void setNoOfPosts(long noOfPosts) {
        this.noOfPosts = noOfPosts;
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
}
