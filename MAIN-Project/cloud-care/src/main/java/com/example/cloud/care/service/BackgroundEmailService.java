package com.example.cloud.care.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BackgroundEmailService {

    @Autowired
    private DonorEmailService emailService;

    public void sendHtmlEmailInBackground(String to, String subject, String html) {

        Thread thread = new Thread(() -> {

            try {
                System.out.println("THREAD STARTED — sending email to " + to);

                emailService.sendHtmlEmail(to, subject, html);

                System.out.println("THREAD DONE — email sent");

            } catch (Exception e) {
                System.err.println("THREAD ERROR: " + e.getMessage());
                e.printStackTrace();
            }

        });

        thread.setName("EmailSenderThread");
        thread.setDaemon(false);  
        thread.start();
    }
}
