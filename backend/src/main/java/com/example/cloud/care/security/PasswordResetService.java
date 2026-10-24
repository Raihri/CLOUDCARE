package com.example.cloud.care.security;

import com.example.cloud.care.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Shared mechanics of the emailed-link password reset used by every role.
 *
 * <ul>
 *   <li>Tokens are 256 bits from {@link SecureRandom}. Only their SHA-256 hash
 *       is stored, so a database leak does not hand out working reset links.</li>
 *   <li>Links expire after {@link #TOKEN_TTL} and are single use (the caller
 *       clears the hash on success).</li>
 *   <li>The token only ever travels by email. It is never put in an HTTP
 *       response, so knowing someone's address is not enough to take over the
 *       account.</li>
 * </ul>
 */
@Service
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);

    public static final Duration TOKEN_TTL = Duration.ofMinutes(30);
    /** A new link is not issued more than once per minute per account. */
    public static final Duration RESEND_COOLDOWN = Duration.ofMinutes(1);
    public static final int MIN_PASSWORD_LENGTH = 8;

    /** Same text whether or not the account exists, so emails can't be enumerated. */
    public static final String GENERIC_REQUEST_MESSAGE =
            "If an account exists for that email, a reset link is on its way. It expires in 30 minutes.";

    private final SecureRandom random = new SecureRandom();
    private final EmailService emailService;
    private final String frontendUrl;

    public PasswordResetService(EmailService emailService,
                                @Value("${cloudcare.frontend-url:http://localhost:3000}") String frontendUrl) {
        this.emailService = emailService;
        this.frontendUrl = frontendUrl.replaceAll("/+$", "");
    }

    public record IssuedToken(String raw, String hash, Instant expiresAt) {}

    public IssuedToken issue() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        return new IssuedToken(raw, hash(raw), Instant.now().plus(TOKEN_TTL));
    }

    public static String hash(String rawToken) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(sha.digest(rawToken.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    /** True when a link was issued less than {@link #RESEND_COOLDOWN} ago. */
    public boolean issuedRecently(Instant currentExpiry) {
        Instant fullTtl = Instant.now().plus(TOKEN_TTL);
        return currentExpiry != null
                && currentExpiry.isAfter(fullTtl.minus(RESEND_COOLDOWN))
                && !currentExpiry.isAfter(fullTtl);
    }

    public static boolean isUsable(Instant expiry) {
        return expiry != null && expiry.isAfter(Instant.now());
    }

    /** @return an error message, or {@code null} when the password is acceptable. */
    public static String validateNewPassword(String password, String confirm) {
        if (password == null || password.isBlank()) return "Please choose a new password.";
        if (password.length() < MIN_PASSWORD_LENGTH)
            return "Password must be at least " + MIN_PASSWORD_LENGTH + " characters.";
        if (confirm != null && !password.equals(confirm)) return "Passwords do not match.";
        return null;
    }

    /**
     * Email the reset link. {@code resetPath} is the frontend route that reads
     * {@code ?token=}, e.g. {@code /reset-password}.
     */
    public void sendLink(String email, String name, String resetPath, String rawToken) {
        String link = frontendUrl + resetPath + "?token=" + URLEncoder.encode(rawToken, StandardCharsets.UTF_8);

        if (!emailService.isConfigured()) {
            // Local development only: with no mail provider there is no other way
            // to receive the link. It goes to the server console, never to the client.
            log.warn("Email is not configured. Password reset link for {} (valid {} min): {}",
                    email, TOKEN_TTL.toMinutes(), link);
            return;
        }

        String body = """
                Hello %s,

                We received a request to reset your CloudCare password.
                Use the link below within %d minutes:

                %s

                If you didn't ask for this, you can ignore this email; your password stays the same.

                CloudCare
                """.formatted(name == null || name.isBlank() ? "there" : name, TOKEN_TTL.toMinutes(), link);
        emailService.sendEmail(email, "Reset your CloudCare password", body);
    }
}
