package com.example.cloud.care.controller.common;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Backend root. Nobody browses to {@code :8080} as a user — the Next.js app
 * at {@code :3000} is the real UI — but a plain health response here is more
 * useful than a 404 for anyone curling the API server directly.
 */
@RestController
public class LandingController {

    @GetMapping("/")
    public Map<String, Object> status() {
        return Map.of(
                "service", "cloudcare-api",
                "status", "ok",
                "frontend", "http://localhost:3000");
    }
}
