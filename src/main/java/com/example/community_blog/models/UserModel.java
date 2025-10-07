package com.example.community_blog.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"roles", "posts"})
public class UserModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private String fullName;

    @Column(nullable = true)
    private String avatarUrl;

    @Transient
    public String getAvatarUrl() {
        if (avatarUrl == null) {
            return "/img/bear.png";
        } else if (avatarUrl.startsWith("https") || isTrustedAvatarUrl(avatarUrl)) {
            return avatarUrl;
        }
        return "/img/" + avatarUrl;
    }

    @Transient
    private boolean isTrustedAvatarUrl(String url) {
        return url.startsWith("https://lh3.googleusercontent.com") ||
                url.startsWith("https://platform-lookaside.fbsbx.com");
    }

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    private boolean enabled = false;

    @CreationTimestamp
    private Instant createdAt;

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<PostModel> posts = new HashSet<>();
}
