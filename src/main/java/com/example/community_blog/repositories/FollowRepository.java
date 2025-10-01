package com.example.community_blog.repositories;

import com.example.community_blog.models.FollowModel;
import com.example.community_blog.models.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface FollowRepository extends JpaRepository<FollowModel, Long> {
    List<FollowModel> findByFollowing(UserModel user);

    boolean existsByFollowingAndFollower(UserModel following, UserModel follower);

    @Transactional
    @Modifying(clearAutomatically = true)
    void deleteByFollowingAndFollower(UserModel following, UserModel follower);
}
