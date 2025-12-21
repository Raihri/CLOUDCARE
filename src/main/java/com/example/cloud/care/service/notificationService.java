package com.example.cloud.care.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.example.cloud.care.model.notification;

@Service
public class notificationService {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // notification to all
    public void sendToAll(notification notification)
    {
        messagingTemplate.convertAndSend("/topic/all",notification);
    }

    // notification to specific
    public void sendToSpecific(String email,notification notification)
    {

        try
        {
            messagingTemplate.convertAndSendToUser(email, "/queue/notifications",notification);
            System.out.println("Raima-------------------DONE");
        }

        catch(Exception e)
        {
            System.out.println("Raima we failed-------------------TTTTTTT");
        }




    }
    
}
