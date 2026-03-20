package com.intermediate.Blog.Application.Configurations;


import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.intermediate.Blog.Application.Models.User;
import com.intermediate.Blog.Application.Repositories.UserRepo;
import com.intermediate.Blog.Application.Security.JwtTokenHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Component
public class GoogleOAuth2SuccessHandler implements AuthenticationSuccessHandler {   // class that handles the OAuth2 authentication requests and responses.

    private final String frontEndsUrl  = "http://localhost:5173";      // the frontend url .

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private JwtTokenHelper jwtTokenHelper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {    // this is the method which is getting implemented ,and we can say that it is the main method which is handling the requests and responses.
        OAuth2User oAuth2User =  (OAuth2User) authentication.getPrincipal(); // this is having the credentials of the user who have verified his/her gmail .
        // the principal contains data like ,  some token , the email , the name , the date when it is verified things like that.

        String email = oAuth2User.getAttribute("email");   //we are actually taking out the email from thr principal
        String name  = oAuth2User.getAttribute("name");  // here we are having the name

        if (email == null) throw new IllegalStateException("Google account email not available");  //checking if email is null or not .

        Optional<Object> existing = userRepo.findByEmail(email);

        // If user exists, treat this as OAuth2 login and issue JWT.
        if (existing.isPresent()) {
            User user = (User) existing.get();
            String token = jwtTokenHelper.generateToken(user.getEmail(), user.getRole());
            String redirectUrl = frontEndsUrl + "/oauth2/google"
                    + "?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
            response.sendRedirect(redirectUrl);
            return;
        }

        // Otherwise, send user to OAuth2 registration flow (frontend will ask for username).
        String redirectUrl = frontEndsUrl + "/oauth2/google"
                + "?email=" + URLEncoder.encode(email, StandardCharsets.UTF_8)
                + (name != null ? "&name=" + URLEncoder.encode(name, StandardCharsets.UTF_8) : "");

        response.sendRedirect(redirectUrl); // this is exactly how the user is getting redirected after the verification .
    }
}
