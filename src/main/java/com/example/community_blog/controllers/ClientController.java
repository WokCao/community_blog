package com.example.community_blog.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ClientController {
    @GetMapping("/")
    public String home(Model model) {
        return "index"; // Return the name of the view (e.g., index.html)
    }

    @GetMapping("/login")
    public String login(Model model) {
        return "login"; // Return the name of the login view (e.g., login.html)
    }

    @GetMapping("/register")
    public String register(Model model) {
        return "register"; // Return the name of the register view (e.g., register.html)
    }
}
