package com.example.community_blog.dto;

import com.example.community_blog.models.CommentModel;

public record CommentView(
        CommentModel comment,
        boolean liked,
        boolean disliked
) {}

