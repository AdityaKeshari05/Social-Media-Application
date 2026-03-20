package com.intermediate.Blog.Application.Controllers;

import com.intermediate.Blog.Application.DtoLayers.MessageResponse;
import com.intermediate.Blog.Application.DtoLayers.PostDto;
import com.intermediate.Blog.Application.Exception.AccessControlException;
import com.intermediate.Blog.Application.Exception.ResourceNotFoundException;
import com.intermediate.Blog.Application.Models.Notification;
import com.intermediate.Blog.Application.Models.Post;
import com.intermediate.Blog.Application.Models.User;
import com.intermediate.Blog.Application.Repositories.*;
import com.intermediate.Blog.Application.ServiceLayer.AccountService;
import com.intermediate.Blog.Application.ServiceLayer.CurrentUserService;
import com.intermediate.Blog.Application.ServiceLayer.ImageUploadService;
import com.intermediate.Blog.Application.ServiceLayer.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ImageUploadService imageUploadService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private CurrentUserService currentUserService;

    @PostMapping()
    public ResponseEntity<PostDto> createPost(@Valid @RequestBody PostDto postDto) {
        PostDto createdPost = postService.createPost(postDto, currentUserService.getCurrentUser().getId());
        return new ResponseEntity<>(createdPost, HttpStatus.CREATED);
    }

    @PutMapping("/{postId}")
    public ResponseEntity<PostDto> updatePost(@Valid @RequestBody PostDto postDto, @PathVariable Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

        if (currentUserService.getCurrentUser().getId() != post.getUser().getId()) {
            throw new AccessControlException("Access denied");
        }

        PostDto updatedPost = postService.updatePost(postDto, postId);
        return ResponseEntity.ok(updatedPost);
    }

    @DeleteMapping("/{postId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> deletePost(@PathVariable Long postId) {
        User currentUser = currentUserService.getCurrentUser();
        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
        if (currentUser.getId() != post.getUser().getId() && !currentUser.getRole().equals("ADMIN")) {
            throw new AccessControlException("Access denied");
        }
        postService.deletePost(postId);
        return ResponseEntity.ok(new MessageResponse("Post deleted successfully"));
    }

    @GetMapping("/")
    public ResponseEntity<Page<PostDto>> getFeed(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "15") int size,
            @RequestParam(required = false, defaultValue = "default") String seed) {
        Page<PostDto> result = accountService.getAllViewablePostsPaginated(
                currentUserService.getCurrentUser(),
                page,
                size,
                seed
        );
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostDto> getPostById(@PathVariable Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
        if (!accountService.canViewPosts(currentUserService.getCurrentUser(), post.getUser())) {
            throw new AccessControlException("You can't view the posts of this account");
        }
        return ResponseEntity.ok(postService.getPostById(postId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PostDto>> getPostsByUser(@PathVariable Long userId) {
        User owner = userRepo.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        if (!accountService.canViewPosts(currentUserService.getCurrentUser(), owner)) {
            throw new AccessControlException("You can't view the posts of this account");
        }
        return ResponseEntity.ok(postService.getPostByUser(userId));
    }

    @GetMapping("/category/{id}")
    public ResponseEntity<List<PostDto>> getPostByCategoryId(@PathVariable("id") Long categoryId) {
        return ResponseEntity.ok(postService.getPostByCategoryId(categoryId));
    }

    @PostMapping("/postImage/{postId}")
    public ResponseEntity<PostDto> updatePostImage(@RequestParam MultipartFile file, @PathVariable Long postId) {
        Notification notification = new Notification();
        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
        User currentUser = currentUserService.getCurrentUser();
        if (!currentUser.getEmail().equals(post.getUser().getEmail()) && !currentUser.getRole().equals("ADMIN")) {
            throw new AccessControlException("Access denied");
        }
        String imagePath = imageUploadService.uploadImage(file);
        PostDto updatedPost = postService.updatePostImage(postId, imagePath);
        return ResponseEntity.ok(updatedPost);


    }
}
