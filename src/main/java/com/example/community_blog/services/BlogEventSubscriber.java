package com.example.community_blog.services;

import com.example.community_blog.dto.BlogPublishedEvent;
import com.example.community_blog.models.FollowModel;
import com.example.community_blog.models.UserModel;
import com.example.community_blog.repositories.FollowRepository;
import com.example.community_blog.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class BlogEventSubscriber implements MessageListener {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;

    public BlogEventSubscriber(RedisMessageListenerContainer container,
                               FollowRepository followRepository,
                               UserRepository userRepository,
                               SimpMessagingTemplate messagingTemplate,
                               NotificationService notificationService) {
        this.followRepository = followRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
        this.notificationService = notificationService;

        container.addMessageListener(this, new ChannelTopic("blog-published"));
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            BlogPublishedEvent event = mapper.readValue(message.getBody(), BlogPublishedEvent.class);

            UserModel author = userRepository.findById(event.getAuthorId()).orElseThrow();
            List<FollowModel> followers = followRepository.findByFollowing(author);

            for (FollowModel f : followers) {
                UserModel follower = f.getFollower();

                notificationService.createNotification(
                        follower,
                        author,
                        event.getTitle()
                );

                // Send to WebSocket user-specific queue
                messagingTemplate.convertAndSendToUser(
                        follower.getEmail(), // Principal name
                        "/queue/notifications",
                        Map.of(
                                "author", author.getFullName(),
                                "title", event.getTitle(),
                                "postId", event.getPostId()
                        )
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}