package com.example.community_blog.controllers;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

            Page<PostModel> notablePosts = postService.getNotablePostsExceptFor(postModel.getId());
            Page<PostModel> relatedPosts = postService.searchRelatedPostsByTag(postModel.getId(), postModel.getTags());

            model.addAttribute("post", postModel);
            model.addAttribute("user", postModel.getAuthor());
            model.addAttribute("notablePosts", notablePosts.getContent());
            model.addAttribute("relatedPosts", relatedPosts.getContent());
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

    @GetMapping("/posts/test")
    public String testPost(Model model) {
        // Mock author
        Map<String, Object> author = new HashMap<>();
        author.put("fullname", "Jane Doe");
        author.put("avatar", "https://i.pravatar.cc/150?img=5");

        // Mock post
        Map<String, Object> post = new HashMap<>();
        post.put("id", 101L);
        post.put("title", "Understanding Spring Boot with Real Examples");
        post.put("excerpt", "In this article, we’ll dive deep into Spring Boot, covering practical use cases and best practices.");
        post.put("author", author);
        post.put("timeAgo", "2 hours ago");
        post.put("viewCount", "1.2k");
        post.put("commentCount", "45");
        post.put("saveCount", "89");
        post.put("likeCount", 24);
        post.put("dislikeCount", 3);
        post.put("userLiked", true);
        post.put("userDisliked", false);
        post.put("topics", List.of("Spring Boot", "Java", "Backend Development"));
        post.put("content", """
        <h1>Introduction</h1>
        <p>Spring Boot simplifies the development of production-grade applications with minimal configuration. 
        In this post we will explore features such as auto-configuration, Spring Data JPA, and Spring Security.</p>

        <h2>1. Why Spring Boot?</h2>
        <p>Spring Boot removes boilerplate and lets developers focus on business logic instead of setup. 
        It provides embedded servers, starter dependencies, and easy integration with databases.</p>

        <h2>2. Using Spring Data JPA</h2>
        <p>Spring Data JPA allows you to work with databases using repositories instead of verbose DAO code. 
        Example: <code>UserRepository extends JpaRepository&lt;User, Long&gt;</code>.</p>

        <h3>Example Query Methods</h3>
        <ul>
          <li><code>findByEmail(String email)</code></li>
          <li><code>findByStatusOrderByCreatedAtDesc(String status)</code></li>
        </ul>

        <h2>3. Redis Caching</h2>
        <p>Spring Boot integrates seamlessly with Redis. Caching annotations such as 
        <code>@Cacheable</code> and <code>@CacheEvict</code> help improve performance.</p>

        <h2>4. Security with JWT</h2>
        <p>With Spring Security, JWT authentication can protect APIs. 
        A custom <code>OncePerRequestFilter</code> validates tokens on each request.</p>

        <h2>Conclusion</h2>
        <p>Spring Boot accelerates Java backend development. Combined with JPA, Redis, and Security, 
        it provides a powerful platform for building modern web applications.</p>
    """);

        // Mock comments
        List<Map<String, Object>> comments = new ArrayList<>();
        comments.add(Map.of(
                "author", Map.of(
                        "fullname", "John Smith",
                        "avatar", "https://i.pravatar.cc/100?img=12"),
                "timeAgo", "1 hour ago",
                "content", "Great explanation! I especially liked the JPA examples."
        ));
        comments.add(Map.of(
                "author", Map.of(
                        "fullname", "Emily Johnson",
                        "avatar", "https://i.pravatar.cc/100?img=32"),
                "timeAgo", "30 minutes ago",
                "content", "Could you also write about microservices with Spring Cloud?"
        ));
        post.put("comments", comments);

        // Mock related posts
        List<Map<String, Object>> relatedPosts = List.of(
                Map.of("id", 201L, "title", "Getting Started with Spring Security",
                        "excerpt", "Learn how to add authentication and authorization to your app.",
                        "thumbnail", "https://picsum.photos/300/200?1",
                        "author", Map.of("fullname", "Alice Brown"),
                        "timeAgo", "3 days ago"),
                Map.of("id", 202L, "title", "Mastering Spring Data JPA",
                        "excerpt", "Advanced features of JPA with Spring Boot.",
                        "thumbnail", "https://picsum.photos/300/200?2",
                        "author", Map.of("fullname", "Robert Lee"),
                        "timeAgo", "5 days ago")
        );

        // Mock notable posts by author
        List<Map<String, Object>> authorNotablePosts = List.of(
                Map.of("id", 301L, "title", "Spring Boot Testing with JUnit 5",
                        "excerpt", "How to write effective tests in Spring Boot.",
                        "thumbnail", "https://picsum.photos/200/200?3",
                        "timeAgo", "1 week ago", "viewCount", "2.1k"),
                Map.of("id", 302L, "title", "Introduction to REST APIs",
                        "excerpt", "REST basics every developer should know.",
                        "thumbnail", "https://picsum.photos/200/200?4",
                        "timeAgo", "2 weeks ago", "viewCount", "3.4k")
        );

        // Mock logged-in user
        Map<String, Object> user = Map.of(
                "fullname", "Current User",
                "avatar", "https://i.pravatar.cc/100?img=55"
        );

        // Add to model
        model.addAttribute("post", post);
        model.addAttribute("relatedPosts", relatedPosts);
        model.addAttribute("authorNotablePosts", authorNotablePosts);
        model.addAttribute("user", user);

        return "post-details";
    }
}
