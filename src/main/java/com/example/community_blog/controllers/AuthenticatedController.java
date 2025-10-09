package com.example.community_blog.controllers;

import com.example.community_blog.models.PostModel;
import com.example.community_blog.models.UserModel;
import com.example.community_blog.services.PostService;
import com.example.community_blog.services.SchedulerService;
import com.example.community_blog.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthenticatedController {
    private final PostService postService;
    private final UserService userService;
    private final SchedulerService schedulerService;

    @Autowired
    public AuthenticatedController(PostService postService, UserService userService, SchedulerService schedulerService) {
        this.postService = postService;
        this.userService = userService;
        this.schedulerService = schedulerService;
    }

    @GetMapping("/home")
    public String home(Model model) {
        UserModel currentUser = userService.getCurrentUser();
        if (currentUser != null) {
            model.addAttribute("user", currentUser);
        } else {
            return "redirect:/auth/login";
        }

        Page<PostModel> postModelPage = postService.getLatestPosts();
        model.addAttribute("posts", postModelPage.getContent());
        model.addAttribute("totalElements", postModelPage.getTotalElements());
        return "homepage";
    }

    @GetMapping("/write-post")
    public String writePost(Model model) {
        UserModel currentUser = userService.getCurrentUser();
        if (currentUser != null) {
            model.addAttribute("user", currentUser);
        } else {
            return "redirect:/auth/login";
        }

        return "write-post";
    }

    @GetMapping("/bot/fetch")
    public ResponseEntity<String> triggerBot() {
        schedulerService.fetchArticles();
        return ResponseEntity.ok("Bot triggered for programming news");
    }
}
