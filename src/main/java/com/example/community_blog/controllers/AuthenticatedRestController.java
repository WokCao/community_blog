package com.example.community_blog.controllers;

import com.example.community_blog.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/users/me")
public class AuthenticatedRestController {
    private final UserService userService;

    public AuthenticatedRestController(UserService userService) {
        this.userService = userService;
    }

    @PatchMapping("/update-avatar")
    public ResponseEntity<Map<String, Object>> updateAvatar(
            @RequestBody Map<String, String> request) {
        try {
            boolean updated = userService.updateAvatar(request.get("avatarUrl"));
            if (updated) {
                return ResponseEntity.ok(Map.of("success", true));
            } else {
                return ResponseEntity.badRequest().body(Map.of("success", false));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @PatchMapping("/update-fullname")
    public ResponseEntity<Map<String, Object>> updateName(
            @RequestBody Map<String, String> request) {
        try {
            boolean updated = userService.updateFullName(request.get("fullName"));
            if (updated) {
                return ResponseEntity.ok(Map.of("success", true));
            } else {
                return ResponseEntity.badRequest().body(Map.of("success", false));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }
}
