package com.example.community_blog.controllers;

import com.example.community_blog.dto.CommentView;
import com.example.community_blog.dto.CreatePostRequest;
import com.example.community_blog.models.PostModel;
import com.example.community_blog.models.UserModel;
import com.example.community_blog.services.CommentService;
import com.example.community_blog.services.PostService;
import com.example.community_blog.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class AuthenticatedController {
    private final PostService postService;
    private final UserService userService;
    private final CommentService commentService;

    @Autowired
    public AuthenticatedController(PostService postService, UserService userService, CommentService commentService) {
        this.postService = postService;
        this.userService = userService;
        this.commentService = commentService;
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

    @GetMapping("/users/me")
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
            model.addAttribute("totalViews", postService.calculatePostsView());
            model.addAttribute("totalLikes", postService.calculatePostsLike());
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        }
        return "profile";
    }

    @PostMapping("/posts/create")
    public String processWritePost(Model model, @Valid CreatePostRequest createPostRequest) {
        try {
            PostModel postModel = postService.publishPost(createPostRequest);
            return "redirect:/posts/" + postModel.getId();
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "write-post";
        }
    }

    @GetMapping("/posts/{id}")
    public String viewPost(Model model, @Valid @PathVariable("id") Long id) {
        try {
            PostModel postModel = postService.getPostById(id);

            if (postModel == null) {
                throw new Exception("Post not found");
            }

            UserModel currentUser = userService.getCurrentUser();
            if (currentUser != null) {
                model.addAttribute("user", currentUser);
            } else {
                return "redirect:/auth/login";
            }

            Page<PostModel> notablePosts = postService.getNotablePostsExceptFor(postModel.getId());
            Page<PostModel> relatedPosts = postService.searchRelatedPostsByTag(postModel.getId(), postModel.getTags());
            List<CommentView> comments = postModel.getComments().stream()
                    .map(c -> new CommentView(
                            c,
                            c.getLikedBy().contains(currentUser),
                            c.getDislikedBy().contains(currentUser)
                    ))
                    .toList();

            model.addAttribute("post", postModel);
            model.addAttribute("notablePosts", notablePosts.getContent());
            model.addAttribute("relatedPosts", relatedPosts.getContent());
            model.addAttribute("isPostLikedByUser", postModel.isLikedBy(currentUser));
            model.addAttribute("isPostDislikedByUser", postModel.isDislikedBy(currentUser));
            model.addAttribute("comments", comments);

            return "post-details";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        }

    }

    @PostMapping("/posts/{id}/comment")
    public String postComment(Model model, @Valid @PathVariable("id") Long id, String content) {
        try {
            postService.addCommentToPost(id, content);
            return "redirect:/posts/" + id;
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        }
    }

    @ResponseBody
    @PostMapping("/posts/{id}/like")
    public ResponseEntity<Map<String, Object>> likePost(Model model, @Valid @PathVariable("id") Long id) {
        try {
            UserModel currentUser = userService.getCurrentUser();

            if (currentUser == null) {
                return ResponseEntity.status(401).build();
            }

            PostModel postModel = postService.likePost(id);

            if (postModel == null) {
                return ResponseEntity.badRequest().build();
            }

            return ResponseEntity.ok(Map.of(
                    "likeCount", postModel.getLikeCount(),
                    "dislikeCount", postModel.getDislikeCount(),
                    "isPostLikedByUser", postModel.isLikedBy(currentUser),
                    "isPostDislikedByUser", postModel.isDislikedBy(currentUser)
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @ResponseBody
    @PostMapping("/posts/{id}/dislike")
    public ResponseEntity<Map<String, Object>> dislikePost(Model model, @Valid @PathVariable("id") Long id) {
        try {
            UserModel currentUser = userService.getCurrentUser();

            if (currentUser == null) {
                return ResponseEntity.status(401).build();
            }

            PostModel postModel = postService.dislikePost(id);

            if (postModel == null) {
                return ResponseEntity.badRequest().build();
            }
            return ResponseEntity.ok(Map.of(
                    "likeCount", postModel.getLikeCount(),
                    "dislikeCount", postModel.getDislikeCount(),
                    "isPostLikedByUser", postModel.isLikedBy(currentUser),
                    "isPostDislikedByUser", postModel.isDislikedBy(currentUser)
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/comments/{id}/like")
    public String likeComment(Model model, @Valid @PathVariable("id") Long id) {
        try {
            Long postId = commentService.likeComment(id);
            return "redirect:/posts/" + postId;
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        }
    }

    @PostMapping("/comments/{id}/dislike")
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
