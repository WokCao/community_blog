package com.example.community_blog.controllers;

import com.example.community_blog.services.CommentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/comments")
public class CommentController {
    private final CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/{id}/like")
    public String likeComment(Model model, @Valid @PathVariable("id") Long id) {
        try {
            Long postId = commentService.likeComment(id);
            return "redirect:/posts/" + postId;
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        }
    }

    @PostMapping("/{id}/dislike")
    public String dislikeComment(Model model, @Valid @PathVariable("id") Long id) {
        try {
            Long postId = commentService.dislikeComment(id);
            return "redirect:/posts/" + postId;
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        }
    }
}
