package com.example.cloud.care.security;

import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

/**
 * Short-lived, signed tickets that authenticate a WebSocket (STOMP) connection.
 *
 * <p>The browser cannot reuse its HTTP session for the socket in every
 * deployment (the socket may live on a different origin than the Next.js app),
 * so the signed-in user first asks {@code GET /api/ws-ticket}, which is
 * session-authenticated, and presents the ticket in the STOMP CONNECT frame.
 *
 * <p>Format: {@code base64url(email|expiryEpochSeconds).base64url(HMAC-SHA256)}.
 * The key is random per process: tickets are only needed for the few seconds
 * before CONNECT, and the client simply fetches a fresh one on reconnect.
 */
@Service
public class WebSocketTicketService {

    static final Duration TTL = Duration.ofMinutes(2);
    private static final String ALGORITHM = "HmacSHA256";

    private final byte[] key = new byte[32];

    public WebSocketTicketService() {
        new SecureRandom().nextBytes(key);
    }

    public String issue(String email) {
        long expiry = Instant.now().plus(TTL).getEpochSecond();
        String payload = b64(email + "|" + expiry);
        return payload + "." + sign(payload);
    }

    /** @return the email the ticket was issued to, if the ticket is genuine and unexpired. */
    public Optional<String> verify(String ticket) {
        if (ticket == null) return Optional.empty();
        int dot = ticket.indexOf('.');
        if (dot <= 0) return Optional.empty();

        String payload = ticket.substring(0, dot);
        byte[] given = ticket.substring(dot + 1).getBytes(StandardCharsets.US_ASCII);
        byte[] expected = sign(payload).getBytes(StandardCharsets.US_ASCII);
        if (!MessageDigest.isEqual(given, expected)) return Optional.empty();

        String decoded;
        try {
            decoded = new String(Base64.getUrlDecoder().decode(payload), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
        int bar = decoded.lastIndexOf('|');
        if (bar <= 0) return Optional.empty();
        try {
            long expiry = Long.parseLong(decoded.substring(bar + 1));
            if (Instant.now().getEpochSecond() > expiry) return Optional.empty();
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
        return Optional.of(decoded.substring(0, bar));
    }

    private String sign(String payload) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(key, ALGORITHM));
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Cannot sign WebSocket ticket", e);
        }
    }

    private static String b64(String s) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(s.getBytes(StandardCharsets.UTF_8));
    }
}
