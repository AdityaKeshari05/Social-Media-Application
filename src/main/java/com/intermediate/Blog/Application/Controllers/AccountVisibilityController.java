package com.intermediate.Blog.Application.Controllers;


import com.intermediate.Blog.Application.DtoLayers.MessageResponse;
import com.intermediate.Blog.Application.Models.User;
import com.intermediate.Blog.Application.ServiceLayer.CurrentUserService;
import com.intermediate.Blog.Application.ServiceLayer.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/account")
public class AccountVisibilityController {

    @Autowired
    private UserService userService;

    @Autowired
    private CurrentUserService currentUserService;

    @PutMapping("/private")
    public ResponseEntity<MessageResponse> makePrivate() {
        User user = currentUserService.getCurrentUser();
        userService.makeAccountPrivate(user);
        return ResponseEntity.ok(new MessageResponse("Account is private now"));
    }

    @PutMapping("/public")
    public ResponseEntity<MessageResponse> makePublic() {
        User user = currentUserService.getCurrentUser();
        userService.makeAccountPublic(user);
        return ResponseEntity.ok(new MessageResponse("Account is public now"));
    }
}
