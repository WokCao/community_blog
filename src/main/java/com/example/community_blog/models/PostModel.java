package com.example.community_blog.models;

import com.example.community_blog.utils.TimeAgoUtil;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "posts")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"author", "comments", "tags"})
public class PostModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
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

    @ManyToMany
    @JoinTable(name = "post_likes",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<UserModel> likedBy = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "post_dislikes",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<UserModel> dislikedBy = new HashSet<>();

    @Column
    private LocalDateTime autoPublishAt;

    @Transient
    public String getStringAutoPublishAt() {
        // Format: 01-01-2021 00:00:00
        if (autoPublishAt == null) return "";
        return autoPublishAt.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
    }

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
    private Long shareCount = 0L;

    public void addComment(CommentModel comment) {
        comments.add(comment);
        comment.setPost(this);
    }

    public void removeComment(CommentModel comment) {
        comments.remove(comment);
        comment.setPost(null);
    }

    public Long getCommentCount() {
        return (long) comments.size();
    }

    public Long getLikeCount() {
        return (long) likedBy.size();
    }

    public Long getDislikeCount() {
        return (long) dislikedBy.size();
    }

    @Transient
    public boolean isLikedBy(UserModel user) {
        return likedBy.contains(user);
    }

    @Transient
    public boolean isDislikedBy(UserModel user) {
        return dislikedBy.contains(user);
    }

    @Transient
    public void addUserWhoDislikePost(UserModel user) {
        if (isDislikedBy(user)) {
            dislikedBy.remove(user);
        } else {
            dislikedBy.add(user);
            likedBy.remove(user);
        }
    }

    @Transient
    public void addUserWhoLikePost(UserModel user) {
        if (isLikedBy(user)) {
            likedBy.remove(user);
        } else {
            likedBy.add(user);
            dislikedBy.remove(user);
        }
    }

    @Transient
    public String getPlainContent() {
        if (content == null) return "";
        String text = content;
        text = text.replaceAll("(?is)<script[^>]*>.*?</script>", " ");
        text = text.replaceAll("(?is)<style[^>]*>.*?</style>", " ");
        text = text.replaceAll("<[^>]+>", " ");
        text = text.replaceAll("&nbsp;", " ");
        text = text.replaceAll("&amp;", "&");
        text = text.replaceAll("&lt;", "<");
        text = text.replaceAll("&gt;", ">");
        text = text.replaceAll("&quot;", "\"");
        text = text.replaceAll("&apos;", "'");
        return text.replaceAll("\\s+", " ").trim();
    }
}
