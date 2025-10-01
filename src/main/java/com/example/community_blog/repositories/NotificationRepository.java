package com.example.community_blog.repositories;

import com.example.community_blog.models.NotificationModel;
import com.example.community_blog.models.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationModel, Long> {
    // All notifications for a user
    List<NotificationModel> findByRecipientOrderByCreatedAtDesc(UserModel user);

    // Only unread notifications
    List<NotificationModel> findByRecipientAndIsReadFalseOrderByCreatedAtDesc(UserModel user);
}
