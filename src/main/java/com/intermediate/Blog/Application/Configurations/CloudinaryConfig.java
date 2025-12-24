package com.intermediate.Blog.Application.Configurations;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary(){
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dodmyrnjl",
                "api_key","994642681862525",
                "api_secret","0WQWmuM1_hVG6E-SXhmqzipaYw0"

        ));


    }
}
