package com.example.community_blog.services;

import com.example.community_blog.dto.RegisterRequest;
import com.example.community_blog.models.Role;
import com.example.community_blog.models.UserModel;
import com.example.community_blog.repositories.RoleRepository;
import com.example.community_blog.repositories.UserRepository;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String PRODUCT_CACHE = "verificationToken:";

    @Value("${blog.control.email.account}")
    private String controlEmail;

    public UserModel getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = null;

        if (authentication.getPrincipal() instanceof org.springframework.security.oauth2.core.user.OAuth2User oAuth2User) {
            email = oAuth2User.getAttribute("email");
        } else if (authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
            email = userDetails.getUsername();
        } else {
            email = authentication.getName();
        }

        if (email == null) {
            return null;
        }

        return userRepository.findByEmail(email).orElse(null);
    }

    @Transactional
    public UserModel registerUser(RegisterRequest registerRequest) throws BadRequestException {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new BadRequestException("Email already registered");
        }
        Role roleUser = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_USER")));

        Role roleAdmin = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_ADMIN")));

        if (!Objects.equals(registerRequest.getEmail(), controlEmail)) {
            roleAdmin = null;
        }

        UserModel user = new UserModel();
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setFullName(registerRequest.getFullName());
        user.setAvatarUrl("bear.png");
        user.setRoles(roleAdmin != null ? Set.of(roleUser, roleAdmin) : Set.of(roleUser));
        return userRepository.save(user);
    }

    @Transactional
    public String createVerificationToken(UserModel user) {
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(PRODUCT_CACHE + token, user.getEmail(), Duration.ofMinutes(5));
        return token;
    }

    @Transactional
    public boolean verifyUser(String token, String email) {
        System.out.println(token);
        System.out.println(email);
        String cachedEmail = (String) redisTemplate.opsForValue().get(PRODUCT_CACHE + token);
        System.out.println(cachedEmail);
        if (cachedEmail != null && cachedEmail.equals(email)) {
            UserModel user = userRepository.findByEmail(email).orElse(null);
            if (user != null) {
                user.setEnabled(true);
                userRepository.save(user);
                redisTemplate.delete(PRODUCT_CACHE + token);
                return true;
            }
        }
        return false;
    }

    public Long countUsers() {
        return userRepository.count();
    }

    public boolean updateAvatar(String avatarUrl) throws BadRequestException {
        UserModel currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new BadRequestException("User not authenticated");
        }

        currentUser.setAvatarUrl(avatarUrl);
        userRepository.save(currentUser);
        return true;
    }

    public boolean updateFullName(String fullName) throws BadRequestException {
        UserModel currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new BadRequestException("User not authenticated");
        }

        currentUser.setFullName(fullName);
        userRepository.save(currentUser);
        return true;
    }
}
