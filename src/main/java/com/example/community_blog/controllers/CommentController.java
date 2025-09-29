package com.example.community_blog.controllers;

import com.example.community_blog.models.CommentModel;
import com.example.community_blog.models.UserModel;
import com.example.community_blog.services.CommentService;
import com.example.community_blog.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
@RequestMapping("/comments")
public class CommentController {
    private final CommentService commentService;
    private final UserService userService;

    @Autowired
    public CommentController(CommentService commentService, UserService userService) {
        this.commentService = commentService;
        this.userService = userService;
    }

    @ResponseBody
    @PostMapping("/{id}/like")
    public ResponseEntity<?> likeComment(Model model, @Valid @PathVariable("id") Long id) {
        try {
            CommentModel commentModel = commentService.likeComment(id);

            UserModel currentUser = userService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
            }

            return ResponseEntity.ok(Map.of(
                    "likeCount", commentModel.getLikeCount(),
                    "dislikeCount", commentModel.getDislikeCount(),
                    "isCommentLikedByUser", commentModel.getLikedBy().contains(currentUser),
                    "isCommentDislikedByUser", commentModel.getDislikedBy().contains(currentUser)
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @ResponseBody
    @PostMapping("/{id}/dislike")
    public ResponseEntity<?> dislikeComment(Model model, @Valid @PathVariable("id") Long id) {
        try {
            CommentModel commentModel = commentService.dislikeComment(id);

            UserModel currentUser = userService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
            }

            return ResponseEntity.ok(Map.of(
                    "likeCount", commentModel.getLikeCount(),
                    "dislikeCount", commentModel.getDislikeCount(),
                    "isCommentLikedByUser", commentModel.getLikedBy().contains(currentUser),
                    "isCommentDislikedByUser", commentModel.getDislikedBy().contains(currentUser)
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
