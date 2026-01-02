package com.intermediate.Blog.Application.Models;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "follow_requests",
        uniqueConstraints = {
        @UniqueConstraint(columnNames = {"requester_id" , "target_id"})
        }
        )
public class FollowRequest {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "requester_id" , nullable = false)
    private User requester;

    @ManyToOne
    @JoinColumn(name = "target_id" , nullable = false)
    private User target;

    @Enumerated(EnumType.STRING)
    private RequestStatus requestStatus;


    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public User getRequester() {
        return requester;
    }

    public User getTarget() {
        return target;
    }

    public RequestStatus getRequestStatus() {
        return requestStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setRequester(User requester) {
        this.requester = requester;
    }

    public void setTarget(User target) {
        this.target = target;
    }

    public void setRequestStatus(RequestStatus requestStatus) {
        this.requestStatus = requestStatus;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
