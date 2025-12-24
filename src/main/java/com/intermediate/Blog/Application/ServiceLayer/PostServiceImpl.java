package com.intermediate.Blog.Application.ServiceLayer;


import com.intermediate.Blog.Application.DtoLayers.PostDto;
import com.intermediate.Blog.Application.DtoLayers.UserDto;
import com.intermediate.Blog.Application.Exception.ResourceNotFoundException;
import com.intermediate.Blog.Application.Models.Category;
import com.intermediate.Blog.Application.Models.Like;
import com.intermediate.Blog.Application.Models.Post;
import com.intermediate.Blog.Application.Models.User;
import com.intermediate.Blog.Application.Repositories.*;
import org.hibernate.boot.jaxb.hbm.internal.CacheAccessTypeConverter;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl implements PostService{

    @Autowired
    private PostRepository postRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private CategoryRepository categoryRepo;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private LikeRepository likeRepository;



    @Override
    public PostDto createPost(PostDto postDto , Long userId){
        User user = userRepo.findById(userId)
                .orElseThrow(()-> new ResourceNotFoundException("User","id",userId));

        Category category = categoryRepo.findById(postDto.getCategoryId()).orElseThrow(()-> new ResourceNotFoundException("Category","id",postDto.getCategoryId()));


        Post post = modelMapper.map(postDto,Post.class);
        post.setUser(user);
        post.setCategory(category);

        Post savedPost = postRepo.save(post);
        PostDto dto = modelMapper.map(savedPost,PostDto.class);
        dto.setProfilePicture(post.getUser().getUserProfile().getProfilePicture());
        return dto;
    }



    @Override
    public PostDto updatePost(PostDto  postDto , Long postId){
        Post post = postRepo.findById(postId)
                .orElseThrow(()-> new ResourceNotFoundException("Post","id",postId));

        if(postDto.getTitle() != null) {
            post.setTitle(postDto.getTitle());
        }else{
            post.setTitle(post.getTitle());
        }

        if(postDto.getContent() != null) {
            post.setContent(postDto.getContent());
        }else{
            post.setContent(post.getContent());
        }


        Category category = categoryRepo.findById(post.getCategory().getId()).orElseThrow(()-> new ResourceNotFoundException("Category","id",post.getCategory().getId()));
        post.setCategory(category);

        Post updatePost = postRepo.save(post);
        PostDto response = modelMapper.map(updatePost,PostDto.class);
        response.setCategoryId(updatePost.getCategory().getId());
        response.setCategoryName(updatePost.getCategory().getName());
        response.setProfilePicture(post.getUser().getUserProfile().getProfilePicture());
        return response;
    }

    @Override
    public void deletePost(Long postId){
        Post post = postRepo.findById(postId)
                .orElseThrow(()-> new ResourceNotFoundException("Post","id",postId));
        postRepo.delete(post);
    }

    @Override
    public PostDto getPostById(Long postId){
        Post post = postRepo.findById(postId)
                .orElseThrow(()-> new ResourceNotFoundException("Post","id",postId));
        PostDto dto =  modelMapper.map(post,PostDto.class);
        dto.setCategoryId(post.getCategory().getId());
        dto.setCategoryName(post.getCategory().getName());
        dto.setLikes(post.getLikes());
        dto.setNoOfComments(commentRepository.countByPost(post));
        dto.setNoOfLikes(likeRepository.countByPost(post));
        dto.setPostImage(post.getPostImage());
        if(post.getUser() != null && post.getUser().getUserProfile() != null){
            dto.setProfilePicture(post.getUser().getUserProfile().getProfilePicture());
        }else{
            dto.setProfilePicture("https://up.yimg.com/ib/th/id/OIP.TpqSE-tsrMBbQurUw2Su-AHaHk?pid=Api&rs=1&c=1&qlt=95&w=119&h=122");
        }
        return dto;
    }

    @Override
    public  List<PostDto> getAllPosts(){


        return postRepo.findAll()
                .stream()
                .map(post ->{
                    PostDto dto = modelMapper.map(post,PostDto.class);
                    dto.setCategoryId(post.getCategory().getId());
                    dto.setCategoryName(post.getCategory().getName());
                    dto.setLikes(post.getLikes());
                    dto.setNoOfComments(commentRepository.countByPost(post));
                    dto.setNoOfLikes(likeRepository.countByPost(post));
                    dto.setPostImage(post.getPostImage());
                    if(post.getUser() != null && post.getUser().getUserProfile() != null){
                        dto.setProfilePicture(post.getUser().getUserProfile().getProfilePicture());
                    }else{
                        dto.setProfilePicture("https://up.yimg.com/ib/th/id/OIP.TpqSE-tsrMBbQurUw2Su-AHaHk?pid=Api&rs=1&c=1&qlt=95&w=119&h=122");
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<PostDto> getPostByUser(Long userId){
        User user = userRepo.findById(userId)
                .orElseThrow(()-> new ResourceNotFoundException("User","id",userId));

        return postRepo.findByUser(user)
                .stream()
                .map(post -> {
                    PostDto dto = modelMapper.map(post,PostDto.class);
                    if(post.getCategory() != null) {
                        dto.setCategoryId(post.getCategory().getId());
                        dto.setCategoryName(post.getCategory().getName());
                        dto.setLikes(post.getLikes());
                        dto.setNoOfComments(commentRepository.countByPost(post));
                        dto.setNoOfLikes(likeRepository.countByPost(post));
                        dto.setPostImage(post.getPostImage());

                        if(post.getUser() != null && post.getUser().getUserProfile() != null){
                            dto.setProfilePicture(post.getUser().getUserProfile().getProfilePicture());
                        }else{
                            dto.setProfilePicture("https://up.yimg.com/ib/th/id/OIP.TpqSE-tsrMBbQurUw2Su-AHaHk?pid=Api&rs=1&c=1&qlt=95&w=119&h=122");
                        }
                    }else {
                        dto.setCategoryId(null);
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<PostDto> getPostByCategoryId(Long id) {

        Category category = categoryRepo.findById(id).orElseThrow(()-> new ResourceNotFoundException("Category","id",id));
        List<Post> posts = postRepo.findByCategory(category);

        return posts.stream()
                .map(post -> {
                    PostDto dto = modelMapper.map(post,PostDto.class);
                    dto.setCategoryId(post.getCategory().getId());
                    dto.setCategoryName(post.getCategory().getName());
                    dto.setLikes(post.getLikes());
                    dto.setNoOfComments(commentRepository.countByPost(post));
                    dto.setNoOfLikes(likeRepository.countByPost(post));
                    dto.setPostImage(post.getPostImage());
                    if(post.getUser() != null && post.getUser().getUserProfile() != null){
                        dto.setProfilePicture(post.getUser().getUserProfile().getProfilePicture());
                    }else{
                        dto.setProfilePicture("https://up.yimg.com/ib/th/id/OIP.TpqSE-tsrMBbQurUw2Su-AHaHk?pid=Api&rs=1&c=1&qlt=95&w=119&h=122");
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public PostDto updatePostImage(Long postId,  String imagePath) {
        Post post1 = postRepo.findById(postId).orElseThrow(()-> new ResourceNotFoundException("Post" , "id" , postId));
        post1.setPostImage(imagePath);
        post1 =  postRepo.save(post1);
        return modelMapper.map(post1  , PostDto.class);
    }


}

