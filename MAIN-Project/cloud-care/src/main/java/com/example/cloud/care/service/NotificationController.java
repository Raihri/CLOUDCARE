package com.example.cloud.care.service;

import com.example.cloud.care.model.NotificationMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;

@Service
public class NotificationController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // doctor sends notification
    public void sendNotification(String patientId) {
        System.out.println("[NotificationController] sending message to /topic/patient/" + patientId);
        messagingTemplate.convertAndSend("/topic/patient/" + patientId,
                new NotificationMessage("Doctor sent an update for patient ID: " + patientId));
    }
}
