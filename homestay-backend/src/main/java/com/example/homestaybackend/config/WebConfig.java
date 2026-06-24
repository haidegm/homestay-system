package com.example.homestaybackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 1. 原有的房源图片映射
        registry.addResourceHandler("/house/**")
                .addResourceLocations("file:D:/wlh/homestay_upload/house/");

        // 2. 新增：用户头像映射
        // 浏览器访问 http://localhost:8080/uploads/avatar/xxx.jpg
        // 实际指向 D:/wlh/homestay_upload/useravator/xxx.jpg
        registry.addResourceHandler("/uploads/avatar/**")
                .addResourceLocations("file:D:/wlh/homestay_upload/useravator/");
    }
}