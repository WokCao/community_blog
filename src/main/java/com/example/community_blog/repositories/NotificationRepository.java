package com.example.community_blog.repositories;

import com.example.community_blog.models.NotificationModel;
import com.example.community_blog.models.UserModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationModel, Long> {
    Page<NotificationModel> findByRecipient(UserModel recipient, Pageable pageable);

    Page<NotificationModel> findByRecipientAndIsRead(UserModel recipient, boolean isRead, Pageable pageable);

    // Only unread notifications
    List<NotificationModel> findByRecipientAndIsReadFalseOrderByCreatedAtDesc(UserModel user);
}
