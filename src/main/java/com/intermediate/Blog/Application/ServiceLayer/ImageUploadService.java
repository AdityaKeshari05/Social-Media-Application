package com.intermediate.Blog.Application.ServiceLayer;


import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.intermediate.Blog.Application.Configurations.CloudinaryConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
public class ImageUploadService {

    @Autowired
    private Cloudinary cloudinary;

    public String uploadImage(MultipartFile file){
        try{

            Map uploadResults = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap("resource_type","auto"));
            return uploadResults.get("secure_url").toString();
        }catch (Exception e){
             throw new RuntimeException("Image Upload failed"+e.getMessage());
        }
    }

}
