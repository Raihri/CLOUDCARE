package com.example.cloud.care.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
                // Handle static resources from classpath:/static/
                registry.addResourceHandler("/**")
                                .addResourceLocations("classpath:/static/");

                // Handle doctor profile images from doctor-pic-uploads directory
                registry.addResourceHandler("/doctor-pic-uploads/**")
                                .addResourceLocations("file:doctor-pic-uploads/");
        }
}