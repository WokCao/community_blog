package com.example.community_blog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CommunityBlogApplication {

	public static void main(String[] args) {
		SpringApplication.run(CommunityBlogApplication.class, args);
	}

}
