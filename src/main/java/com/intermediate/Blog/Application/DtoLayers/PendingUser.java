package com.intermediate.Blog.Application.DtoLayers;

import com.intermediate.Blog.Application.Models.User;

public class PendingUser {
   private User userData;
   private String otp;

    public PendingUser(User userData, String otp) {
        this.userData = userData;
        this.otp = otp;
    }

    public PendingUser(){}


    public User getUserData() {
        return userData;
    }

    public void setUserData(User userData) {
        this.userData = userData;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}
