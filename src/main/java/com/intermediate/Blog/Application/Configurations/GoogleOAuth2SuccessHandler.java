package com.intermediate.Blog.Application.Configurations;


import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class GoogleOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final String frontEndsUrl  = "http://localhost:5173";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User =  (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String name  = oAuth2User.getAttribute("name");

        if (email == null) throw new AssertionError();

        String redirectUrl = frontEndsUrl + "?email"+ URLEncoder.encode(email , StandardCharsets.UTF_8);

        response.sendRedirect(redirectUrl);
    }
}
