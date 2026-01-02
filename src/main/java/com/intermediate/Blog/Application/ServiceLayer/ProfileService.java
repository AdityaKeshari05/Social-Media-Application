package com.intermediate.Blog.Application.ServiceLayer;

import com.intermediate.Blog.Application.DtoLayers.PostDto;
import com.intermediate.Blog.Application.DtoLayers.ProfileDto;
import com.intermediate.Blog.Application.Exception.ResourceNotFoundException;
import com.intermediate.Blog.Application.Models.AccountVisibility;
import com.intermediate.Blog.Application.Models.User;
import com.intermediate.Blog.Application.Models.UserProfile;
import com.intermediate.Blog.Application.Repositories.FollowerRepository;
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

    @Autowired
    private FollowerRepository followerRepository;



    public UserProfile getProfile(Long userId){
        UserProfile profile = profileRepository.findByUserId(userId).orElseThrow(
                ()-> new RuntimeException("Profile Not Found")
        );

        User user = userRepo.findById(userId).orElseThrow(()-> new RuntimeException("User Not Found"));

        profile.setNoOfPosts(postRepository.countByUser(user));
        return profile;
    }

    public UserProfile updateBio(User user, String bio){

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


    public ProfileDto buildProfileDto(User profileOwner, User viewer, List<PostDto> posts) {

        UserProfile profile = getProfile(profileOwner.getId());
        boolean isOwner = profileOwner.getId()==viewer.getId();
        boolean isPublic = profileOwner.getAccountVisibility()==AccountVisibility.PUBLIC;
        boolean isFollower = !isOwner && followerRepository.existsByFollowerAndFollowing(viewer,profileOwner);
        boolean canViewPosts = isOwner || isFollower || isPublic ;   // this states that if the user is the owner, or the user follows the viewer or the viewer's account is public then we can see the profile of the viewer .
        if(!canViewPosts){
            posts = List.of();    // Here we are Hiding the posts.
        }

        ProfileDto dto = new ProfileDto();
        dto.setUsername(profile.getUser().getUsername());
        dto.setBio(profile.getBio());
        dto.setProfilePicture(profile.getProfilePicture());
        dto.setNoOfPosts(profile.getNoOfPosts());
        dto.setPosts(posts);

        // account visibility mapping
        dto.setPrivate(
                profileOwner.getAccountVisibility() == AccountVisibility.PRIVATE // here we are setting up the account visibility . if true then only followers can see the profile , if false  then anyone can see the profile .
        );

        return dto;
    }
}
