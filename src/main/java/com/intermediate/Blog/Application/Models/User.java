package com.intermediate.Blog.Application.Models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    @Column(nullable = false)
    private String username;

    @Column(unique = true , nullable = true)
    private String email;

    private String password ;

    private String role;

    @OneToMany(mappedBy = "user" , cascade = CascadeType.ALL)
    private List<Post> posts ;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Like> likes;

    private String otp;
    private LocalDateTime otpExpiry;
    private boolean verified = false;

    private String loginOtp;
    private LocalDateTime loginOtpTime;

    @OneToOne(mappedBy = "user" , cascade = CascadeType.ALL)
    private UserProfile userProfile;


    public UserProfile getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
    }

    public String getLoginOtp() {
        return loginOtp;
    }

    public void setLoginOtp(String loginOtp) {
        this.loginOtp = loginOtp;
    }

    public LocalDateTime getLoginOtpTime() {
        return loginOtpTime;
    }

    public void setLoginOtpTime(LocalDateTime loginOtpTime) {
        this.loginOtpTime = loginOtpTime;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<Post> getPosts() {
        return posts;
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
    }

    public List<Like> getLikes() {
        return likes;
    }

    public void setLikes(List<Like> likes) {
        this.likes = likes;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public LocalDateTime getOtpExpiry() {
        return otpExpiry;
    }

    public void setOtpExpiry(LocalDateTime otpExpiry) {
        this.otpExpiry = otpExpiry;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }
}
