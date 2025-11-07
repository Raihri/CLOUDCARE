package com.example.cloud.care.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;


import java.util.Random;

@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public String sendVerificationEmail(String to) {
        logger.info("Preparing verification email for: {}", to);
        if (to == null || to.isEmpty()) {
            System.out.println("Recipient email is null or empty.");
            logger.error("Recipient email is null or empty.");
            return null;
        }

        // Generate a 6-digit numeric OTP
        String code = String.format("%06d", new Random().nextInt(1000000));

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            String sender = ((org.springframework.mail.javamail.JavaMailSenderImpl) mailSender).getUsername();

            message.setFrom(sender);
            message.setTo(to);
            message.setSubject("CloudCare Email Verification Code");
            message.setText("Welcome to CloudCare!\n\n"
                    + "Your verification code is: " + code + "\n\n"
                    + "Please enter this code on the verification page to activate your account.\n\n"
                    + "If you didn’t sign up, you can safely ignore this email.\n\n"
                    + "Best regards,\nCloudCare Team");

            mailSender.send(message);
            logger.info("Verification code {} sent to {}", code, to);
            return code;
        } catch (MailException e) {
            logger.error("MailException sending to {}: {}", to, e.getMessage());
            System.out.println("Failed to send email: " + e.getMessage());
            return null;
        } catch (Exception e) {
            logger.error("Unexpected error sending to {}: {}", to, e.getMessage());
            System.out.println("Unexpected error: " + e.getMessage());
            return null;
        }
    }
}