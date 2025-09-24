package com.example.community_blog.models;

import com.example.community_blog.utils.TimeAgoUtil;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "posts")
@Data
public class PostModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    public String getTimeAgo() {
        return TimeAgoUtil.getTimeAgo(updatedAt);
    }

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private UserModel author;

    @Column(nullable = false, columnDefinition = "BOOLEAN")
    private boolean allowComment = true;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Visibility visibility = Visibility.PUBLIC;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "post_tags", joinColumns = @JoinColumn(name = "post_id"))
    private Set<String> tags = new HashSet<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommentModel> comments = new ArrayList<>();

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

    @Column(nullable = false)
    private Long viewCount = 0L;

    @Column(nullable = false)
    private Long saveCount = 0L;

    @Column(nullable = false)
    private Long commentCount = 0L;

    @Column(nullable = false)
    private Long likeCount = 0L;

    @Column(nullable = false)
    private Long shareCount = 0L;

    public void addComment(CommentModel comment) {
        comments.add(comment);
        comment.setPost(this);
        this.commentCount++;
    }

    public void removeComment(CommentModel comment) {
        comments.remove(comment);
        comment.setPost(null);
        this.commentCount = Math.max(0, this.commentCount - 1);
    }
}
