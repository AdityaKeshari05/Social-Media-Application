package com.intermediate.Blog.Application.Controllers;

import com.intermediate.Blog.Application.DtoLayers.PostDto;
import com.intermediate.Blog.Application.DtoLayers.ProfileDto;
import com.intermediate.Blog.Application.Exception.ResourceNotFoundException;
import com.intermediate.Blog.Application.Models.Post;
import com.intermediate.Blog.Application.Models.User;
import com.intermediate.Blog.Application.Models.UserProfile;
import com.intermediate.Blog.Application.Repositories.UserRepo;
import com.intermediate.Blog.Application.ServiceLayer.CurrentUserService;
import com.intermediate.Blog.Application.ServiceLayer.ImageUploadService;
import com.intermediate.Blog.Application.ServiceLayer.PostService;
import com.intermediate.Blog.Application.ServiceLayer.ProfileService;
import com.intermediate.Blog.Application.ServiceLayer.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

    @Autowired
    private CurrentUserService currentUserService;

    @GetMapping("/me")
    public ResponseEntity<ProfileDto> getMyProfile(){
        User user = currentUserService.getCurrentUser();
        List<PostDto> posts = postService.getPostByUser(user.getId());
        return ResponseEntity.ok(profileService.buildProfileDto(user,user,posts)); // if i am the owner of the profile  then only will be able to see the profile.
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ProfileDto> getUserProfile(@PathVariable Long userId){
        User user = userRepo.findById(userId).orElseThrow(()-> new ResourceNotFoundException("User", "id",userId));
        List<PostDto> posts = postService.getPostByUser(userId);
        User viewer = currentUserService.getCurrentUser();
        return ResponseEntity.ok(profileService.buildProfileDto( user ,viewer, posts)); // if the user follows the viewer in case the viewer's account is private then only will be able to see the user's profile
    }



        @PutMapping("/bio")
        public ResponseEntity<ProfileDto> updateBio(
                @RequestBody Map<String ,String> body
                ){
            User user = currentUserService.getCurrentUser();
            String bio  = body.get("bio");
            List<PostDto> posts = postService.getPostByUser(user.getId());
            profileService.updateBio(user, bio);
            return ResponseEntity.ok(profileService.buildProfileDto(user,user,posts));
        }

    @PutMapping("/name")
    public ResponseEntity<ProfileDto> updateName(@RequestBody Map<String, String> body) {
        User user = currentUserService.getCurrentUser();
        String name = body.get("name");
        List<PostDto> posts = postService.getPostByUser(user.getId());
        profileService.updateName(user, name);
        return ResponseEntity.ok(profileService.buildProfileDto(user, user, posts));
    }

    @PutMapping("/website")
    public ResponseEntity<ProfileDto> updateWebsite(@RequestBody Map<String, String> body) {
        User user = currentUserService.getCurrentUser();
        String website = body.get("website");
        List<PostDto> posts = postService.getPostByUser(user.getId());
        profileService.updateWebsite(user, website);
        return ResponseEntity.ok(profileService.buildProfileDto(user, user, posts));
    }

    @PutMapping("/link2")
    public ResponseEntity<ProfileDto> updateLink2(@RequestBody Map<String, String> body) {
        User user = currentUserService.getCurrentUser();
        String link2 = body.get("link2");
        List<PostDto> posts = postService.getPostByUser(user.getId());
        profileService.updateLink2(user, link2);
        return ResponseEntity.ok(profileService.buildProfileDto(user, user, posts));
    }

    @PostMapping("/profile-picture")
    public ResponseEntity<ProfileDto> updateProfilePicture(@RequestParam MultipartFile file ){
        User user = currentUserService.getCurrentUser();
        String imageUrl = imageUploadService.uploadImage(file);
        profileService.updateProfilePicture(user,imageUrl);
        List<PostDto> posts = postService.getPostByUser(user.getId());
        return ResponseEntity.ok(profileService.buildProfileDto(user,user, posts)); // Same case as update bio
    }
}
