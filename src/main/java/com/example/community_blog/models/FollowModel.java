package com.example.community_blog.models;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "follows", uniqueConstraints = @UniqueConstraint(columnNames = {"follower_id", "following_id"}))
@Data
public class FollowModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "follower_id")
    private UserModel follower;   // the user who follows

    @ManyToOne(optional = false)
    @JoinColumn(name = "following_id")
    private UserModel following;  // the user being followed

    private LocalDateTime createdAt = LocalDateTime.now();
}
