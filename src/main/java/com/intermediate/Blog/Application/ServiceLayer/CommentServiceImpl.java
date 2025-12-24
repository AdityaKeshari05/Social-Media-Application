package com.intermediate.Blog.Application.ServiceLayer;


import com.intermediate.Blog.Application.DtoLayers.CommentDto;
import com.intermediate.Blog.Application.Exception.ResourceNotFoundException;
import com.intermediate.Blog.Application.Models.Comment;
import com.intermediate.Blog.Application.Models.Post;
import com.intermediate.Blog.Application.Models.User;
import com.intermediate.Blog.Application.Repositories.CommentRepository;
import com.intermediate.Blog.Application.Repositories.PostRepository;
import com.intermediate.Blog.Application.Repositories.UserRepo;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentRepository commentRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PostRepository postRepo;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public CommentDto createComment(Long userId , Long postId , CommentDto commentDto){
        User user = userRepo.findById(userId)
                .orElseThrow(()-> new ResourceNotFoundException("User","id",userId));

        Post post = postRepo.findById(postId)
                .orElseThrow(()-> new ResourceNotFoundException("Post","id",postId));

        Comment comment = modelMapper.map(commentDto,Comment.class);
        comment.setPost(post);
        comment.setUser(user);

        Comment savedComment = commentRepo.save(comment);
        return modelMapper.map(savedComment, CommentDto.class);

    }

    @Override
    public void deleteComment(Long commentId){
        Comment comment = commentRepo.findById(commentId)
                .orElseThrow(()-> new ResourceNotFoundException("Comment","id",commentId));
        commentRepo.delete(comment);

    }

    @Override
    public List<CommentDto> getCommentsByPost(Long postId){
        Post post = postRepo.findById(postId)
                .orElseThrow(()-> new ResourceNotFoundException("Post","id",postId));

        return commentRepo.findByPost(post)
                .stream()
                .map(comment -> modelMapper.map(comment , CommentDto.class))
                .collect(Collectors.toList());
    }

}
