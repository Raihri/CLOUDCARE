package com.example.cloud.care.controller.patient;

import com.example.cloud.care.model.User;
import com.example.cloud.care.repository.UserRepository;
import com.example.cloud.care.security.PasswordResetService;
import com.example.cloud.care.security.PasswordResetService.IssuedToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Patient password reset by emailed link.
 *
 * <ol>
 *   <li>{@code POST /forgot {email}} always answers with the same message. If
 *       the account exists, a single-use link valid for 30 minutes is emailed.</li>
 *   <li>{@code GET /reset?token=} lets the reset page show "link expired" early.</li>
 *   <li>{@code POST /reset {token, password, confirmPassword}} sets the new
 *       password and burns the token. It does not sign the user in.</li>
 * </ol>
 * Token handling is shared with the doctor flow in {@link PasswordResetService}.
 */
@RestController
@RequestMapping("/patient/api/password")
public class PatientPasswordApiController {

    private static final Logger log = LoggerFactory.getLogger(PatientPasswordApiController.class);
    private static final String INVALID_LINK = "This reset link is invalid or has expired. Please request a new one.";

    private final UserRepository userRepository;
    private final PasswordResetService resets;
    private final BCryptPasswordEncoder passwordEncoder;

    public PatientPasswordApiController(UserRepository userRepository,
                                        PasswordResetService resets,
                                        BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
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

        Optional<User> found = userRepository.findByEmail(email);
        if (found.isEmpty()) {
            log.info("Password reset requested for an unknown patient email");
        } else if (!resets.issuedRecently(found.get().getResetTokenExpiry())) {
            User user = found.get();
            IssuedToken token = resets.issue();
            user.setResetTokenHash(token.hash());
            user.setResetTokenExpiry(token.expiresAt());
            userRepository.save(user);
            resets.sendLink(user.getEmail(), user.getName(), "/reset-password", token.raw());
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

        Optional<User> found = resolve(token);
        if (found.isEmpty()) {
            return result(false, INVALID_LINK);
        }

        User user = found.get();
        user.setPassword(passwordEncoder.encode(password));
        user.setResetTokenHash(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
        return result(true, "Your password has been changed. You can now sign in.");
    }

    private Optional<User> resolve(String token) {
        if (token == null || token.isBlank()) return Optional.empty();
        return userRepository.findByResetTokenHash(PasswordResetService.hash(token))
                .filter(u -> PasswordResetService.isUsable(u.getResetTokenExpiry()));
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
