package com.intermediate.Blog.Application.Configurations;


import com.intermediate.Blog.Application.Security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {    // one of the main class , which handles the security configurations which handles like what api endpoints are public and private , handles the authentication part , applies the filters before getting logged in ,manages the session and etc

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private GoogleOAuth2SuccessHandler googleOAuth2SuccessHandler;

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean

    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {  // a class of security filter chain which is implemented and this only handles everything.
        return
                http.
                        csrf(csrf -> csrf.disable())   //  disabling the csrf which stands for cross-sharing-resource-forgery , it's basically kind of a security attack, and to prevent that attack we disable it, it's very important .
                        .authorizeHttpRequests(auth ->
                                auth.requestMatchers("/api/auth/**","/api/posts/**",
                                                "/v3/api-docs/**",
                                                "/swagger-ui/**",
                                                "/swagger-ui.html").permitAll()    // the end points above can be accessed by  any unauthorized user , because we have used permitAll() .
                                        .anyRequest().authenticated()) // rather than those mentioned endpoints in the above , all the other endpoints are authenticated ,  which means only authorized users can access those endpoints.
                        .oauth2Login(auth2-> auth2.successHandler(googleOAuth2SuccessHandler)) // without this line everything thing related to auth2 is waste this is the main line of code which is initiating the auth2 handler.
                        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // this line is making the session stateless ,  which means the server will not remember the user for each request, the user must carry the required authentication infos for each request
                                                                                                                        // we do so , so that the server will not store the user's state , enabling scalable rest-complaint, and also we are using token-based authentication , where each request is independently authenticated.
                        .addFilterBefore(jwtAuthenticationFilter , UsernamePasswordAuthenticationFilter.class) // before the username-password authentication filter we  are having jwt-authentication filter. at first jwt filter will validate the token generated from the login .
                        .build();
    }



}
