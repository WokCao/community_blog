package com.example.community_blog.controllers;

import com.example.community_blog.models.NotificationModel;
import com.example.community_blog.models.UserModel;
import com.example.community_blog.services.NotificationService;
import com.example.community_blog.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

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

    @GetMapping("/unread")
    @ResponseBody
    public ResponseEntity<?> getUnreadNotifications() {
        UserModel currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
        }

        List<NotificationModel> notificationModelList = notificationService.getUnreadNotifications(currentUser);
        List<Map<String, Object>> notifications = notificationModelList.stream().map(notificationModel -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("postId", 1);
                    map.put("author", notificationModel.getActor() != null ? notificationModel.getActor().getFullName() : null);
                    map.put("title", notificationModel.getMessage());
                    return map;
                })
                .toList();


        return ResponseEntity.ok(notifications);
    }
}
