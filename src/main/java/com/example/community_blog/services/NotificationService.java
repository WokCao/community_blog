package com.example.community_blog.services;

import com.example.community_blog.models.NotificationModel;
import com.example.community_blog.models.PostModel;
import com.example.community_blog.models.UserModel;
import com.example.community_blog.repositories.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Autowired
    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public NotificationModel createNotification(UserModel recipient, UserModel actor, String message, PostModel post) {
        NotificationModel notif = new NotificationModel();
        notif.setRecipient(recipient);
        notif.setActor(actor);
        notif.setMessage(message);
        notif.setRead(false);
        notif.setPost(post);
        return notificationRepository.save(notif);
    }

    public List<NotificationModel> getUnreadNotifications(UserModel recipient) {
        return notificationRepository.findByRecipientAndIsReadFalseOrderByCreatedAtDesc(recipient);
    }

    public List<NotificationModel> getAllNotifications(UserModel recipient) {
        return notificationRepository.findByRecipientOrderByCreatedAtDesc(recipient);
    }

    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }
}
