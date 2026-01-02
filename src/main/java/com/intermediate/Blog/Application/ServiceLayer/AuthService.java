package com.intermediate.Blog.Application.ServiceLayer;

import com.intermediate.Blog.Application.DtoLayers.OAuth2RegisterRequest;
import com.intermediate.Blog.Application.DtoLayers.PasswordChangeDto;
import com.intermediate.Blog.Application.DtoLayers.UserDto;
import com.intermediate.Blog.Application.DtoLayers.PendingUser;
import com.intermediate.Blog.Application.Exception.ResourceNotFoundException;
import com.intermediate.Blog.Application.Models.User;
import com.intermediate.Blog.Application.Models.UserProfile;
import com.intermediate.Blog.Application.Repositories.ProfileRepository;
import com.intermediate.Blog.Application.Repositories.UserRepo;
import com.intermediate.Blog.Application.Security.JwtTokenHelper;
import org.apache.coyote.BadRequestException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class AuthService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private JwtTokenHelper jwtTokenHelper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private EmailService emailService;

    @Autowired
    private OtpService otpService;

    @Autowired
    private ProfileRepository profileRepository;



    public String register(User userRequest) {


        if (userRepo.findByUsername(userRequest.getUsername()).isPresent()) {
            throw new RuntimeException("Username already taken!");
        }


        if (userRepo.findByEmail(userRequest.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists!");
        }

        String otp = generateOtp();
        PendingUser user =  new PendingUser();
        user.setUserData((userRequest));
        user.setOtp(otp);

        otpService.savePendingUser(userRequest.getEmail(), user);

        emailService.sendMail(userRequest.getEmail() , otp);






        return "Otp sent to your email ! Please verify to Complete Registration . ";
    }

    public String generateOtp(){
        return String.valueOf(100000+new Random().nextInt(900000));
    }

    public UserDto verifyOtp(String email , String otp){

        PendingUser user = otpService.getPendingUser(email);

        if(user == null ){
            throw new RuntimeException("No Registration pending for this email");
        }
        if(!user.getOtp().equals(otp)){
            throw new RuntimeException("Invalid Otp");
        }

        User finalUser = user.getUserData();
        finalUser.setPassword(passwordEncoder.encode(finalUser.getPassword()));
        finalUser.setRole("USER");
        finalUser.setVerified(true);

        User saved = userRepo.save(finalUser);
        UserProfile  p = new UserProfile();
        p.setUser(finalUser);
        profileRepository.save(p);


        emailService.sendWelcomeMail(email , saved.getUsername());

        otpService.removePendingUser(email);
        return modelMapper.map(saved  ,UserDto.class);
    }



    public Map<String , Object> login(String username, String password) {


        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        if (!auth.isAuthenticated()) {
            throw new RuntimeException("Invalid username or password!");
        }

        User user =  userRepo.findByUsername(username).orElseThrow(()-> new RuntimeException("User not Found"));

        String otp = generateOtp();

        user.setLoginOtp(otp);
        user.setLoginOtpTime(LocalDateTime.now());

        userRepo.save(user);


        emailService.sendLoginEmail(user.getEmail() , otp);

        Map<String , Object> response = new HashMap<>();
        response.put("message" , "An Otp is sent to your registered Email Id . Please Enter the Otp to Login .");
        response.put("email" , user.getEmail());


        return response;
    }

    public Map<String , Object> loginStepTwo(String email , String otp){
        User user = (User) userRepo.findByEmail(email).orElseThrow(()-> new RuntimeException("User not found"));

        if(user.getLoginOtp() == null || !user.getLoginOtp().equals(otp)){
            throw new RuntimeException("Invalid Otp");
        }

        user.setLoginOtp(null);
        userRepo.save(user);

        String token = jwtTokenHelper.generateToken(user.getUsername() , user.getRole());

        Map<String ,Object> response = new HashMap<>();
        response.put("Message" , "Login Successful");
        response.put("Token"  , token);

        return response;

    }



    public Map<String, Object> registerOAuthUser(OAuth2RegisterRequest request) {

        if(userRepo.findByEmail(request.getEmail()).isPresent()){
            throw new RuntimeException("Email already exists !!");
        }

        String username = request.getUsername();
        int suffix = 1;
        while(userRepo.findByUsername(username).isPresent()){
            username  = request.getUsername() + "_" + suffix;
            suffix++;
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setUsername(username);
        user.setRole("USER");
        user.setVerified(true);

        User saved = userRepo.save(user);

        UserProfile userProfile  = new UserProfile();
        userProfile.setUser(saved);
        profileRepository.save(userProfile);

        String token = jwtTokenHelper.generateToken(saved.getUsername()  , saved.getRole());

        Map<String , Object> response = new HashMap<>();
        response.put("message" , "Registration Successful");
        response.put("token" , token);
        response.put("username" , saved.getUsername());
        response.put("email", saved.getPassword());

        return response;
    }

    public String changePass(PasswordChangeDto request) throws BadRequestException {
        String current_email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user  = (User) userRepo.findByEmail(current_email).orElseThrow(()-> new ResourceNotFoundException("User" , "email" , current_email));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepo.save(user);

        return "Password changed successfully";
    }
}
