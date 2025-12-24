package com.intermediate.Blog.Application.ServiceLayer;

import com.intermediate.Blog.Application.DtoLayers.PostDto;
import com.intermediate.Blog.Application.DtoLayers.ProfileDto;
import com.intermediate.Blog.Application.Exception.ResourceNotFoundException;
import com.intermediate.Blog.Application.Models.User;
import com.intermediate.Blog.Application.Models.UserProfile;
import com.intermediate.Blog.Application.Repositories.PostRepository;
import com.intermediate.Blog.Application.Repositories.ProfileRepository;
import com.intermediate.Blog.Application.Repositories.UserRepo;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProfileService {

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ModelMapper modelMapper;

    public UserProfile getProfile(Long userId){
        UserProfile profile = profileRepository.findByUserId(userId).orElseThrow(
                ()-> new RuntimeException("Profile Not Found")
        );

        User user = userRepo.findById(userId).orElseThrow(()-> new RuntimeException("User Not Found"));

        profile.setNoOfPosts(postRepository.countByUser(user));
        return profile;
    }

    public UserProfile updateBio(String email , String bio){
        User user = (User) userRepo.findByEmail(email).orElseThrow(()-> new ResourceNotFoundException("User" , "email", email));
        UserProfile profile = profileRepository.findByUser(user).orElseThrow(
                ()-> new RuntimeException("Profile Not Found")
        );

        profile.setBio(bio);
        return profileRepository.save(profile);
    }
    public UserProfile updateProfilePicture(User user , String imagePath){
        UserProfile profile = profileRepository.findByUser(user).orElseThrow(
                ()-> new RuntimeException("Profile Not Found")
        );

        profile.setProfilePicture(imagePath);
        return profileRepository.save(profile);
    }
}
