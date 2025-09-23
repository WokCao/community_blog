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

    @Value("${BLOG.CONTROL.EMAIL.ACCOUNT}")
    private String controlEmail;

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
}
