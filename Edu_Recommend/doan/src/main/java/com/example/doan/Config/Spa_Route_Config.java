package com.example.doan.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class Spa_Route_Config implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/home").setViewName("forward:/index.html");
        registry.addViewController("/courses").setViewName("forward:/index.html");
        registry.addViewController("/login").setViewName("forward:/index.html");
        registry.addViewController("/register").setViewName("forward:/index.html");
        registry.addViewController("/cart").setViewName("forward:/index.html");
        registry.addViewController("/student").setViewName("forward:/index.html");
        registry.addViewController("/teacher").setViewName("forward:/index.html");
        registry.addViewController("/admin").setViewName("forward:/index.html");
        registry.addViewController("/recommend").setViewName("forward:/index.html");
        registry.addViewController("/courses/{id}").setViewName("forward:/index.html");
        registry.addViewController("/learn/{id}").setViewName("forward:/index.html");
    }
}
