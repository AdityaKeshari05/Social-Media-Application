package com.intermediate.Blog.Application.ServiceLayer;


import com.intermediate.Blog.Application.DtoLayers.NotificationDto;
import com.intermediate.Blog.Application.Exception.ResourceNotFoundException;
import com.intermediate.Blog.Application.Models.Notification;
import com.intermediate.Blog.Application.Models.NotificationType;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import com.intermediate.Blog.Application.Models.Post;
import com.intermediate.Blog.Application.Models.User;
import com.intermediate.Blog.Application.Repositories.NotificationRepository;
import com.intermediate.Blog.Application.Repositories.PostRepository;
import com.intermediate.Blog.Application.Repositories.UserRepo;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService{

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ModelMapper modelMapper;


    private static final int MAX_NOTIFICATIONS_PER_USER = 50;


    @Override
    public void createLikeNotification(Post post, User actor) {
        User recipient = post.getUser();
        if (recipient == null || recipient.getId() == actor.getId()) {
            return;
        }
        Notification n = new Notification();
        n.setRecipient(recipient);
        n.setActor(actor);
        n.setPostId(post.getId());
        n.setType(NotificationType.LIKE);
        n.setCommentText(null);

        notificationRepository.save(n);
        trimOldNotifications(recipient);

    }

    @Override
    public void createCommentNotification(Post post, User actor, String commentText) {
        User recipient = post.getUser();
        if (recipient == null || recipient.getId() == actor.getId()) {
            return;
        }

        Notification n = new Notification();
        n.setRecipient(recipient);
        n.setActor(actor);
        n.setType(NotificationType.COMMENT);
        n.setPostId(post.getId());
        n.setCommentText(commentText);

        notificationRepository.save(n);
        trimOldNotifications(recipient);
    }

    @Override
    public void createFollowRequestNotification(User target, User requester) {
        if(target.getId() == requester.getId()){
            return ;
        }
        Notification n = new Notification();
        n.setRecipient(target);      // the one who will accept/reject
        n.setActor(requester);       // who requested
        n.setType(NotificationType.FOLLOW_REQUEST);
        n.setPostId(null);
        n.setCommentText(null);

        notificationRepository.save(n);
        trimOldNotifications(target);
    }

    @Override
    public void createFollowedNotification(User target, User follower) {
        if(target.getId() == follower.getId()){
            return ;
        }

        Notification n = new Notification();
        n.setRecipient(target);  // the one who got a new follower
        n.setActor(follower);    // the follower
        n.setType(NotificationType.FOLLOWED);
        n.setPostId(null);
        n.setCommentText(null);

        notificationRepository.save(n);
        trimOldNotifications(target);
    }

    @Override
    public void createFollowRequestAcceptedNotification(User target, User acceptor) {
        if(target.getId() == acceptor.getId()){
            return ;
        }

        Notification n = new Notification();
        n.setRecipient(target);   // the original requester who sent the follow request
        n.setActor(acceptor);     // the private user who accepted it
        n.setType(NotificationType.FOLLOW_REQUEST_ACCEPTED);
        n.setPostId(null);
        n.setCommentText(null);

        notificationRepository.save(n);
        trimOldNotifications(target);
    }

    @Override
    public void createCommentReplyNotification(com.intermediate.Blog.Application.Models.Comment parentComment, User actor, String replyText) {
        User recipient = parentComment.getUser();
        if (recipient == null || recipient.getId() == actor.getId()) {
            return;
        }

        Notification n = new Notification();
        n.setRecipient(recipient);
        n.setActor(actor);
        n.setType(NotificationType.COMMENT_REPLY);
        n.setPostId(parentComment.getPost() != null ? parentComment.getPost().getId() : null);
        n.setCommentText(replyText);

        notificationRepository.save(n);
        trimOldNotifications(recipient);
    }

    @Override
    public void createCommentLikeNotification(com.intermediate.Blog.Application.Models.Comment comment, User actor) {
        User recipient = comment.getUser();
        if (recipient == null || recipient.getId() == actor.getId()) {
            return;
        }

        Notification n = new Notification();
        n.setRecipient(recipient);
        n.setActor(actor);
        n.setType(NotificationType.COMMENT_LIKE);
        n.setPostId(comment.getPost() != null ? comment.getPost().getId() : null);
        n.setCommentText(comment.getText());

        notificationRepository.save(n);
        trimOldNotifications(recipient);
    }

    @Override
    public Page<NotificationDto> getNotificationsForCurrentUser(Pageable pageable) {
        User currentUser  = currentUserService.getCurrentUser();
        Page<Notification> page = notificationRepository.findByRecipientOrderByCreatedAtDesc(currentUser , pageable);
        return page.map(this::toDto);
    }

    @Override
    public long getUnreadCountForCurrentUser() {
        User currentUser  = currentUserService.getCurrentUser();
        return notificationRepository.countByRecipientAndReadAtIsNull(currentUser);
    }

    @Override
    public void markAsRead(Long notificationId) {
        User current = currentUserService.getCurrentUser();
        Notification n = notificationRepository
                .findByIdAndRecipient(notificationId, current)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));

        if (n.getReadAt() == null) {
            n.setReadAt(LocalDateTime.now());
            notificationRepository.save(n);
        }
    }

    @Override
    public void markAllAsReadForCurrentUser() {
        User current = currentUserService.getCurrentUser();
        List<Notification> unread = notificationRepository.findByRecipientAndReadAtIsNull(current);
        if (unread.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        for (Notification n : unread) {
            n.setReadAt(now);
        }
        notificationRepository.saveAll(unread);
    }

    private NotificationDto toDto(Notification n){
        NotificationDto dto = new NotificationDto();
        dto.setId(n.getId());
        dto.setType(n.getType());
        dto.setCreatedAt(n.getCreatedAt());
        dto.setRead(n.getReadAt() != null);

        if(n.getActor() != null){
            dto.setActorId(n.getActor().getId());
            dto.setActorUsername(n.getActor().getUsername());
            if(n.getActor().getUserProfile() != null){
                dto.setActorProfilePicture(n.getActor().getUserProfile().getProfilePicture());
            }
        }

        dto.setPostId(n.getPostId());
        dto.setCommentText(n.getCommentText());

        if(n.getPostId() != null){
            postRepository.findById(n.getPostId()).ifPresent(post -> dto.setPostTitle(post.getTitle()));
        }

        dto.setCanFollowBack(false);
        return dto;
    }

    private void trimOldNotifications(User recipient){
        List<Notification> all  = notificationRepository.findByRecipientOrderByCreatedAtDesc(recipient);
        if(all.size() > MAX_NOTIFICATIONS_PER_USER){
            List<Notification> toDelete = all.subList(MAX_NOTIFICATIONS_PER_USER , all.size());
            notificationRepository.deleteAll(toDelete);
        }
    }

}

