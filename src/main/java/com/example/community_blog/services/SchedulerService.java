package com.example.community_blog.services;

import com.example.community_blog.dto.BlogPublishedEvent;
import com.example.community_blog.models.PostModel;
import com.example.community_blog.repositories.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SchedulerService {
    private final PostRepository postRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public SchedulerService(PostRepository postRepository, RedisTemplate<String, Object> redisTemplate) {
        this.postRepository = postRepository;
        this.redisTemplate = redisTemplate;
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
}
