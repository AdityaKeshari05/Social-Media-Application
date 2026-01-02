package com.intermediate.Blog.Application.ServiceLayer;

import com.intermediate.Blog.Application.Exception.ResourceNotFoundException;
import com.intermediate.Blog.Application.Models.PasswordReset;
import com.intermediate.Blog.Application.Models.User;
import com.intermediate.Blog.Application.Repositories.PasswordResetRepository;
import com.intermediate.Blog.Application.Repositories.UserRepo;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    @Autowired
    private UserRepo userRepo;


    private PasswordReset passwordReset;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PasswordResetRepository passwordResetRepository;

    @Autowired
    private EmailService emailService;




    public void forgotPassword(String email) throws MessagingException {
        User user  = (User) userRepo.findByEmail(email).orElseThrow(()-> new ResourceNotFoundException("User" ,"Email" ,email));

        String token = UUID.randomUUID().toString();

        PasswordReset passwordReset1 = new PasswordReset();
        passwordReset1.setToken(token);
        passwordReset1.setUser(user);
        passwordReset1.setExpiryDate(LocalDateTime.now().plusMinutes(15));

        passwordResetRepository.save(passwordReset1);

        emailService.sendResetMail(user.getEmail() , token);
    }

    public void resetPassword(String token , String newPassword){
        PasswordReset reset = passwordResetRepository.findByToken(token).orElseThrow(()->new ResourceNotFoundException("PasswordReset" , "Token" , token));

        if(reset.getExpiryDate().isBefore(LocalDateTime.now())){
            throw new RuntimeException("Token Expired");
        }

        User user = reset.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));

        passwordResetRepository.delete(reset);
    }


}
