package com.intermediate.Blog.Application.ServiceLayer;

import com.intermediate.Blog.Application.DtoLayers.UserDto;
import com.intermediate.Blog.Application.Exception.ResourceNotFoundException;
import com.intermediate.Blog.Application.Models.AccountVisibility;
import com.intermediate.Blog.Application.Models.User;
import com.intermediate.Blog.Application.Repositories.UserRepo;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepo repo;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;



    @Override
    public UserDto createUser(User userRequest) {

        // Password encode only on creation
        userRequest.setPassword(passwordEncoder.encode(userRequest.getPassword()));

        User savedUser = repo.save(userRequest);

        return modelMapper.map(savedUser, UserDto.class);
    }



    @Override
    public UserDto updateUser(User userRequest, Long userId) {

        User user = repo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Only fields you want to update
        user.setUsername(userRequest.getUsername());
        user.setEmail(userRequest.getEmail());


        if (userRequest.getPassword() != null && !userRequest.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        }

        User updatedUser = repo.save(user);

        return modelMapper.map(updatedUser, UserDto.class);
    }


    @Override
    public UserDto getUserById(Long userId) {

        User user = repo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return modelMapper.map(user, UserDto.class);
    }


    @Override
    public List<UserDto> getAllUsers() {

        return repo.findAll()
                .stream()
                .map(user -> modelMapper.map(user, UserDto.class))
                .collect(Collectors.toList());
    }



    @Override
    public void deleteUser(Long userId) {

        User user = repo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        repo.delete(user);
    }

    @Override
    public void makeAccountPublic(User user) {
        if (user.getAccountVisibility() == AccountVisibility.PUBLIC){
            throw new IllegalStateException("Account is already Public");
        }
        user.setAccountVisibility(AccountVisibility.PUBLIC);
        repo.save(user);
    }

    @Override
    public void makeAccountPrivate(User user) {
        if(user.getAccountVisibility() == AccountVisibility.PRIVATE){
            throw new IllegalStateException("Account is already Private");
        }

        user.setAccountVisibility(AccountVisibility.PRIVATE);
        repo.save(user);
    }

    @Override
    public User getUserByEmail(String email){
        User user  = (User) repo.findByEmail(email).orElseThrow(()-> new ResourceNotFoundException("User","email" , email));
        return user;
    }

}
