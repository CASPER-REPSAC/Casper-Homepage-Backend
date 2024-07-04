package com.example.newsper.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/profile/**")
                .addResourceLocations("file:///home/casper/newsper_profile/");
        registry.addResourceHandler("/article/**")
                .addResourceLocations("file:///home/casper/newsper_article/");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // 모든 경로에 대해 허용
                .allowedOrigins("https://casper.or.kr", "https://oauth2.googleapis.com", "https://www.googleapis.com", "https://github.com", "https://api.github.com")  // 모든 도메인 허용
                .allowedMethods("*")  // 모든 HTTP 메서드 허용
                .allowedHeaders("*")  // 모든 헤더 허용
                .allowCredentials(true);
    }
}
