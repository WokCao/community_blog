package com.example.community_blog.controllers;

import com.example.community_blog.models.PostModel;
import com.example.community_blog.models.UserModel;
import com.example.community_blog.services.PostService;
import com.example.community_blog.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final PostService postService;

    @Autowired
    public UserController(UserService userService, PostService postService) {
        this.userService = userService;
        this.postService = postService;
    }

    @GetMapping("/me")
    public String viewProfile(Model model) {
        UserModel currentUser = userService.getCurrentUser();
        if (currentUser != null) {
            model.addAttribute("user", currentUser);
        } else {
            return "redirect:/auth/login";
        }

        try {
            Page<PostModel> notablePosts = postService.getUserNotablePosts(currentUser.getId());
            model.addAttribute("notablePosts", notablePosts.getContent());
            model.addAttribute("totalNotablePosts", notablePosts.getTotalElements());
            model.addAttribute("totalViews", postService.calculatePostsView());
            model.addAttribute("totalLikes", postService.calculatePostsLike());
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        }
        return "profile";
    }

    @ResponseBody
    @PatchMapping("/me/update-avatar")
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

    @ResponseBody
    @PatchMapping("/me/update-fullname")
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

    @GetMapping("/{userId}/notable-posts")
    public String getNotablePosts(@PathVariable("userId") Long userId, Model model,
                                  @RequestParam(value = "page", defaultValue = "0") int page,
                                  @RequestParam(value = "search", defaultValue = "") String search,
                                  @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
                                  @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir
    ) {
        try {
            UserModel currentUser = userService.getCurrentUser();
            if (currentUser != null) {
                model.addAttribute("user", currentUser);
            }

            Page<PostModel> postModelPage = postService.getPostsOfUser(page, search, sortBy, sortDir, userId);
            model.addAttribute("posts", postModelPage.getContent());
            model.addAttribute("totalPosts", postModelPage.getTotalElements());
            model.addAttribute("search", search);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("sortDir", sortDir);
            model.addAttribute("totalPages", postModelPage.getTotalPages());
            model.addAttribute("currentPage", postModelPage.getNumber());
            model.addAttribute("postUserId", userId);
            return "user-notable-posts";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        }
    }
}
