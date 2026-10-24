package com.example.cloud.care.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The browser origins CloudCare is served from (the Next.js app, plus the
 * backend itself for the legacy server-rendered pages). Used by the origin
 * check, CORS and the WebSocket handshake so the list lives in one place.
 */
@Component
public class AllowedOrigins {

    private final Set<String> origins;

    public AllowedOrigins(@Value("${cloudcare.allowed-origins}") String csv) {
        this.origins = Arrays.stream(csv.split(","))
                .map(String::trim)
                .map(s -> s.replaceAll("/+$", ""))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
    }

    public Set<String> asSet() {
        return origins;
    }

    public String[] asArray() {
        return origins.toArray(String[]::new);
    }
}
