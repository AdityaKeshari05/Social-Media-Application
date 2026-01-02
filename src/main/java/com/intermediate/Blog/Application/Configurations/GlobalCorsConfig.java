package com.intermediate.Blog.Application.Configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class GlobalCorsConfig {

    @Bean
    public WebMvcConfigurer webMvcConfigurer() {      // The Global CORS Config for testing it with the frontend for the local machine .
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry corsRegistry) {
                corsRegistry.addMapping("/**")     // "/**" says that the all the paths are accessed for now
                        .allowedOrigins("*" , "http://127.0.0.1:5500") //  "*" says that right now any frontend can access this backend when the system is running
                        .allowedMethods("GET", "POST", "PUT", "DELETE") // only get,post , put and delete methods are allowed.
                        .allowedHeaders("*") // all headers are allowed
                        .allowCredentials(false); // any credentials are not allowed
            }
        };
    }
}
