package com.example.cloud.care.config;

import com.example.cloud.care.repository.AdminRepository;
import com.example.cloud.care.model.Admin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Configuration
public class AdminBootstrapConfig {

    @Value("${ADMIN_EMAIL}")
    private String adminEmail;

    @Value("${ADMIN_PASSWORD}")
    private String adminPassword;

    private final AdminRepository adminDao;
    private final BCryptPasswordEncoder passwordEncoder;

    public AdminBootstrapConfig(AdminRepository adminDao, BCryptPasswordEncoder passwordEncoder) {
        this.adminDao = adminDao;
        this.passwordEncoder = passwordEncoder;
    }

    // Runs after Spring context is fully initialized
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void createInitialAdmin() {
        if (adminDao.findByEmail(adminEmail).isEmpty()) {
            Admin admin = new Admin();
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            adminDao.save(admin);
            System.out.println("✅ Initial admin created: " + adminEmail);
        }
    }
}