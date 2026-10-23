package com.example.cloud.care.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;



import org.springframework.scheduling.annotation.Async;

import java.util.regex.Pattern;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.NamingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    @Value("${brevo.api.key}")
    private String apiKey;

    @Value("${brevo.sender.email}")
    private String senderEmail;

    @Value("${brevo.sender.name}")
    private String senderName;

    private static final String BREVO_URL =
            "https://api.brevo.com/v3/smtp/email";

    private final RestTemplate restTemplate = new RestTemplate();

    // Email regex pattern for validation
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    

    /**
     * Validates if the email address is in a valid format.
     * 
     * @param email the email address to validate
     * @return true if email is valid, false otherwise
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        String trimmed = email.trim();
        // Basic regex check
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            return false;
        }

        // Domain check (DNS MX or A record)
        String domain = trimmed.substring(trimmed.indexOf('@') + 1);
        if (!isDomainValid(domain)) {
            logger.warn("Email domain appears invalid or has no MX/A DNS records: {}", domain);
            return false;
        }

        return true;
    }

    /**
     * Checks whether the domain has MX or A DNS records.
     * Falls back to checking A records if MX records are not present.
     */
    public boolean isDomainValid(String domain) {
        if (domain == null || domain.isEmpty()) {
            return false;
        }

        int maxAttempts = 3;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                Hashtable<String, String> env = new Hashtable<>();
                env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
                DirContext dirContext = new InitialDirContext(env);
                Attributes attrs = dirContext.getAttributes(domain, new String[] { "MX", "A" });
                if (attrs == null)
                    return false;
                if (attrs.get("MX") != null && attrs.get("MX").size() > 0)
                    return true;
                if (attrs.get("A") != null && attrs.get("A").size() > 0)
                    return true;
                return false;
            } catch (NamingException e) {
                String msg = e.getMessage() == null ? "" : e.getMessage();
                logger.warn("DNS lookup failed for domain {} (attempt {}): {}", domain, attempt, msg);

                // If it's a SERVFAIL (response code 2), consider retrying
                if (msg.contains("response code 2") && attempt < maxAttempts) {
                    try {
                        Thread.sleep(500L);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                    continue;
                }

                // Non-recoverable error or final attempt — break out and fallback
                break;
            }
        }

        // Final fallback: try a simple A-record resolution via InetAddress
        try {
            java.net.InetAddress.getByName(domain);
            return true;
        } catch (Exception ex) {
            logger.warn("InetAddress fallback failed for domain {}: {}", domain, ex.getMessage());
            return false;
        }
    }

}
