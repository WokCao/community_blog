package com.example.community_blog.services;

import com.example.community_blog.dto.BlogPublishedEvent;
import com.example.community_blog.models.FollowModel;
import com.example.community_blog.models.NotificationModel;
import com.example.community_blog.models.PostModel;
import com.example.community_blog.models.UserModel;
import com.example.community_blog.repositories.FollowRepository;
import com.example.community_blog.repositories.PostRepository;
import com.example.community_blog.repositories.UserRepository;
import com.example.community_blog.utils.TimeAgoUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class BlogEventSubscriber implements MessageListener {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;
    private final PostRepository postRepository;

    public BlogEventSubscriber(RedisMessageListenerContainer container,
                               FollowRepository followRepository,
                               UserRepository userRepository,
                               SimpMessagingTemplate messagingTemplate,
                               NotificationService notificationService,
                               PostRepository postRepository) {
        this.followRepository = followRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
        this.notificationService = notificationService;
        this.postRepository = postRepository;

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
                Optional<PostModel> optionalPostModel = postRepository.findById(event.getPostId());

                if (optionalPostModel.isPresent()) {
                    NotificationModel notificationModel = notificationService.createNotification(
                            follower,
                            author,
                            event.getTitle(),
                            optionalPostModel.get()
                    );

                    // Send to WebSocket user-specific queue
                    messagingTemplate.convertAndSendToUser(
                            follower.getEmail(), // Principal name
                            "/queue/notifications",
                            Map.of(
                                    "author", author.getFullName(),
                                    "title", event.getTitle(),
                                    "postId", event.getPostId(),
                                    "time", TimeAgoUtil.getTimeAgo(notificationModel.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant()),
                                    "avatarUrl", author.getAvatarUrl()
                            )
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}