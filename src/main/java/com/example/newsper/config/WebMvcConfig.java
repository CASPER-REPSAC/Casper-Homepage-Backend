package com.example.newsper.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String homePath="/home/casper/";
        registry.addResourceHandler("/profile/**")
                .addResourceLocations("file://" + homePath + "profile/");
        registry.addResourceHandler("/article/**")
                .addResourceLocations("file://" + homePath + "article/");
        registry.addResourceHandler("/assignment/**")
                .addResourceLocations("file://" + homePath + "assignment/");
        registry.addResourceHandler("/submit/**")
                .addResourceLocations("file://" + homePath + "submit/");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(false);
    }
}
