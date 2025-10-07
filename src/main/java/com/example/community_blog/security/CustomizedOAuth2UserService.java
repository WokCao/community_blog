package com.example.community_blog.security;

import com.example.community_blog.models.Role;
import com.example.community_blog.models.UserModel;
import com.example.community_blog.repositories.RoleRepository;
import com.example.community_blog.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Set;

@Service
public class CustomizedOAuth2UserService extends DefaultOAuth2UserService {
    @Value("${blog.control.email.account}")
    private String controlEmail;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Autowired
    public CustomizedOAuth2UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        if (email == null) {
            throw new UsernameNotFoundException("Email not found from Google account");
        }

        UserModel userModel = userRepository.findByEmail(email).orElse(null);
        if (userModel != null && !userModel.getPassword().isEmpty()) {
            throw new OAuth2AuthenticationException("Email already registered");
        } else if (userModel == null) {
            saveOAuth2User(oAuth2User);
        }
        return oAuth2User;
    }

    private void saveOAuth2User(OAuth2User oAuth2User) {
        Role roleUser = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_USER")));

        Role roleAdmin = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_ADMIN")));

        String email = oAuth2User.getAttribute("email");

        userRepository.findByEmail(email).orElseGet(() -> {
            UserModel user = new UserModel();
            user.setEmail(email);
            user.setFullName(oAuth2User.getAttribute("name"));
            user.setPassword("");
            user.setEnabled(true);
            user.setAvatarUrl(oAuth2User.getAttribute("picture"));
            if (checkControlEmail(email)) {
                user.setRoles(Set.of(roleUser, roleAdmin));
            } else {
                user.setRoles(Set.of(roleUser));
            }
            return userRepository.save(user);
        });
    }

    private boolean checkControlEmail(String oAuth2Email) {
        return Objects.equals(oAuth2Email, controlEmail);
    }
}