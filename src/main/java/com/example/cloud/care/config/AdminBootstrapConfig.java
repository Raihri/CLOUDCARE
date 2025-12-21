package com.example.cloud.care.config;

import com.example.cloud.care.dao.AdminDao;
import com.example.cloud.care.model.Admin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Configuration
public class AdminBootstrapConfig {

    @Value("${ADMIN_USERNAME}")
    private String adminUsername;

    @Value("${ADMIN_PASSWORD}")
    private String adminPassword;

    private final AdminDao adminDao;
    private final BCryptPasswordEncoder passwordEncoder;

    public AdminBootstrapConfig(AdminDao adminDao, BCryptPasswordEncoder passwordEncoder) {
        this.adminDao = adminDao;
        this.passwordEncoder = passwordEncoder;
    }

    // Runs after Spring context is fully initialized
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void createInitialAdmin() {
        if (adminDao.findByUsername(adminUsername).isEmpty()) {
            Admin admin = new Admin();
            admin.setUsername(adminUsername);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            adminDao.save(admin);
            System.out.println("✅ Initial admin created: " + adminUsername);
        }
    }
}