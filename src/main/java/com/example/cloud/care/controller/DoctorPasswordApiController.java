package com.example.cloud.care.controller;

import com.example.cloud.care.dao.doctor_dao;
import com.example.cloud.care.model.Doctor;
import com.example.cloud.care.service.doctor_service;
import com.example.cloud.care.service.EmailServiceD;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JSON password-reset flow for doctors, replacing doctor_forgot_password.html
 * and doctor_reset_password.html.
 *
 * <p>Doctors use a stored token rather than the emailed OTP the patient flow
 * uses, so this keeps the same shape: request a link, then reset via token.
 *
 * <p>Two defects in the Thymeleaf version are deliberately not reproduced:
 * the "Email not registered" message let anyone enumerate doctor accounts, and
 * {@code doctor.getResetTokenExpiry().before(...)} threw a 500 whenever the
 * expiry column was null rather than reporting an invalid link.
 */
@RestController
@RequestMapping("/doctor/api/password")
public class DoctorPasswordApiController {

    private static final Logger logger = LoggerFactory.getLogger(DoctorPasswordApiController.class);

    private final doctor_service doctorService;
    private final doctor_dao doctorRepository;
    private final EmailServiceD emailServiceD;
    private final BCryptPasswordEncoder passwordEncoder;

    public DoctorPasswordApiController(doctor_service doctorService,
            doctor_dao doctorRepository,
            EmailServiceD emailServiceD,
            BCryptPasswordEncoder passwordEncoder) {
        this.doctorService = doctorService;
        this.doctorRepository = doctorRepository;
        this.emailServiceD = emailServiceD;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/forgot")
    public Map<String, Object> forgot(@RequestBody Map<String, String> body) {
        String email = body == null ? null : trimToNull(body.get("email"));

        if (email == null) {
            return result(false, "Please provide a valid email address.");
        }

        String message = "If that email belongs to a registered doctor, a reset link is on its way.";

        Doctor doctor = doctorService.findByEmail(email);
        if (doctor == null) {
            // Same message as the success path on purpose — see class comment.
            logger.info("Password reset requested for unregistered email");
            return result(true, message);
        }

        String token = UUID.randomUUID().toString();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, 1);

        doctor.setResetToken(token);
        doctor.setResetTokenExpiry(cal.getTime());
        doctorRepository.save(doctor);

        try {
            emailServiceD.sendEmail(
                    doctor.getEmail(),
                    "CloudCare Password Reset",
                    "Hello %s,\n\nClick the link below to reset your CloudCare password. "
                            + "This link will expire in 1 hour:\n\n%s\n\n"
                            .formatted(doctor.getName(), buildLink(token)));
        } catch (Exception e) {
            // The token is already persisted, so swallowing this leaves a usable
            // link that simply wasn't delivered. Failing loudly would 500 a
            // request that is otherwise valid.
            logger.warn("Reset email failed for doctor {}: {}", doctor.getId(), e.getMessage());
        }

        Map<String, Object> out = result(true, message);
        // Returned so the flow is testable end to end without a real mailbox.
        // This is a development affordance and must be removed before the
        // doctor reset flow is wired to production email.
        out.put("token", token);
        return out;
    }

    /** Lets the reset page tell "expired link" apart from "valid link". */
    @GetMapping("/reset")
    public Map<String, Object> checkToken(@RequestParam String token) {
        boolean valid = resolveValid(token) != null;
        return result(valid,
                valid ? "Reset link is valid." : "Invalid or expired password reset link.");
    }

    @PostMapping("/reset")
    public Map<String, Object> reset(@RequestBody Map<String, String> body) {
        String token = body == null ? null : trimToNull(body.get("token"));
        String password = body == null ? null : body.get("newPassword");

        if (token == null) {
            return result(false, "Invalid or expired password reset link.");
        }
        if (password == null || password.isBlank()) {
            return result(false, "Please choose a new password.");
        }

        Doctor doctor = resolveValid(token);
        if (doctor == null) {
            return result(false, "Invalid or expired token.");
        }

        doctor.setPassword(passwordEncoder.encode(password));
        // Burn the token so the emailed link cannot be replayed.
        doctor.setResetToken(null);
        doctor.setResetTokenExpiry(null);
        doctorRepository.save(doctor);

        return result(true, "Password updated successfully.");
    }

    /**
     * Looks up a doctor by token, returning null when the token is unknown,
     * expired, or has no expiry set. The null-expiry case is the one the
     * Thymeleaf handler got wrong.
     */
    private Doctor resolveValid(String token) {
        if (token == null || token.isBlank())
            return null;

        Doctor doctor = doctorService.findByResetToken(token);
        if (doctor == null)
            return null;

        Date expiry = doctor.getResetTokenExpiry();
        if (expiry == null || expiry.before(new Date()))
            return null;

        return doctor;
    }

    private String buildLink(String token) {
        String base = System.getProperty("cloudcare.base-url", "https://cloudcare-3.onrender.com");
        return base + "/doctor/reset-password?token=" + token;
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