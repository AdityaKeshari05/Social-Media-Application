package com.intermediate.Blog.Application.Controllers;

import com.intermediate.Blog.Application.DtoLayers.PostDto;
import com.intermediate.Blog.Application.DtoLayers.ProfileDto;
import com.intermediate.Blog.Application.Exception.ResourceNotFoundException;
import com.intermediate.Blog.Application.Models.Post;
import com.intermediate.Blog.Application.Models.User;
import com.intermediate.Blog.Application.Models.UserProfile;
import com.intermediate.Blog.Application.Repositories.UserRepo;
import com.intermediate.Blog.Application.ServiceLayer.ImageUploadService;
import com.intermediate.Blog.Application.ServiceLayer.PostService;
import com.intermediate.Blog.Application.ServiceLayer.ProfileService;
import com.intermediate.Blog.Application.ServiceLayer.UserService;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.data.repository.config.RepositoryConfigurationSource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private UserService userService;

    @Autowired
    private ImageUploadService imageUploadService;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PostService postService;


    @GetMapping("/me")
    public ResponseEntity<ProfileDto>  getMyProfile(){
       String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
       User user = (User) userRepo.findByEmail(currentUser).orElseThrow(()-> new RuntimeException("User not Found"));

        List<PostDto> posts = postService.getPostByUser(user.getId());
        UserProfile profile = profileService.getProfile(user.getId());

        return ResponseEntity.ok(new ProfileDto(
                profile.getUser().getUsername(),
                profile.getBio(),
                profile.getProfilePicture(),
                profile.getNoOfPosts(),
                posts
        ));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ProfileDto> getUserProfile(@PathVariable Long userId){
        User user = userRepo.findById(userId).orElseThrow(()-> new ResourceNotFoundException("User", "id",userId));
        List<PostDto> posts = postService.getPostByUser(userId);
        UserProfile profile = profileService.getProfile(userId);

        return ResponseEntity.ok(new ProfileDto(
                profile.getUser().getUsername(),
                profile.getBio(),
                profile.getProfilePicture(),
                profile.getNoOfPosts(),
                posts
        ));
    }



        @PutMapping("/bio")
        public ResponseEntity<ProfileDto> updateBio(
                @RequestBody Map<String ,String> body
                ){
            String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = (User) userRepo.findByEmail(currentUser).orElseThrow(()-> new RuntimeException("User not Found"));
            String bio  = body.get("bio");

            List<PostDto> posts = postService.getPostByUser(user.getId());
            UserProfile  profile = profileService.getProfile(user.getId());
            profile =  profileService.updateBio(currentUser , bio);


            return ResponseEntity.ok(new ProfileDto(
                    profile.getUser().getUsername(),
                    profile.getBio(),
                    profile.getProfilePicture(),
                    profile.getNoOfPosts(),
                    posts
            ));
        }

    @PostMapping("/profile-picture")
    public ResponseEntity<ProfileDto> updateProfilePicture(@RequestParam MultipartFile file ){
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = (User) userRepo.findByEmail(currentUser).orElseThrow(()-> new RuntimeException("User not Found"));
        String imageUrl = imageUploadService.uploadImage(file);
        profileService.updateProfilePicture(user,imageUrl);
        List<PostDto> posts = postService.getPostByUser(user.getId());
        UserProfile  profile = profileService.getProfile(user.getId());

        return ResponseEntity.ok(new ProfileDto(
                profile.getUser().getUsername(),
                profile.getBio(),
                profile.getProfilePicture(),
                profile.getNoOfPosts(),
                posts
        ));
    }
  }
