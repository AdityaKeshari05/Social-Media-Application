package com.intermediate.Blog.Application.Controllers;


import com.intermediate.Blog.Application.DtoLayers.UserDto;
import com.intermediate.Blog.Application.DtoLayers.UserPreview;
import com.intermediate.Blog.Application.Exception.AccessControlException;
import com.intermediate.Blog.Application.Models.User;
import com.intermediate.Blog.Application.ServiceLayer.CurrentUserService;
import com.intermediate.Blog.Application.ServiceLayer.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private CurrentUserService currentUserService;

    @PostMapping("/")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> createUser(@RequestBody User userDto){
        UserDto createdUser = userService.createUser(userDto);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @PutMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDto> updateUser(@RequestBody User user , @PathVariable Long userId){
        User currentUser = currentUserService.getCurrentUser();
        if(currentUser.getId() != userId.longValue() && !currentUser.getRole().equals("ADMIN")) {
            throw new AccessControlException("Access denied");
        }
        UserDto updatedUser = userService.updateUser(user, userId);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/username-available")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> usernameAvailable(@RequestParam("username") String username) {
        User currentUser = currentUserService.getCurrentUser();
        boolean available = userService.isUsernameAvailable(username, currentUser.getId());
        return ResponseEntity.ok(java.util.Map.of("available", available));
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

    /** Search users by username (partial, case-insensitive). Returns at most 20 previews. */
    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<UserPreview>> searchUsers(@RequestParam(value = "q", required = false) String query) {
        return ResponseEntity.ok(userService.searchByUsername(query != null ? query : ""));
    }

    @GetMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long userId){
        User currentUser = currentUserService.getCurrentUser();

        if(currentUser.getId() != userId.longValue() && !currentUser.getRole().equals("ADMIN")) {
            throw new AccessControlException("Access denied");
        }

        return ResponseEntity.ok(userService.getUserById(userId));
    }

}
