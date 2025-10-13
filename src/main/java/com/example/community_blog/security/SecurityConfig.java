package com.example.community_blog.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
public class SecurityConfig {
    private final CustomizedOAuth2UserService customizedOAuth2UserService;

    @Value("${blog.base.url}")
    private String BASE_URL;

    @Autowired
    public SecurityConfig(CustomizedOAuth2UserService customizedOAuth2UserService) {
        this.customizedOAuth2UserService = customizedOAuth2UserService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(new CorsConfigurationSource() {
                    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                        CorsConfiguration config = new CorsConfiguration();
                        config.addAllowedOriginPattern("http://localhost:8080");
                        config.addAllowedOriginPattern("https://localhost:8080");
                        config.addAllowedOriginPattern(BASE_URL);
                        config.setAllowCredentials(true);
                        config.addAllowedHeader("*");
                        config.addAllowedMethod("*");
                        config.setMaxAge(3600L);
                        return config;
                    }
                }))
                .csrf(csrf -> csrf.ignoringRequestMatchers("/login/oauth2/code/google", "/bot/fetch"))
                .headers(headers -> headers
                        .contentSecurityPolicy(policy -> policy
                                .policyDirectives(
                                        "default-src 'self'; " +
                                                "img-src 'self' data: https://lh3.googleusercontent.com https://platform-lookaside.fbsbx.com; " +
                                                "script-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net https://cdnjs.cloudflare.com; " +
                                                "style-src 'self' 'unsafe-inline' https://cdnjs.cloudflare.com https://fonts.googleapis.com; " +
                                                "font-src 'self' https://cdnjs.cloudflare.com https://fonts.gstatic.com data:; " +
                                                "connect-src 'self' ws://localhost:8080 wss://localhost:8080 https://cdn.jsdelivr.net;"
                                )
                        )
                )
                .sessionManagement(session -> session
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                        .expiredUrl("/auth/login?expired=true")
                )
                .authorizeHttpRequests(auth ->
                        auth.requestMatchers("/auth/**", "/", "/css/**", "/js/**", "/img/**", "/posts", "/posts/*", "/oauth2/**", "/login/oauth2/**", "/bot/fetch").permitAll()
                                .anyRequest().authenticated())
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/auth/login")
                        .userInfoEndpoint(userInfo -> {
                            userInfo.userService(customizedOAuth2UserService);
                        })
                        .defaultSuccessUrl("/home", true)
                        .failureUrl("/auth/login?error=true")
                )
                .formLogin(form -> form
                        .loginPage("/auth/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .permitAll()
                        .successHandler((request, response, authentication) -> {

                            response.sendRedirect("/home");
                        })
                        .failureHandler((request, response, authentication) -> {
                            response.sendRedirect("/auth/login?error=true");
                        })
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .httpBasic(AbstractHttpConfigurer::disable);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
