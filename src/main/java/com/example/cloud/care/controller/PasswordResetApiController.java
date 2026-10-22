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
 * JSON password-reset flow for patients, replacing forgot_password.html,
 * otp_reset.html and reset_password.html.
 *
 * <p>These live under {@code /patient/api/**} rather than being new endpoints on
 * {@code AuthController} because the Thymeleaf versions return view names.
 * The underlying service calls are shared, not duplicated: sendPasswordResetCode,
 * verify and resetPassword are the exact methods the templates drove.
 *
 * <p>Unlike the legacy flow this accepts the OTP as a single 6-character code
 * rather than six separate digit fields, because that shape only existed to feed
 * the Thymeleaf six-box widget.
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

    /**
     * Step 1 — email a reset code.
     *
     * <p>Always reports success regardless of whether the address is registered,
     * so this cannot be used to enumerate which emails have accounts.
     */
    @PostMapping("/forgot")
    public Map<String, Object> forgot(@RequestBody Map<String, String> body) {
        String email = body == null ? null : trimToNull(body.get("email"));

        if (email == null) {
            return result(false, "Please provide a valid email address.");
        }

        try {
            userService.sendPasswordResetCode(email);
            logger.info("Password reset code sent to {}", email);
        } catch (Exception e) {
            // Deliberately swallowed. The legacy handler surfaced the raw
            // exception message, which told an anonymous visitor whether an
            // account existed. Failing silently keeps the response uniform.
            logger.warn("Password reset request failed for {}: {}", email, e.getMessage());
        }

        return result(true, "If that email is registered, a verification code is on its way.");
    }

    /** Step 2 — verify the emailed code. */
    @PostMapping("/verify")
    public Map<String, Object> verify(@RequestBody Map<String, String> body) {
        String email = body == null ? null : trimToNull(body.get("email"));
        String code = body == null ? null : trimToNull(body.get("code"));

        if (email == null) {
            return result(false, "Email information missing. Please try again.");
        }
        if (code == null || !code.matches("\\d{6}")) {
            return result(false, "Please enter the full 6-digit code.");
        }

        if (!userService.verify(email, code)) {
            return result(false, "Invalid or expired verification code!");
        }

        Map<String, Object> out = result(true, "Code verified.");
        out.put("email", email);
        return out;
    }

    /** Step 3 — set the new password. */
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
            return result(false, "Unable to reset password. The verification code may have expired.");
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