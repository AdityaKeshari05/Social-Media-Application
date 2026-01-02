package com.intermediate.Blog.Application.Configurations;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary(){                 // Integrating the cloudinary Api to store images and videos.
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dodmyrnjl",  // cloud name
                "api_key","994642681862525",        // the cloudinary api key
                "api_secret","0WQWmuM1_hVG6E-SXhmqzipaYw0"  // the cloudinary api secret

        ));


    }
}
