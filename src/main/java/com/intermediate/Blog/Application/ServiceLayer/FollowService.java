package com.intermediate.Blog.Application.ServiceLayer;


import com.intermediate.Blog.Application.DtoLayers.UserPreview;
import com.intermediate.Blog.Application.DtoLayers.UserPreviewWithFollowStatus;
import com.intermediate.Blog.Application.Exception.ResourceNotFoundException;
import com.intermediate.Blog.Application.Models.*;
import com.intermediate.Blog.Application.Repositories.FollowRequestRepository;
import com.intermediate.Blog.Application.Repositories.FollowerRepository;
import com.intermediate.Blog.Application.Repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class FollowService {

    @Autowired
    private FollowerRepository followerRepository;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private FollowRequestRepository followRequestRepository;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private NotificationService notificationService;

    public void followUser(Long targetUserId){
        User currentUser = currentUserService.getCurrentUser();
        User targetUser = userRepo.findById(targetUserId).orElseThrow(()-> new ResourceNotFoundException("User" , "id" , targetUserId));

        if(currentUser.getId() == targetUser.getId()){
            throw new IllegalStateException("Cannot follow yourself");
        }

        if(followerRepository.existsByFollowerAndFollowing(currentUser  , targetUser)){
            throw new IllegalStateException("Already Following");
        }
        if(targetUser.getAccountVisibility() == AccountVisibility.PRIVATE){
            if(followRequestRepository.existsByRequesterAndTarget(currentUser ,targetUser)){
                throw new  IllegalStateException("Follow request already sent");
            }

            FollowRequest request = new FollowRequest();
            request.setRequester(currentUser);
            request.setTarget(targetUser);
            request.setRequestStatus(RequestStatus.PENDING);
            request.setCreatedAt(LocalDateTime.now());

            followRequestRepository.save(request);
            notificationService.createFollowRequestNotification(targetUser , currentUser);

            return;

        }


        createFollower(currentUser ,targetUser);
        notificationService.createFollowedNotification(targetUser , currentUser);
    }

    public void unfollowUser(Long targetUserId){
        User currentUser = currentUserService.getCurrentUser();

        User targetUser = userRepo.findById(targetUserId).orElseThrow(()-> new ResourceNotFoundException("User" , "id" , targetUserId));

        int deleted = followerRepository.deleteByFollowerIdAndFollowingId(currentUser.getId(), targetUser.getId());
        if (deleted == 0) {
            throw new IllegalStateException("You do not follow this user");
        }
    }

    public void removeFollower(Long followerId) {
        User currentUser = currentUserService.getCurrentUser();
        User follower = userRepo.findById(followerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", followerId));

        int deleted = followerRepository.deleteByFollowerIdAndFollowingId(follower.getId(), currentUser.getId());
        if (deleted == 0) {
            throw new IllegalStateException("This user is not following you");
        }
    }

    /** Cancel a pending follow request that the current user sent to targetUserId (private accounts). */
    public void cancelFollowRequest(Long targetUserId) {
        User currentUser = currentUserService.getCurrentUser();
        User targetUser = userRepo.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", targetUserId));

        FollowRequest request = followRequestRepository.findByRequesterAndTarget(currentUser, targetUser)
                .orElseThrow(() -> new ResourceNotFoundException("FollowRequest", "targetUserId", targetUserId));

        if (request.getRequestStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("Request is not pending");
        }

        followRequestRepository.delete(request);
    }

    public List<UserPreview> getFollowers() {
        User currentUser = currentUserService.getCurrentUser();
        return followerRepository.findByFollowing(currentUser)
                .stream().map(
                        f-> new UserPreview(f.getFollower().getId() ,
                                f.getFollower().getUsername(),
                                f.getFollower().getUserProfile().getProfilePicture())
                ).toList();
    }
    public List<UserPreview> getFollowing() {
        User currentUser = currentUserService.getCurrentUser();
        return followerRepository.findByFollower(currentUser)
                .stream().map(
                        f-> new UserPreview(f.getFollowing().getId() ,
                                f.getFollowing().getUsername(),
                                f.getFollowing().getUserProfile().getProfilePicture())
                ).toList();

    }

    public List<UserPreview> getFollowersOfUser(Long userId) {
        User owner = userRepo.findById(userId).orElseThrow(()-> new ResourceNotFoundException("User" , "id" , userId));
        return followerRepository.findByFollowing(owner)
                .stream()
                .map(f -> {
                    User follower = f.getFollower();
                    String pic = follower.getUserProfile() == null
                            ? "default.png"
                            : follower.getUserProfile().getProfilePicture();

                    UserPreview preview = new UserPreview(
                            follower.getId(),
                            follower.getUsername(),
                            pic
                    );
                    if (follower.getUserProfile() != null) {
                        preview.setName(follower.getUserProfile().getName());
                    }
                    return preview;
                }).toList();
    }

    public List<UserPreviewWithFollowStatus> getFollowersOfUserWithFollowStatus(Long userId) {
        User owner = userRepo.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        User currentUser = currentUserService.getCurrentUser();
        return followerRepository.findByFollowing(owner)
                .stream()
                .map(f -> {
                    User follower = f.getFollower();
                    String pic = follower.getUserProfile() == null ? "default.png" : follower.getUserProfile().getProfilePicture();
                    boolean following = follower.getId() != currentUser.getId()
                            && followerRepository.existsByFollowerAndFollowing(currentUser, follower);
                    return new UserPreviewWithFollowStatus(follower.getId(), follower.getUsername(), pic, following);
                })
                .toList();
    }

    public List<UserPreviewWithFollowStatus> getFollowingOfUserWithFollowStatus(Long userId) {
        User owner = userRepo.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        User currentUser = currentUserService.getCurrentUser();
        return followerRepository.findByFollower(owner)
                .stream()
                .map(f -> {
                    User followingUser = f.getFollowing();
                    String pic = followingUser.getUserProfile() == null ? "default.png" : followingUser.getUserProfile().getProfilePicture();
                    boolean following = followingUser.getId() != currentUser.getId()
                            && followerRepository.existsByFollowerAndFollowing(currentUser, followingUser);
                    return new UserPreviewWithFollowStatus(followingUser.getId(), followingUser.getUsername(), pic, following);
                })
                .toList();
    }

    /** Mutual followers: followers of profileUserId that the current user also follows. */
    public List<UserPreviewWithFollowStatus> getMutualFollowers(Long profileUserId) {
        User profileOwner = userRepo.findById(profileUserId).orElseThrow(() -> new ResourceNotFoundException("User", "id", profileUserId));
        User currentUser = currentUserService.getCurrentUser();
        if (profileOwner.getId() == currentUser.getId()) {
            return List.of();
        }
        List<UserPreviewWithFollowStatus> result = new ArrayList<>();
        for (FollowRelation fr : followerRepository.findByFollowing(profileOwner)) {
            User follower = fr.getFollower();
            if (follower.getId() == currentUser.getId()) continue;
            if (!followerRepository.existsByFollowerAndFollowing(currentUser, follower)) continue;
            String pic = follower.getUserProfile() == null ? "default.png" : follower.getUserProfile().getProfilePicture();
            result.add(new UserPreviewWithFollowStatus(follower.getId(), follower.getUsername(), pic, true));
        }
        return result;
    }

    public List<UserPreview> getFollowingOfUser(Long userId) {
        User owner = userRepo.findById(userId).orElseThrow(()-> new ResourceNotFoundException("User" , "id" , userId));
        return followerRepository.findByFollower(owner)
                .stream()
                .map(f -> {
                    User follower = f.getFollowing();
                    String pic = follower.getUserProfile() == null
                            ? "default.png"
                            : follower.getUserProfile().getProfilePicture();

                    UserPreview preview = new UserPreview(
                            follower.getId(),
                            follower.getUsername(),
                            pic
                    );
                    if (follower.getUserProfile() != null) {
                        preview.setName(follower.getUserProfile().getName());
                    }
                    return preview;
                }).toList();
    }

    public String acceptFollowRequest(Long requesterId){
        User currentUser = currentUserService.getCurrentUser();

        FollowRequest request = followRequestRepository
                .findByRequesterIdAndTargetId(requesterId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("FollowRequest", "requesterId", requesterId));

        User requestedUser = request.getRequester();
        createFollower(requestedUser , currentUser);
        followRequestRepository.delete(request);
        notificationService.createFollowRequestAcceptedNotification(requestedUser , currentUser);

        return "Request accepted";
    }

    public String rejectFollowRequest(Long requesterId){
        User currentUser = currentUserService.getCurrentUser();
        FollowRequest request = followRequestRepository
                .findByRequesterIdAndTargetId(requesterId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("FollowRequest", "requesterId", requesterId));
        followRequestRepository.delete(request);
        return "Request Rejected";

    }

    private void createFollower(User currentUser , User targetUser){
        FollowRelation followRelation = new FollowRelation();
        followRelation.setFollower(currentUser);
        followRelation.setFollowing(targetUser);
        followRelation.setCreatedAt(LocalDateTime.now());
        followerRepository.save(followRelation);

    }

}



