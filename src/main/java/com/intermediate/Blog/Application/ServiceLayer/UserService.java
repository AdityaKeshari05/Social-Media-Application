package com.intermediate.Blog.Application.ServiceLayer;

import com.intermediate.Blog.Application.DtoLayers.UserDto;
import com.intermediate.Blog.Application.DtoLayers.UserPreview;
import com.intermediate.Blog.Application.Models.User;

import java.util.List;

public interface UserService {

    /** Search users by username (partial match, case-insensitive). Returns at most 20 previews. */
    List<UserPreview> searchByUsername(String query);

    UserDto createUser(User user);
    UserDto updateUser(User user, Long userId);
    UserDto getUserById(Long userId);
    List<UserDto> getAllUsers();
    User getUserByEmail(String email);
    void deleteUser(Long userId);
    void makeAccountPublic(User user);
    void makeAccountPrivate(User user);

    /** Check if a username is available (case-insensitive). */
    boolean isUsernameAvailable(String username, Long currentUserId);
}
