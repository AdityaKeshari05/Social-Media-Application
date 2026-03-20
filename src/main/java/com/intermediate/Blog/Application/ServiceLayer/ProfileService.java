package com.intermediate.Blog.Application.ServiceLayer;

import com.intermediate.Blog.Application.DtoLayers.PostDto;
import com.intermediate.Blog.Application.DtoLayers.ProfileDto;
import com.intermediate.Blog.Application.DtoLayers.UserPreview;
import com.intermediate.Blog.Application.DtoLayers.UserPreviewWithFollowStatus;
import com.intermediate.Blog.Application.Exception.ResourceNotFoundException;
import com.intermediate.Blog.Application.Models.AccountVisibility;
import com.intermediate.Blog.Application.Models.User;
import com.intermediate.Blog.Application.Models.UserProfile;
import com.intermediate.Blog.Application.Repositories.FollowerRepository;
import com.intermediate.Blog.Application.Repositories.FollowRequestRepository;
import com.intermediate.Blog.Application.Repositories.PostRepository;
import com.intermediate.Blog.Application.Repositories.ProfileRepository;
import com.intermediate.Blog.Application.Repositories.UserRepo;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    @Autowired
    private FollowRequestRepository followRequestRepository;

    @Autowired
    private FollowService followService;



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

    public UserProfile updateName(User user, String name) {
        UserProfile profile = profileRepository.findByUser(user).orElseThrow(
                () -> new RuntimeException("Profile Not Found")
        );
        profile.setName(name != null ? name.trim() : null);
        return profileRepository.save(profile);
    }

    public UserProfile updateWebsite(User user, String website) {
        UserProfile profile = profileRepository.findByUser(user).orElseThrow(
                () -> new RuntimeException("Profile Not Found")
        );
        profile.setWebsite(website != null ? website.trim() : null);
        return profileRepository.save(profile);
    }

    public UserProfile updateLink2(User user, String link2) {
        UserProfile profile = profileRepository.findByUser(user).orElseThrow(
                () -> new RuntimeException("Profile Not Found")
        );
        profile.setLink2(link2 != null ? link2.trim() : null);
        return profileRepository.save(profile);
    }

    public ProfileDto buildProfileDto(User profileOwner, User viewer, List<PostDto> posts) {

        UserProfile profile = getProfile(profileOwner.getId());
        boolean isOwner = profileOwner.getId()==viewer.getId();
        boolean isPublic = profileOwner.getAccountVisibility()==AccountVisibility.PUBLIC;
        boolean isFollower = !isOwner && followerRepository.existsByFollowerAndFollowing(viewer,profileOwner);
        boolean hasPendingRequest = !isOwner && !isFollower
                && profileOwner.getAccountVisibility() == AccountVisibility.PRIVATE
                && followRequestRepository.existsByRequesterAndTarget(viewer, profileOwner);
        boolean canViewPosts = isOwner || isFollower || isPublic ;   // this states that if the user is the owner, or the user follows the viewer or the viewer's account is public then we can see the profile of the viewer .
        if(!canViewPosts){
            posts = List.of();    // Here we are Hiding the posts.
        }

        ProfileDto dto = new ProfileDto();
        dto.setUserId(profileOwner.getId());
        dto.setUsername(profile.getUser().getUsername());
        dto.setName(profile.getName());
        dto.setWebsite(profile.getWebsite());
        dto.setLink2(profile.getLink2());
        dto.setBio(profile.getBio());
        dto.setProfilePicture(profile.getProfilePicture());
        dto.setNoOfPosts(profile.getNoOfPosts());
        dto.setPosts(posts);

        long followersCount = followerRepository.countFollowers(profileOwner.getId());
        long followingCount = followerRepository.countFollowing(profileOwner.getId());
        dto.setFollowersCount(followersCount);
        dto.setFollowingCount(followingCount);

        // account visibility mapping
        dto.setPrivate(
                profileOwner.getAccountVisibility() == AccountVisibility.PRIVATE
        );
        dto.setFollowing(isFollower);
        dto.setFollowRequested(hasPendingRequest);

        // Mutual followers preview (at most 2) and count: "Followed by X, Y and more" when viewing someone else's profile
        if (!isOwner) {
            List<UserPreviewWithFollowStatus> mutual = followService.getMutualFollowers(profileOwner.getId());
            dto.setMutualCount(mutual.size());
            List<UserPreview> preview = mutual.stream()
                    .limit(2)
                    .map(m -> new UserPreview(m.getId(), m.getUsername(), m.getProfilePicture()))
                    .toList();
            dto.setMutualPreview(preview);
            dto.setCurrentUserId(viewer.getId());
        }

        return dto;
    }
}
