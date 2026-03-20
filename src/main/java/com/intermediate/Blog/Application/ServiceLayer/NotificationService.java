package com.intermediate.Blog.Application.ServiceLayer;
import com.intermediate.Blog.Application.DtoLayers.NotificationDto;
import com.intermediate.Blog.Application.Models.Post;
import com.intermediate.Blog.Application.Models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {
     void createLikeNotification(Post post , User actor);
     void createCommentNotification(Post post , User actor , String commentText);
     void createFollowRequestNotification(User target , User requester);
     void createFollowedNotification(User target, User follower);
     void createFollowRequestAcceptedNotification(User target, User acceptor);
     void createCommentReplyNotification(com.intermediate.Blog.Application.Models.Comment parentComment, User actor, String replyText);
     void createCommentLikeNotification(com.intermediate.Blog.Application.Models.Comment comment, User actor);

     Page<NotificationDto> getNotificationsForCurrentUser(Pageable pageable);

     long getUnreadCountForCurrentUser();

     void markAsRead(Long notificationId);
     void markAllAsReadForCurrentUser();
}


