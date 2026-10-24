package com.example.cloud.care.security;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordResetServiceTest {

    private final PasswordResetService resets = new PasswordResetService(null, "http://localhost:3000/");

    @Test
    void issuedTokenIsRandomAndOnlyItsHashIsKept() {
        var a = resets.issue();
        var b = resets.issue();
        assertThat(a.raw()).isNotEqualTo(b.raw());
        assertThat(a.raw()).hasSizeGreaterThanOrEqualTo(43); // 256 bits, base64url
        assertThat(a.hash()).isEqualTo(PasswordResetService.hash(a.raw())).hasSize(64).isNotEqualTo(a.raw());
        assertThat(a.expiresAt()).isAfter(Instant.now().plus(PasswordResetService.TOKEN_TTL).minusSeconds(5));
    }

    @Test
    void expiryAndCooldown() {
        assertThat(PasswordResetService.isUsable(null)).isFalse();
        assertThat(PasswordResetService.isUsable(Instant.now().minusSeconds(1))).isFalse();
        assertThat(PasswordResetService.isUsable(Instant.now().plusSeconds(60))).isTrue();

        assertThat(resets.issuedRecently(resets.issue().expiresAt())).isTrue();
        assertThat(resets.issuedRecently(Instant.now().plusSeconds(60))).isFalse();
        assertThat(resets.issuedRecently(null)).isFalse();
    }

    @Test
    void passwordRules() {
        assertThat(PasswordResetService.validateNewPassword("", "")).isNotNull();
        assertThat(PasswordResetService.validateNewPassword("short", "short")).contains("8");
        assertThat(PasswordResetService.validateNewPassword("longenough", "different")).contains("match");
        assertThat(PasswordResetService.validateNewPassword("longenough", "longenough")).isNull();
    }
}
