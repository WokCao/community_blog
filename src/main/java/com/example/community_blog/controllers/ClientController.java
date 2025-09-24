package com.example.community_blog.controllers;

import com.example.community_blog.dto.RegisterRequest;
import com.example.community_blog.models.UserModel;
import com.example.community_blog.services.EmailService;
import com.example.community_blog.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Controller
public class ClientController {
    @Autowired
    private UserService userService;
    @Autowired
    private EmailService emailService;

    @GetMapping("/")
    public String home(Model model) {
        return "index";
    }

    @GetMapping("/auth/login")
    public String login(Model model) {
        return "login";
    }

    @GetMapping("/auth/register")
    public String register(Model model) {
        return "register";
    }

    @PostMapping("/auth/register")
    public String processRegister(@ModelAttribute("user") RegisterRequest registerRequest, Model model) {
        try {
            UserModel savedUser = userService.registerUser(registerRequest);

            String token = userService.createVerificationToken(savedUser);

            String encodedEmail = Base64.getUrlEncoder()
                    .encodeToString(savedUser.getEmail().getBytes(StandardCharsets.UTF_8));

            String verificationUrl = "http://localhost:8080/auth/verify?token=" + token + "&email=" + encodedEmail;

            emailService.sendVerificationEmail(savedUser.getEmail(), verificationUrl);

            model.addAttribute("successMessage", "Registration successful! Please check your email to verify your account.");
            return "register";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/auth/verify")
    public String verifyAccount(@RequestParam("token") String token, @RequestParam("email") String encodedEmail, Model model) {
        String email = new String(Base64.getUrlDecoder().decode(encodedEmail), StandardCharsets.UTF_8);
        boolean verified = userService.verifyUser(token, email);

        if (verified) {
            model.addAttribute("successMessage", "Account verified! You can now log in.");
        } else {
            model.addAttribute("errorMessage", "Invalid or expired verification link.");
        }
        return "login";
    }
}
