package com.intermediate.Blog.Application.Controllers;

import com.intermediate.Blog.Application.DtoLayers.PostDto;
import com.intermediate.Blog.Application.Exception.ResourceNotFoundException;
import com.intermediate.Blog.Application.Models.Like;
import com.intermediate.Blog.Application.Models.Post;
import com.intermediate.Blog.Application.Models.User;
import com.intermediate.Blog.Application.Repositories.*;
import com.intermediate.Blog.Application.ServiceLayer.ImageUploadService;
import com.intermediate.Blog.Application.ServiceLayer.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ImageUploadService imageUploadService;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private CommentRepository commentRepository;

    @PostMapping()
    public ResponseEntity<PostDto> createPost(@RequestBody PostDto postDto ){
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = (User) userRepo.findByEmail(currentUser).orElseThrow(()-> new ResourceNotFoundException("User" ,"email", currentUser));
        if(!currentUser.equals(user.getEmail())){
            throw new RuntimeException("Access Denied");
        }
        PostDto createdPost = postService.createPost(postDto, user.getId());
        return new ResponseEntity<>(createdPost, HttpStatus.CREATED);
    }

    @PutMapping("/{postId}")
    public ResponseEntity<PostDto> updatePost(@RequestBody PostDto postDto , @PathVariable Long postId){
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        Post post = postRepository.findById(postId).orElseThrow(()-> new ResourceNotFoundException("Post", "id", postId));

        if(!currentUser.equals(post.getUser().getEmail())){
            throw new RuntimeException("Access Denied");
        }

        PostDto updatedPost = postService.updatePost(postDto,postId);
        return ResponseEntity.ok(updatedPost);
    }

    @DeleteMapping("/{postId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> deletePost(@PathVariable Long postId){
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = (User) userRepo.findByEmail(currentUser).orElseThrow(()-> new ResourceNotFoundException("User" ,"email", currentUser));


        Post post = postRepository.findById(postId).orElseThrow(()-> new ResourceNotFoundException("Post", "id", postId));
        if(!currentUser.equals(post.getUser().getEmail()) &&  !user.getRole().equals("ADMIN")){
            throw new RuntimeException("Access Denied");
        }

        postService.deletePost(postId);
        return new ResponseEntity<>("Post Deleted Successfully",HttpStatus.OK);
    }

    @GetMapping("/")
    public ResponseEntity<List<PostDto>> getAllPosts(){
        return ResponseEntity.ok(postService.getAllPosts());
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostDto> getPostById(@PathVariable Long postId){
        return ResponseEntity.ok(postService.getPostById(postId));
    }
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PostDto>> getPostsByUser(@PathVariable Long userId){
        return ResponseEntity.ok(postService.getPostByUser(userId));
    }

    @GetMapping("/category/{id}")
    public ResponseEntity<List<PostDto>> getPostByCategoryId(@PathVariable Long id){
        return ResponseEntity.ok(postService.getPostByCategoryId(id));
    }

    @PostMapping("/postImage/{postId}")
    public ResponseEntity<PostDto> updatePostImage(@RequestParam MultipartFile file , @PathVariable Long postId){
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = (User) userRepo.findByEmail(currentUser).orElseThrow(()-> new ResourceNotFoundException("User" ,"email", currentUser));
        Post post = postRepository.findById(postId).orElseThrow(()-> new ResourceNotFoundException("Post", "id", postId));

        if(!currentUser.equals(post.getUser().getEmail()) &&  !user.getRole().equals("ADMIN")){
            throw new RuntimeException("Access Denied");
        }

        String imagePath = imageUploadService.uploadImage(file);

        PostDto post1 = postService.updatePostImage(postId,imagePath);
        List<Like> likes = post.getLikes();
        post1.setNoOfLikes(likeRepository.countByPost(post));
        post1.setCategoryId(post.getCategory().getId());
        post1.setCategoryName(post.getCategory().getName());
        post1.setProfilePicture(post.getUser().getUserProfile().getProfilePicture());
        post1.setNoOfComments(commentRepository.countByPost(post));


        return ResponseEntity.ok(post1);
    }






}
