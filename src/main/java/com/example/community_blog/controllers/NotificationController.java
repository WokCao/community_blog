package com.example.community_blog.controllers;

import com.example.community_blog.models.NotificationModel;
import com.example.community_blog.models.UserModel;
import com.example.community_blog.services.NotificationService;
import com.example.community_blog.services.UserService;
import com.example.community_blog.utils.TimeAgoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/notifications")
public class NotificationController {
    private final NotificationService notificationService;
    private final UserService userService;

    @Autowired
    public NotificationController(NotificationService notificationService, UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }

    @GetMapping
    @ResponseBody
    public ResponseEntity<?> getNotifications() {
        UserModel currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
        }

        List<NotificationModel> notificationModelList = notificationService.getAllNotifications(currentUser);

        List<Map<String, Object>> notifications = notificationModelList.stream()
                .map(notificationModel -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("postId", notificationModel.getPost() != null ? notificationModel.getPost().getId() : null);
                    map.put("notificationId", notificationModel.getId());
                    map.put("author", notificationModel.getActor() != null ? notificationModel.getActor().getFullName() : null);
                    map.put("title", notificationModel.getMessage());
                    map.put("time", TimeAgoUtil.getTimeAgo(notificationModel.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant()));
                    map.put("avatarUrl", notificationModel.getActor() != null ? notificationModel.getActor().getAvatarUrl() : null);
                    map.put("read", notificationModel.isRead());
                    return map;
                })
                .toList();

        return ResponseEntity.ok(notifications);
    }

    @PatchMapping("/{notificationId}/markAsRead")
    @ResponseBody
    public ResponseEntity<?> markAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok().build();
    }
}
