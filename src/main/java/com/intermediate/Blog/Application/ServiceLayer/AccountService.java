package com.intermediate.Blog.Application.ServiceLayer;

import com.intermediate.Blog.Application.DtoLayers.PostDto;
import com.intermediate.Blog.Application.Exception.ResourceNotFoundException;
import com.intermediate.Blog.Application.Models.AccountVisibility;
import com.intermediate.Blog.Application.Models.User;
import com.intermediate.Blog.Application.Repositories.FollowerRepository;
import com.intermediate.Blog.Application.Repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountService {
    @Autowired
    private FollowerRepository  followerRepository;

    @Autowired
    private PostService postService;

    @Autowired
    private UserRepo userRepo;

    public boolean canViewFollowList(User viewer  , User owner){

//        first we will check if  the viewer is the owner itself

        if(viewer.getId() == owner.getId()){
            return true;
        }
//        Now we will see that if the owner's account is public
        else if(owner.getAccountVisibility() == AccountVisibility.PUBLIC){
            return true;
        }
//       Now we will see if the viewer follows the owner .
        else if(followerRepository.existsByFollowerAndFollowing(viewer  , owner)){
            return true;
        }

//        If any of the condition is not true means the user is not allowed to  see the follow or follower list of the user , so return false.
        return false;
    }

    public boolean canViewPosts(User viewer, User postOwner){

//        If the owner's account is Public , means the viewer can view the account's post .
        if(postOwner.getAccountVisibility() == AccountVisibility.PUBLIC){
            return true;
        }

//        If viewer is the Owner , so obviously he/she can view their posts
        if(viewer.getId() == postOwner.getId()){
            return true;
        }
//        if the postOwner's account is private , and the viewer follows the postOwner , then the viewer can see the posts.
        if(followerRepository.existsByFollowerAndFollowing(viewer ,  postOwner)){
            return true;
        }
//        If not a single condition matches , then the viewer do not have the access to view the post .
        return false;
    }

    public List<PostDto> getAllViewAblePosts(User currentUser){
        List<PostDto> posts = postService.getAllPosts();

        return posts.stream().filter(post->{
            User owner = userRepo.findById(post.getUser().getId()).orElseThrow(()-> new ResourceNotFoundException("User" , "id" , post.getUser().getId()));
            return canViewPosts(currentUser , owner);
                }).toList();

    }

    public Page<PostDto> getAllViewablePostsPaginated(User currentUser , int page , int size , String seed){
        return postService.getViewablePostPaginated(currentUser.getId() , page  , size ,seed);
    }

}
