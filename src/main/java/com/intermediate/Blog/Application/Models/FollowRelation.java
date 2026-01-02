package com.intermediate.Blog.Application.Models;


import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "followers",
        uniqueConstraints = {
        @UniqueConstraint(columnNames = {"follower_id" , "following_id"})
        })
public class FollowRelation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;


    @ManyToOne
    @JoinColumn(name = "follower_id")
    private User follower;

    @ManyToOne
    @JoinColumn(name = "following_id")
    private User following;

    private LocalDateTime createdAt;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public User getFollower() {
        return follower;
    }

    public void setFollower(User follower) {
        this.follower = follower;
    }

    public User getFollowing() {
        return following;
    }

    public void setFollowing(User following) {
        this.following = following;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
