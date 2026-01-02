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
public class GoogleOAuth2SuccessHandler implements AuthenticationSuccessHandler {   // class that handles the OAuth2 authentication requests and responses.

    private final String frontEndsUrl  = "http://localhost:5173";      // the frontend url .

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {    // this is the method which is getting implemented ,and we can say that it is the main method which is handling the requests and responses.
        OAuth2User oAuth2User =  (OAuth2User) authentication.getPrincipal(); // this is having the credentials of the user who have verified his/her gmail .
        // the principal contains data like ,  some token , the email , the name , the date when it is verified things like that.

        String email = oAuth2User.getAttribute("email");   //we are actually taking out the email from thr principal
        String name  = oAuth2User.getAttribute("name");  // here we are having the name

        if (email == null) throw new AssertionError();  //checking if email is null or not .

        String redirectUrl = frontEndsUrl + "?email"+ URLEncoder.encode(email , StandardCharsets.UTF_8); // after getting verified the user is being redirected to the signup  page with the url contains the email from which the frontend can access the email.

        response.sendRedirect(redirectUrl); // this is exactly how the user is getting redirected after the verification .
    }
}
