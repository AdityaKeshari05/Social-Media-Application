package com.intermediate.Blog.Application.ServiceLayer;


import com.intermediate.Blog.Application.DtoLayers.UserPreview;
import com.intermediate.Blog.Application.Exception.ResourceNotFoundException;
import com.intermediate.Blog.Application.Models.*;
import com.intermediate.Blog.Application.Repositories.FollowRequestRepository;
import com.intermediate.Blog.Application.Repositories.FollowerRepository;
import com.intermediate.Blog.Application.Repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FollowService {

    @Autowired
    private FollowerRepository followerRepository;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private FollowRequestRepository followRequestRepository;

    public void followUser(Long targetUserId){
        User currentUser  = getCurrentUser();
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
            return;

        }


        createFollower(currentUser ,targetUser);
    }

    public void unfollowUser(Long targetUserId){
        User currentUser  = getCurrentUser();

        User targetUser = userRepo.findById(targetUserId).orElseThrow(()-> new ResourceNotFoundException("User" , "id" , targetUserId));

        if(!followerRepository.existsByFollowerAndFollowing(currentUser , targetUser)){
            throw new IllegalStateException("You do not follow this user");
        }

        followerRepository.deleteByFollowerAndFollowing(currentUser , targetUser);
    }

    public List<UserPreview> getFollower(){
        User currentUser  = getCurrentUser();

        return followerRepository.findByFollowing(currentUser)
                .stream().map(
                        f-> new UserPreview(f.getFollower().getId() ,
                                f.getFollower().getUsername(),
                                f.getFollower().getUserProfile().getProfilePicture())
                ).toList();
    }
    public List<UserPreview> getFollowing(){
        User currentUser  = getCurrentUser();

        return followerRepository.findByFollower(currentUser)
                .stream().map(
                        f-> new UserPreview(f.getFollowing().getId() ,
                                f.getFollowing().getUsername(),
                                f.getFollowing().getUserProfile().getProfilePicture())
                ).toList();

    }

    public String acceptFollowRequest(Long requesterId){
        User currentUser = getCurrentUser();

        FollowRequest request = followRequestRepository.findByRequesterId(requesterId);
        if(request == null){
            throw new ResourceNotFoundException("Request","id",requesterId);
        }
        if(request.getTarget().getId()!=currentUser.getId()){
            throw new IllegalStateException("Not authorized");
        }

        User requestedUser = request.getRequester();
        createFollower(requestedUser , currentUser);
        followRequestRepository.delete(request);

        return "Request accepted";
    }

    public String rejectFollowRequest(Long requesterId){
        User currentUser = getCurrentUser();
        FollowRequest request = followRequestRepository.findByRequesterId(requesterId);
        if(request == null){
            throw new ResourceNotFoundException("Request","id",requesterId);
        }
        if(request.getTarget().getId()!=currentUser.getId()){
            throw new IllegalStateException("Not authorized");
        }
        followRequestRepository.delete(request);
        return "Request Rejected";

    }

    private User getCurrentUser(){
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = (User) userRepo.findByEmail(currentUser).orElseThrow(()-> new RuntimeException("User not Found"));
        return user;
    }

    private void createFollower(User currentUser , User targetUser){
        FollowRelation followRelation = new FollowRelation();
        followRelation.setFollower(currentUser);
        followRelation.setFollowing(targetUser);
        followRelation.setCreatedAt(LocalDateTime.now());
        followerRepository.save(followRelation);

    }

}



