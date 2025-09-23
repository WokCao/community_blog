package com.example.community_blog.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Collections;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth ->
                        auth.requestMatchers("/auth/**", "/", "/css/**", "/js/**", "/images/**").permitAll()
                        .anyRequest().authenticated())
                .sessionManagement(session -> session
                    .maximumSessions(1)
                    .maxSessionsPreventsLogin(false)
                        .expiredUrl("/auth/login?expired=true")
                )
                .cors(cors -> cors.configurationSource(new CorsConfigurationSource() {
                    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                        CorsConfiguration config = new CorsConfiguration();
                        config.addAllowedOriginPattern(Collections.singletonList("localhost:3000").toString());
                        config.setAllowCredentials(true);
                        config.addAllowedHeader("*");
                        config.addAllowedMethod("*");
                        config.setMaxAge(3600L);
                        return config;
                    }
                }))
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
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
