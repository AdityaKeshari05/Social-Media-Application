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


    private String bio ;
    private String profilePicture ;



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

}
