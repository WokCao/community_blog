package com.example.community_blog.controllers;

import com.example.community_blog.dto.CommentView;
import com.example.community_blog.dto.CreatePostRequest;
import com.example.community_blog.models.PostModel;
import com.example.community_blog.models.UserModel;
import com.example.community_blog.services.PostService;
import com.example.community_blog.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/posts")
public class PostController {
    private final PostService postService;
    private final UserService userService;

    @Autowired
    public PostController(PostService postService, UserService userService) {
        this.postService = postService;
        this.userService = userService;
    }

    @PostMapping("/create")
    public String processWritePost(Model model, @Valid CreatePostRequest createPostRequest) {
        try {
            PostModel postModel = postService.publishPost(createPostRequest);
            return "redirect:/posts/" + postModel.getId();
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "write-post";
        }
    }

    @GetMapping("/{id}")
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

    @PostMapping("/{id}/comment")
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
    @PostMapping("/{id}/like")
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
    @PostMapping("/{id}/dislike")
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
}
