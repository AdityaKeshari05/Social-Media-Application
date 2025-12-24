package com.intermediate.Blog.Application.Controllers;


import com.intermediate.Blog.Application.DtoLayers.UserDto;
import com.intermediate.Blog.Application.Models.User;
import com.intermediate.Blog.Application.ServiceLayer.UserService;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> createUser(@RequestBody User userDto){
        UserDto createdUser = userService.createUser(userDto);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @PutMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDto> updateUser(@RequestBody User user , @PathVariable Long userId){
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User  currentUser =  userService.getUserByEmail(currentUserEmail);
        if(currentUser.getId() != userId.longValue() && !currentUser.getRole().equals("ADMIN")) {
            throw new RuntimeException("Access Denied");
        }
        UserDto updatedUser = userService.updateUser(user, currentUser.getId());
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable Long userId){
        userService.deleteUser(userId);
        return new ResponseEntity<>("User Deleted Successfully",HttpStatus.OK);
    }

    @GetMapping("/")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> getAllUsers(){
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long userId){
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.getUserByEmail(currentUserEmail);

        if(currentUser.getId() != userId.longValue() && !currentUser.getRole().equals("ADMIN")) {
            throw new RuntimeException("Access Denied");
        }

        return ResponseEntity.ok(userService.getUserById(userId));
    }

}
