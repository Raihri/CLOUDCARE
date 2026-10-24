package com.example.cloud.care.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.example.cloud.care.model.Notification;

@Service
public class NotificationService {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // notification to all
    public void sendToAll(Notification notification)
    {
        messagingTemplate.convertAndSend("/topic/all",notification);
    }

    // notification to specific
    public void sendToSpecific(String email, Notification notification) {
        if (email == null || email.isBlank()) {
            return;
        }
        try {
            messagingTemplate.convertAndSendToUser(email.trim(), "/queue/notifications", notification);
        } catch (Exception ignored) {
            // A failed live push must not break the database write that
            // triggered it; the notification is still visible via the API.
        }
    }
}
