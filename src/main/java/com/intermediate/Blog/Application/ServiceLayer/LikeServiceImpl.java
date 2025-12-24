package com.intermediate.Blog.Application.ServiceLayer;

import ch.qos.logback.core.encoder.EchoEncoder;
import com.intermediate.Blog.Application.DtoLayers.UserDto;
import com.intermediate.Blog.Application.Exception.ResourceNotFoundException;
import com.intermediate.Blog.Application.Models.Like;
import com.intermediate.Blog.Application.Models.Post;
import com.intermediate.Blog.Application.Models.User;
import com.intermediate.Blog.Application.Repositories.LikeRepository;
import com.intermediate.Blog.Application.Repositories.PostRepository;
import com.intermediate.Blog.Application.Repositories.UserRepo;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LikeServiceImpl implements LikeService {

    @Autowired
    private LikeRepository likeRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PostRepository postRepo;



    @Override
    public Like likePost(long postId , long userId){
        User user = userRepo.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User","id",userId));
        Post post  = postRepo.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post","id",postId));

        if(likeRepo.existsByUserAndPost(user , post)){
            throw new RuntimeException("User already liked the post");
        }

        Like like = new Like();
        like.setUser(user);
        like.setPost(post);
        like.setLikedAt(LocalDateTime.now());
        like = likeRepo.save(like);
        return like;
    }

    @Override
    public void unlikePost(long postId , long userId){
        User user = userRepo.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User","id",userId));
        Post post  = postRepo.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post","id",postId));

        Like like  = likeRepo.findByUserAndPost(user,post).orElseThrow(()->new RuntimeException("Like not found"));
        likeRepo.delete(like);

    }

    @Override
    public Long getLikesCount(long postId) {
        Post post  = postRepo.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post","id",postId));
        return likeRepo.countByPost(post);
    }

    @Override
    public boolean hasUserLiked(long postId ,long userId){
        User user = userRepo.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User","id",userId));
        Post post  = postRepo.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post","id",postId));

        return likeRepo.existsByUserAndPost(user , post);
    }

    @Override
    public List<UserDto> getUsersWhoLikedPost(long postId){
        Post post  = postRepo.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post","id",postId));
        List<Like> likes = likeRepo.findAllByPost(post);

        return likes.stream()
                .map(like -> new UserDto(
                        like.getUser().getId(),
                        like.getUser().getUsername(),
                        like.getUser().getEmail()
                ))
                .collect(Collectors.toList());
        }

    }

