package com.example.community_blog.dto;

import com.example.community_blog.models.Visibility;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class CreatePostRequest {
    @NotNull(message = "Post title cannot be null")
    @NotEmpty(message = "Title cannot be empty")
    private String title;

    @NotNull(message = "Post tags cannot be null")
    @NotEmpty(message = "Tags cannot be empty")
    private Set<String> tags = new HashSet<>();

    @NotNull(message = "Post content cannot be null")
    @NotEmpty(message = "Content cannot be empty")
    @Size(min = 10, message = "Content must be at least 10 characters long")
    private String content;

    @NotNull(message = "Post allow comment cannot be null")
    private boolean allowComment;

    @NotNull(message = "Post visibility cannot be null")
    private Visibility visibility;
}
