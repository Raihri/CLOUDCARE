// EmailService.java
package com.example.cloud.care.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class DonorEmailService {

    @Autowired
    private JavaMailSender mailSender;
    @Async
public void sendHtmlEmailAsync(String to, String subject, String html) {
    sendHtmlEmail(to, subject, html);
}


    @Async("taskExecutor")   // runs in async thread pool
    public void sendHtmlEmail(String to, String subject, String html) {

        try {
            System.out.println("ASYNC EMAIL START → " + to);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);

            mailSender.send(message);

            System.out.println("ASYNC EMAIL SENT → " + to);

        } catch (Exception e) {
            System.err.println("EMAIL SEND FAILED → " + e.getMessage());
            e.printStackTrace();
        }
    }
}
