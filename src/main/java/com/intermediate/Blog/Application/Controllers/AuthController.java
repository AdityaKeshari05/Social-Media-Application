package com.intermediate.Blog.Application.Controllers;

import com.intermediate.Blog.Application.DtoLayers.LoginRequest;
import com.intermediate.Blog.Application.DtoLayers.OAuth2RegisterRequest;
import com.intermediate.Blog.Application.DtoLayers.UserDto;
import com.intermediate.Blog.Application.Models.User;
import com.intermediate.Blog.Application.ServiceLayer.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;


    private OAuth2RegisterRequest oAuth2RegisterRequest;


    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {

        String createdUser = authService.register(user);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }


    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request.getUsername(), request.getPassword()));
    }

    @PostMapping("/verifyOtp")
    public ResponseEntity<UserDto>  verifyOtp(@RequestBody Map<String , String> req){
        String email = req.get("email");
        String otp = req.get("otp");

        UserDto result = authService.verifyOtp(email , otp);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/verifyLoginOtp")
    public ResponseEntity<Map<String ,Object>> verifyLoginOtp(@RequestBody Map<String , String> req){
        return ResponseEntity.ok(authService.loginStepTwo(req.get("email") , req.get("otp")));
    }


    @PostMapping("/oauth2-register")
    public ResponseEntity<Map<String , Object>> registerOAuth2User(@RequestBody OAuth2RegisterRequest request){
        Map<String , Object> response  = authService.registerOAuthUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
