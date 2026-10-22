package com.example.cloud.care.controller;

import com.example.cloud.care.dao.AdminDao;
import com.example.cloud.care.model.Admin;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * JSON password change for admins, replacing admin_change_password.html.
 *
 * <p>Unlike the other two roles this is not a "forgot password" flow — admins
 * are already authenticated, so it verifies the current password before setting
 * the new one. That mirrors AdminController's Thymeleaf handler exactly.
 */
@RestController
@RequestMapping("/admin/api/password")
public class AdminPasswordApiController {

    private final AdminDao adminDao;
    private final BCryptPasswordEncoder passwordEncoder;

    public AdminPasswordApiController(AdminDao adminDao, BCryptPasswordEncoder passwordEncoder) {
        this.adminDao = adminDao;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/change")
    public Map<String, Object> changePassword(@RequestBody Map<String, String> body, Principal principal) {
        String oldPassword = body == null ? null : body.get("oldPassword");
        String newPassword = body == null ? null : body.get("newPassword");
        String confirm = body == null ? null : body.get("confirmPassword");

        if (oldPassword == null || oldPassword.isEmpty()) {
            return result(false, "Please enter your current password.");
        }
        if (newPassword == null || newPassword.isBlank()) {
            return result(false, "Please choose a new password.");
        }
        if (newPassword.length() < 6) {
            return result(false, "New password must be at least 6 characters.");
        }
        if (!newPassword.equals(confirm)) {
            return result(false, "Passwords do not match!");
        }
        if (oldPassword.equals(newPassword)) {
            return result(false, "New password must be different from the current one.");
        }

        Admin admin = adminDao.findByUsername(principal.getName()).orElse(null);
        if (admin == null) {
            return result(false, "Admin not found.");
        }

        if (!passwordEncoder.matches(oldPassword, admin.getPassword())) {
            return result(false, "Old password is incorrect");
        }

        admin.setPassword(passwordEncoder.encode(newPassword));
        adminDao.save(admin);

        return result(true, "Password changed successfully");
    }

    private static Map<String, Object> result(boolean ok, String message) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("success", ok);
        out.put("message", message);
        return out;
    }
}