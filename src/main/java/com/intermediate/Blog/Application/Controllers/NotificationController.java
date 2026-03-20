package com.intermediate.Blog.Application.Controllers;


import com.intermediate.Blog.Application.DtoLayers.MessageResponse;
import com.intermediate.Blog.Application.DtoLayers.NotificationDto;
import com.intermediate.Blog.Application.ServiceLayer.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;


    @GetMapping
    public ResponseEntity<Page<NotificationDto>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ){
        Pageable pageable = PageRequest.of(page , size);
        Page<NotificationDto> result = notificationService.getNotificationsForCurrentUser(pageable);
        return ResponseEntity.ok(result);
    }


    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCounts(){
        long count = notificationService.getUnreadCountForCurrentUser();
        return ResponseEntity.ok(count);
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<MessageResponse> markAsRead(@PathVariable Long id){
        notificationService.markAsRead(id);
        return ResponseEntity.ok(new MessageResponse("Notification marked as read"));
    }

    @PostMapping("/read-all")
    public ResponseEntity<MessageResponse> markAllAsRead() {
        notificationService.markAllAsReadForCurrentUser();
        return ResponseEntity.ok(new MessageResponse("All notifications marked as read"));
    }
}
