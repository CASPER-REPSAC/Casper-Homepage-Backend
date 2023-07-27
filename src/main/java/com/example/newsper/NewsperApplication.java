package com.example.newsper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
public class NewsperApplication {

	public static void main(String[] args) {
		SpringApplication.run(NewsperApplication.class, args);
	}

}
