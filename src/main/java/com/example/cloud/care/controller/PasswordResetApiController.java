package com.example.cloud.care.controller;

import com.example.cloud.care.service.UserService;
import com.example.cloud.care.service.CustomUserDetailsService;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JSON password-reset endpoint for patients.
 *
 * <p>Demo mode: the old email OTP steps have been removed. The Next.js form
 * sends the email and new password directly; {@code resetPassword} validates
 * the account and stores a fresh BCrypt hash. This keeps the recruiter demo
 * self-contained and independent of an email provider.
 */
@RestController
@RequestMapping("/patient/api/password")
public class PasswordResetApiController {

    private static final Logger logger = LoggerFactory.getLogger(PasswordResetApiController.class);

    private final UserService userService;
    private final CustomUserDetailsService customUserDetailsService;

    public PasswordResetApiController(UserService userService,
            CustomUserDetailsService customUserDetailsService) {
        this.userService = userService;
        this.customUserDetailsService = customUserDetailsService;
    }

    /** Set a new password directly (no OTP in demo mode). */
    @PostMapping("/reset")
    public Map<String, Object> reset(@RequestBody Map<String, String> body) {
        String email = body == null ? null : trimToNull(body.get("email"));
        String password = body == null ? null : body.get("password");
        String confirm = body == null ? null : body.get("confirmPassword");

        if (email == null) {
            return result(false, "Email information missing. Please try again.");
        }
        if (password == null || password.isBlank()) {
            return result(false, "Please choose a new password.");
        }
        if (!password.equals(confirm)) {
            return result(false, "Passwords do not match!");
        }

        try {
            userService.resetPassword(email, password);
        } catch (Exception e) {
            logger.warn("Password reset failed for {}: {}", email, e.getMessage());
            return result(false, "Unable to reset password. Check the email address and try again.");
        }

        // Sign the user in, matching the legacy behaviour where a successful
        // reset dropped you straight onto the dashboard.
        try {
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
        } catch (Exception e) {
            // Not fatal: the password is already changed. They can just log in.
            logger.warn("Could not auto-authenticate after reset for {}: {}", email, e.getMessage());
        }

        return result(true, "Password successfully reset.");
    }

    private static Map<String, Object> result(boolean ok, String message) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("success", ok);
        out.put("message", message);
        return out;
    }

    private static String trimToNull(String s) {
        if (s == null)
            return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}