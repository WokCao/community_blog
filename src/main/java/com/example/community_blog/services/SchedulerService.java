package com.example.community_blog.services;

import com.example.community_blog.dto.BlogPublishedEvent;
import com.example.community_blog.models.PostModel;
import com.example.community_blog.models.Role;
import com.example.community_blog.models.UserModel;
import com.example.community_blog.models.Visibility;
import com.example.community_blog.repositories.PostRepository;
import com.example.community_blog.repositories.RoleRepository;
import com.example.community_blog.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class SchedulerService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RestTemplate restTemplate = new RestTemplate();
    private final PasswordEncoder passwordEncoder;

    @Value("${newsapi.key}")
    private String newsApiKey;
    @Value("${newsapi.url}")
    private String newsApiUrl;
    @Value("${newsapi.pageSize}")
    private int newsApiPageSize;
    @Value("${newsapi.query}")
    private String newsApiQuery;
    @Value("${newsapi.domains}")
    private String newsApiDomains;
    @Value("${bot.secret}")
    private String botSecret;

    @Autowired
    public SchedulerService(PostRepository postRepository, UserRepository userRepository, RoleRepository roleRepository,RedisTemplate<String, Object> redisTemplate, PasswordEncoder passwordEncoder) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.redisTemplate = redisTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    @Scheduled(cron = "0 * * * * *")
    public void autoPublishPosts() {
        List<PostModel> readyPosts = postRepository.findReadyToPublish();

        for (PostModel post : readyPosts) {
            post.setAutoPublishAt(null);

            BlogPublishedEvent event = new BlogPublishedEvent(post.getId(), post.getAuthor().getId(), post.getTitle());
            redisTemplate.convertAndSend("blog-published", event);
        }

        postRepository.saveAll(readyPosts);
    }

    @Scheduled(initialDelay = 10, timeUnit = TimeUnit.SECONDS, fixedRate = 1000 * 60 * 60 * 2)
    public void fetchArticles() {
        String url = String.format("%s?apiKey=%s&q=%s&domains=%s&pageSize=%d&language=en&sortBy=publishedAt",
                newsApiUrl, newsApiKey, newsApiQuery, newsApiDomains, newsApiPageSize);

        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response == null || !"ok".equals(response.get("status"))) {
                return;
            }

            List<Map<String, Object>> articles = (List<Map<String, Object>>) response.get("articles");
            if (articles == null) return;

            UserModel botUser = getBotUser();

            for (Map<String, Object> article : articles) {
                String title = (String) article.get("title");
                String description = (String) article.get("description");
                String content = (String) article.get("content");
                String urlToArticle = (String) article.get("url");
                String source = ((Map<String, String>) article.get("source")).get("name");

                if (title == null || postRepository.existsByTitle(title)) continue;

                PostModel post = new PostModel();
                post.setTitle(title);
                post.setContent(buildContent(description, cleanContent(content), urlToArticle, source));
                post.setAuthor(botUser);
                post.setAllowComment(true);
                post.setVisibility(Visibility.PUBLIC);
                post.setUpdatedAt(Instant.now());
                post.setTags(generateTags(title, content));
                post.setThumbnailUrl("bot_thumbnail.jpg");

                postRepository.save(post);
            }

        } catch (Exception e) {
            System.err.println("[BOT] Error fetching programming news: " + e.getMessage());
        }
    }

    private String buildContent(String description, String content, String url, String source) {
        StringBuilder sb = new StringBuilder();
        if (description != null) sb.append("<p>").append(description).append("</p>");
        if (content != null) sb.append("<p>").append(content).append("</p>");
        sb.append("<p><i>Source: <a href=\"").append(url).append("\">")
                .append(source != null ? source : "NewsAPI.org")
                .append("</a></i></p>");
        return sb.toString();
    }

    private Set<String> generateTags(String title, String content) {
        AIService ai = new AIService();
        return ai.extractTags(title, content);
    }

    private UserModel getBotUser() {
        Role roleUser = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_USER")));

        return userRepository.findByEmail("bot@community.com")
                .orElseGet(() -> {
                    UserModel bot = new UserModel();
                    bot.setEmail("bot@community.com");
                    bot.setAvatarUrl("bot.jpg");
                    bot.setEnabled(true);
                    bot.setPassword(passwordEncoder.encode(botSecret));
                    bot.setRoles(Set.of(roleUser));
                    bot.setFullName("Bot");
                    return userRepository.save(bot);
                });
    }

    private String cleanContent(String rawContent) {
        if (rawContent == null) return "";
        return rawContent.replaceAll("\\[\\+\\d+ chars]", "").trim();
    }
}
