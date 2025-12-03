package com.example.cloud.care.controller;

import com.example.cloud.care.dao.AdminDao;
import com.example.cloud.care.model.Admin;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AdminDao adminDao;
    private final BCryptPasswordEncoder passwordEncoder;

    public AdminController(AdminDao adminDao, BCryptPasswordEncoder passwordEncoder) {
        this.adminDao = adminDao;
        this.passwordEncoder = passwordEncoder;
    }
    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String logout,
                            org.springframework.ui.Model model) {
        if (logout != null) {
            model.addAttribute("message", "Logged out successfully");
        }
        return "admin_login"; // your Thymeleaf login page
    }

    @GetMapping("/change-password")
    public String showChangePassword() {
        return "admin_change_password"; // Thymeleaf page
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam String oldPassword,
                                 @RequestParam String newPassword,
                                 Principal principal,
                                 org.springframework.ui.Model model) {
        Admin admin = adminDao.findByUsername(principal.getName())
        .orElseThrow(() -> new RuntimeException("Admin not found"));
        if (!passwordEncoder.matches(oldPassword, admin.getPassword())) {
            model.addAttribute("error", "Old password is incorrect");
            return "admin_change_password";
        }

        admin.setPassword(passwordEncoder.encode(newPassword));
        adminDao.save(admin);
        model.addAttribute("success", "Password changed successfully");
        return "admin_pending_doctors";
    }
}
