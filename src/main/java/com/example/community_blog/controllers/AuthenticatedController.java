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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

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
        }

        Page<PostModel> postModelPage = postService.getLatestPosts();
        model.addAttribute("posts", postModelPage.getContent());
        model.addAttribute("totalElements", postModelPage.getTotalElements());
        return "homepage";
    }

    @GetMapping("/write-post")
    public String writePost(Model model) {
        return "write-post";
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
            e.printStackTrace();
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

    @PostMapping("/posts/{id}/like")
    public String likePost(Model model, @Valid @PathVariable("id") Long id) {
        try {
            postService.likePost(id);
            return "redirect:/posts/" + id;
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        }
    }

    @PostMapping("/posts/{id}/dislike")
    public String dislikePost(Model model, @Valid @PathVariable("id") Long id) {
        try {
            postService.dislikePost(id);
            return "redirect:/posts/" + id;
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        }
    }
}
