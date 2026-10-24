package com.example.cloud.care.config;

import com.example.cloud.care.security.AllowedOrigins;
import com.example.cloud.care.security.OriginCheckFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Cross-origin policy: CORS for the JSON API and the CSRF origin check, both
 * driven by {@code cloudcare.allowed-origins}.
 */
@Configuration
public class CrossOriginConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer(AllowedOrigins allowedOrigins) {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins(allowedOrigins.asArray())
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowCredentials(true);
            }
        };
    }

    /** Runs before Spring Security so a forged request never reaches a handler. */
    @Bean
    public FilterRegistrationBean<OriginCheckFilter> originCheckFilter(AllowedOrigins allowedOrigins) {
        FilterRegistrationBean<OriginCheckFilter> registration =
                new FilterRegistrationBean<>(new OriginCheckFilter(allowedOrigins.asSet()));
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        registration.addUrlPatterns("/*");
        return registration;
    }
}
