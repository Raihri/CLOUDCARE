package com.example.cloud.care.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class CloudinaryConfig {

    @Value("${cloudinary.cloud_name:}")
    private String cloudName;
    @Value("${cloudinary.api_key:}")
    private String apiKey;

    @Value("${cloudinary.api_secret:}")
    private String apiSecret;

    @Bean
    public Cloudinary cloudinary() {
        // Prefer CLOUDINARY_URL env var if available
        String url = System.getenv("CLOUDINARY_URL");
        if (url != null && !url.isBlank()) {
            return new Cloudinary(url);
        }

        // Fallback to individual properties if provided
        @SuppressWarnings("unchecked")
        Map<String, Object> cfg = ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret);

        return new Cloudinary(cfg);
    }
}