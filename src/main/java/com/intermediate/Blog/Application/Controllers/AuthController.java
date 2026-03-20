package com.intermediate.Blog.Application.Controllers;

import com.intermediate.Blog.Application.DtoLayers.LoginRequest;
import com.intermediate.Blog.Application.DtoLayers.MessageResponse;
import com.intermediate.Blog.Application.DtoLayers.OAuth2RegisterRequest;
import com.intermediate.Blog.Application.DtoLayers.PasswordChangeDto;
import com.intermediate.Blog.Application.DtoLayers.UserDto;
import com.intermediate.Blog.Application.Models.User;
import com.intermediate.Blog.Application.ServiceLayer.AuthService;
import com.intermediate.Blog.Application.ServiceLayer.PasswordResetService;
import jakarta.mail.MessagingException;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private PasswordResetService passwordResetService;



    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@RequestBody User user) {
        String createdUser = authService.register(user);
        return new ResponseEntity<>(new MessageResponse(createdUser), HttpStatus.CREATED);
    }


    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
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
    public ResponseEntity<Map<String, Object>> registerOAuth2User(@Valid @RequestBody OAuth2RegisterRequest request) {
        Map<String , Object> response  = authService.registerOAuthUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/change-password")
    public ResponseEntity<MessageResponse> changePass(@Valid @RequestBody PasswordChangeDto request) throws BadRequestException {
        return ResponseEntity.ok(new MessageResponse(authService.changePass(request)));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@RequestBody Map<String, String> body) throws MessagingException {
        passwordResetService.forgotPassword(body.get("email"));
        return ResponseEntity.ok(new MessageResponse("Email sent successfully"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@RequestBody Map<String, String> body) {
        passwordResetService.resetPassword(body.get("token"), body.get("newPassword"));
        return ResponseEntity.ok(new MessageResponse("Password reset successfully"));
    }
}
