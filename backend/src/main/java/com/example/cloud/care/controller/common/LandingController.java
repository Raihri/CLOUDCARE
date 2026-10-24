package com.example.cloud.care.controller.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Backend root. Nobody browses to {@code :8080} as a user — the Next.js app
 * is the real UI — but a plain health response here is more useful than a
 * 404 for anyone curling the API server directly, and doubles as a quick way
 * to confirm {@code cloudcare.frontend-url} is actually set correctly in a
 * given deploy.
 */
@RestController
public class LandingController {

    private final String frontendUrl;

    public LandingController(@Value("${cloudcare.frontend-url}") String frontendUrl) {
        this.frontendUrl = frontendUrl;
    }

    @GetMapping("/")
    public Map<String, Object> status() {
        return Map.of(
                "service", "cloudcare-api",
                "status", "ok",
                "frontend", frontendUrl);
    }
}
