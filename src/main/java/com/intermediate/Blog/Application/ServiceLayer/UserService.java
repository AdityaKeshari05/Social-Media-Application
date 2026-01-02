package com.intermediate.Blog.Application.ServiceLayer;

import com.intermediate.Blog.Application.DtoLayers.UserDto;
import com.intermediate.Blog.Application.Models.User;   // <-- RIGHT IMPORT

import java.util.List;

public interface UserService {

    UserDto createUser(User user);           // entity input
    UserDto updateUser(User user, Long userId);
    UserDto getUserById(Long userId);
    List<UserDto> getAllUsers();
    User getUserByEmail(String email);
    void deleteUser(Long userId);
    void makeAccountPublic(User user);
    void makeAccountPrivate(User user);
}
