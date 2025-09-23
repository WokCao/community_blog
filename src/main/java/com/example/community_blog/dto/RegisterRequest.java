package com.example.community_blog.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @Pattern(regexp = "^[a-zA-Z0-9_]{3,15}$", message = "Username must be 3-15 characters long and can only contain letters, numbers, and underscores")
    private String fullName;

    @Email(message = "Email should be valid")
    private String email;

    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @Size(min = 8, message = "Confirm password must be at least 8 characters long")
    private String confirmPassword;
}
