package com.intermediate.Blog.Application.Controllers;


import com.intermediate.Blog.Application.ServiceLayer.ImageUploadService;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/images")
public class ImageUploadController {

    @Autowired
    private ImageUploadService  imageUploadService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(@RequestParam("image") MultipartFile file){
        String imageUrl = imageUploadService.uploadImage(file);
        return ResponseEntity.ok(imageUrl);
    }
 }

