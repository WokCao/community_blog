package com.example.community_blog.models;

import lombok.Getter;

@Getter
public enum Visibility {
    PUBLIC("PUBLIC"),
    PRIVATE("PRIVATE");

    private final String value;

    Visibility(String value) {
        this.value = value;
    }

}
