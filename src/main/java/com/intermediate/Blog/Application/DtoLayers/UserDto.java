package com.intermediate.Blog.Application.DtoLayers;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;




public class UserDto {

    private Long id;
    private String username;
    private String email;
    private com.intermediate.Blog.Application.Models.AccountVisibility accountVisibility;
    private String profilePicture;

    public UserDto(Long id, String username, String email, com.intermediate.Blog.Application.Models.AccountVisibility accountVisibility) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.accountVisibility = accountVisibility;
    }

    public UserDto(Long id, String username, String email, com.intermediate.Blog.Application.Models.AccountVisibility accountVisibility, String profilePicture) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.accountVisibility = accountVisibility;
        this.profilePicture = profilePicture;
    }

    public UserDto(){

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public com.intermediate.Blog.Application.Models.AccountVisibility getAccountVisibility() {
        return accountVisibility;
    }

    public void setAccountVisibility(com.intermediate.Blog.Application.Models.AccountVisibility accountVisibility) {
        this.accountVisibility = accountVisibility;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }
}
