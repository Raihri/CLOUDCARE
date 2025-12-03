
 package com.example.cloud.care.config;

import com.example.cloud.care.dao.AdminDao;
import com.example.cloud.care.model.Admin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class AdminBootstrapConfig {

    @Value("${ADMIN_USERNAME}")
    private String adminUsername;

    @Value("${ADMIN_PASSWORD}")
    private String adminPassword;

    @Bean
    public CommandLineRunner createInitialAdmin(AdminDao adminDao, BCryptPasswordEncoder passwordEncoder) {
        return args -> {
            if (adminDao.findByUsername(adminUsername).isEmpty()) {
                Admin admin = new Admin();
                admin.setUsername(adminUsername);
                admin.setPassword(passwordEncoder.encode(adminPassword));
                adminDao.save(admin);
                System.out.println("✅ Initial admin created: " + adminUsername);
            }
        };
    }
} 
