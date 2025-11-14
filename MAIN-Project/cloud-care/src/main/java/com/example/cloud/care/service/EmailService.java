package com.example.cloud.care.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;

import java.util.regex.Pattern;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.NamingException;
import java.util.Hashtable;

@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;
    
    // Email regex pattern for validation
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    
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
    private boolean isDomainValid(String domain) {
        if (domain == null || domain.isEmpty()) {
            return false;
        }
        try {
            Hashtable<String, String> env = new Hashtable<>();
            env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
            DirContext dirContext = new InitialDirContext(env);
            Attributes attrs = dirContext.getAttributes(domain, new String[]{"MX", "A"});
            if (attrs == null) return false;
            if (attrs.get("MX") != null && attrs.get("MX").size() > 0) return true;
            if (attrs.get("A") != null && attrs.get("A").size() > 0) return true;
            return false;
        } catch (NamingException e) {
            logger.warn("DNS lookup failed for domain {}: {}", domain, e.getMessage());
            return false;
        }
    }

    /**
     * Send a verification email asynchronously with a generated code.
     * Validates email format before sending.
     * Returns immediately; email sent in background thread.
     */
    // NOTE: The single-argument convenience method was removed to avoid
    // accidental mismatch between generated codes and persisted codes.
    // Prefer calling sendVerificationEmail(to, code) after generating and
    // persisting the verification code in the service layer.

    /**
     * Send a verification email asynchronously using the provided code.
     * Validates email format before sending.
     * Ensures the code saved in the database matches the code delivered to the user.
     * Returns immediately; email sent in background thread.
     */
    @Async
    public void sendVerificationEmail(String to, String code) {
        logger.info("Preparing verification email for: {} (using provided code)", to);
        if (to == null || to.isEmpty() || code == null || code.isEmpty()) {
            String warningMsg = "⚠️ WARNING: Recipient email or code is null/empty. Email: " + to + ", Code present: " + (code != null && !code.isEmpty());
            System.out.println(warningMsg);
            logger.warn(warningMsg);
            return;
        }
        
        // Validate email format before sending
        if (!isValidEmail(to)) {
            String warningMsg = "⚠️ WARNING: Invalid email format provided: " + to;
            System.out.println(warningMsg);
            logger.warn(warningMsg);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            String sender = ((org.springframework.mail.javamail.JavaMailSenderImpl) mailSender).getUsername();

            message.setFrom(sender);
            message.setTo(to);
            message.setSubject("CloudCare Email Verification Code");
            message.setText("Welcome to CloudCare!\n\n"
                    + "Your verification code is: " + code + "\n\n"
                    + "Please enter this code on the verification page to activate your account.\n\n"
                    + "If you didn't sign up, you can safely ignore this email.\n\n"
                    + "Best regards,\nCloudCare Team");

            mailSender.send(message);
            logger.info("Verification code {} sent to {}", code, to);
        } catch (MailException e) {
            logger.error("MailException sending to {}: {}", to, e.getMessage());
            System.out.println("Failed to send email: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error sending to {}: {}", to, e.getMessage());
            System.out.println("Unexpected error: " + e.getMessage());
        }
    }
}
