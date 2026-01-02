package com.intermediate.Blog.Application.Controllers;


import com.intermediate.Blog.Application.Exception.ResourceNotFoundException;
import com.intermediate.Blog.Application.Models.User;
import com.intermediate.Blog.Application.Repositories.UserRepo;
import com.intermediate.Blog.Application.ServiceLayer.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/account")
public class AccountVisibilityController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepo userRepo;

    @PutMapping("/private")
    public ResponseEntity<String>  makePrivate(){
        User user = getCurrentUser();
        userService.makeAccountPrivate(user);

        return ResponseEntity.ok("Account is Private now !!");

    }

    @PutMapping("/public")
    public ResponseEntity<String> makePublic(){
        User user  = getCurrentUser();
        userService.makeAccountPublic(user);
        return ResponseEntity.ok("Account is Public now !!");
    }


    private User getCurrentUser(){
        String email  = SecurityContextHolder.getContext().getAuthentication().getName();
        User user  = (User) userRepo.findByEmail(email).orElseThrow(()-> new ResourceNotFoundException("User"  ,  "email" , email));
        return user;
    }
}
