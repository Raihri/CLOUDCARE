package com.example.cloud.care.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmailServiceDonor {

    @Value("${brevo.api.key}")
    private String apiKey;

    @Value("${brevo.sender.email}")
    private String senderEmail;

    @Value("${brevo.sender.name}")
    private String senderName;

    private static final String BREVO_URL = "https://api.brevo.com/v3/smtp/email";

    private final RestTemplate restTemplate = new RestTemplate();

    @Async
    public void sendHtmlEmailAsync(String to, String subject, String html) {
        sendHtmlEmail(to, subject, html);
    }

    @Async("taskExecutor")
    public void sendHtmlEmail(String to, String subject, String html) {
        try {
            System.out.println("ASYNC EMAIL START → " + to);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", apiKey);

            Map<String, Object> payload = new HashMap<>();
            payload.put("sender", Map.of("email", senderEmail, "name", senderName));
            payload.put("to", List.of(Map.of("email", to)));
            payload.put("subject", subject);
            payload.put("htmlContent", html); // HTML content

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(BREVO_URL, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("ASYNC EMAIL SENT → " + to);
            } else {
                System.err.println("Brevo error: " + response.getStatusCode() + " - " + response.getBody());
            }

        } catch (Exception e) {
            System.err.println("EMAIL SEND FAILED → " + e.getMessage());
            e.printStackTrace();
        }
    }
}