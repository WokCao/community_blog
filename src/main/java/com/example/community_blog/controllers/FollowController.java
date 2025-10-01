package com.example.community_blog.controllers;

import com.example.community_blog.models.UserModel;
import com.example.community_blog.services.FollowService;
import com.example.community_blog.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequestMapping("/follows")
public class FollowController {
    private final FollowService followService;
    private final UserService userService;

    @Autowired
    public FollowController(FollowService followService, UserService userService) {
        this.followService = followService;
        this.userService = userService;
    }

    @PostMapping("/{userId}")
    public ResponseEntity<?> followUser(@PathVariable Long userId) {
        UserModel currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
        }

        if (currentUser.getId().equals(userId)) {
            return ResponseEntity.badRequest().body("You cannot follow yourself");
        }

        followService.followUser(currentUser, userId);
        return ResponseEntity.ok().body("Followed successfully");
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> unfollowUser(@PathVariable Long userId) {
        UserModel currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
        }

        if (currentUser.getId().equals(userId)) {
            return ResponseEntity.badRequest().body("You cannot unfollow yourself");
        }

        followService.unfollowUser(currentUser, userId);
        return ResponseEntity.ok().body("Unfollowed successfully");
    }
}
