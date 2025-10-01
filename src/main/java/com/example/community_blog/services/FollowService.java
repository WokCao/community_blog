package com.example.community_blog.services;

import com.example.community_blog.models.FollowModel;
import com.example.community_blog.models.UserModel;
import com.example.community_blog.repositories.FollowRepository;
import com.example.community_blog.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FollowService {
    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    @Autowired
    public FollowService(FollowRepository followRepository, UserRepository userRepository) {
        this.followRepository = followRepository;
        this.userRepository = userRepository;
    }

    public void followUser(UserModel follower, Long userId) {
        UserModel following = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!followRepository.existsByFollowingAndFollower(following, follower)) {
            FollowModel follow = new FollowModel();
            follow.setFollower(follower);
            follow.setFollowing(following);
            followRepository.save(follow);
        }
    }

    public void unfollowUser(UserModel follower, Long userId) {
        UserModel following = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        followRepository.deleteByFollowingAndFollower(following, follower);
    }

    public boolean isFollowing(UserModel follower, Long userId) {
        UserModel following = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return followRepository.existsByFollowingAndFollower(following, follower);
    }
}
