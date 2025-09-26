package com.example.community_blog.models;

import com.example.community_blog.utils.TimeAgoUtil;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "comments")
@Data
public class CommentModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private PostModel post;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "commenter_id", nullable = false)
    private UserModel commenter;

    public String getTimeAgo() {
        return TimeAgoUtil.getTimeAgo(updatedAt);
    }

    @ManyToMany(fetch =  FetchType.EAGER)
    @JoinTable(name = "comment_likes",
            joinColumns = @JoinColumn(name = "comment_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<UserModel> likedBy = new HashSet<>();

    @ManyToMany(fetch =  FetchType.EAGER)
    @JoinTable(name = "comment_dislikes",
            joinColumns = @JoinColumn(name = "comment_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<UserModel> dislikedBy = new HashSet<>();

    @CreationTimestamp
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public Long getLikeCount() {
        return (long) likedBy.size();
    }

    public Long getDislikeCount() {
        return (long) dislikedBy.size();
    }

    @Transient
    public void addUserWhoDislikeComment(UserModel user) {
        if (dislikedBy.contains(user)) {
            dislikedBy.remove(user);
        } else {
            dislikedBy.add(user);
            likedBy.remove(user);
        }
    }

    @Transient
    public void addUserWhoLikeComment(UserModel user) {
        if (likedBy.contains(user)) {
            likedBy.remove(user);
        } else {
            likedBy.add(user);
            dislikedBy.remove(user);
        }
    }
}
