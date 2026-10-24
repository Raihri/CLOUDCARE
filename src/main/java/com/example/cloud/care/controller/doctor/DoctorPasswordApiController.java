package com.example.cloud.care.controller.doctor;

import com.example.cloud.care.model.Doctor;
import com.example.cloud.care.repository.DoctorRepository;
import com.example.cloud.care.security.PasswordResetService;
import com.example.cloud.care.security.PasswordResetService.IssuedToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Doctor password reset by emailed link. Same contract as the patient flow
 * ({@code /forgot}, {@code GET /reset}, {@code POST /reset}); the token is only
 * ever delivered by email and stored as a SHA-256 hash.
 */
@RestController
@RequestMapping("/doctor/api/password")
public class DoctorPasswordApiController {

    private static final Logger log = LoggerFactory.getLogger(DoctorPasswordApiController.class);
    private static final String INVALID_LINK = "This reset link is invalid or has expired. Please request a new one.";

    private final DoctorRepository doctorRepository;
    private final PasswordResetService resets;
    private final BCryptPasswordEncoder passwordEncoder;

    public DoctorPasswordApiController(DoctorRepository doctorRepository,
                                       PasswordResetService resets,
                                       BCryptPasswordEncoder passwordEncoder) {
        this.doctorRepository = doctorRepository;
        this.resets = resets;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/forgot")
    @Transactional
    public Map<String, Object> forgot(@RequestBody(required = false) Map<String, String> body) {
        String email = trimToNull(body == null ? null : body.get("email"));
        if (email == null) {
            return result(false, "Please provide a valid email address.");
        }

        Optional<Doctor> found = doctorRepository.findByEmail(email);
        if (found.isEmpty()) {
            log.info("Password reset requested for an unknown doctor email");
        } else if (!resets.issuedRecently(toInstant(found.get().getResetTokenExpiry()))) {
            Doctor doctor = found.get();
            IssuedToken token = resets.issue();
            doctor.setResetToken(token.hash());
            doctor.setResetTokenExpiry(Date.from(token.expiresAt()));
            doctorRepository.save(doctor);
            resets.sendLink(doctor.getEmail(), "Dr. " + doctor.getName(), "/doctor/reset-password", token.raw());
        }
        return result(true, PasswordResetService.GENERIC_REQUEST_MESSAGE);
    }

    @GetMapping("/reset")
    public Map<String, Object> check(@RequestParam(required = false) String token) {
        boolean valid = resolve(token).isPresent();
        return result(valid, valid ? "Reset link is valid." : INVALID_LINK);
    }

    @PostMapping("/reset")
    @Transactional
    public Map<String, Object> reset(@RequestBody(required = false) Map<String, String> body) {
        String token = trimToNull(body == null ? null : body.get("token"));
        String password = body == null ? null : body.get("password");
        String confirm = body == null ? null : body.get("confirmPassword");

        String problem = PasswordResetService.validateNewPassword(password, confirm);
        if (problem != null) {
            return result(false, problem);
        }

        Optional<Doctor> found = resolve(token);
        if (found.isEmpty()) {
            return result(false, INVALID_LINK);
        }

        Doctor doctor = found.get();
        doctor.setPassword(passwordEncoder.encode(password));
        doctor.setResetToken(null);
        doctor.setResetTokenExpiry(null);
        doctorRepository.save(doctor);
        return result(true, "Your password has been changed. You can now sign in.");
    }

    private Optional<Doctor> resolve(String token) {
        if (token == null || token.isBlank()) return Optional.empty();
        return doctorRepository.findByResetToken(PasswordResetService.hash(token))
                .filter(d -> PasswordResetService.isUsable(toInstant(d.getResetTokenExpiry())));
    }

    private static Instant toInstant(Date date) {
        return date == null ? null : date.toInstant();
    }

    private static Map<String, Object> result(boolean ok, String message) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("success", ok);
        out.put("message", message);
        return out;
    }

    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
