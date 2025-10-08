package com.example.community_blog.controllers;

import com.example.community_blog.models.PostModel;
import com.example.community_blog.models.UserModel;
import com.example.community_blog.services.PostService;
import com.example.community_blog.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthenticatedController {
    private final PostService postService;
    private final UserService userService;

    @Autowired
    public AuthenticatedController(PostService postService, UserService userService) {
        this.postService = postService;
        this.userService = userService;
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
}
