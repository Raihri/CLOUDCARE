package com.example.cloud.care.controller;

import com.example.cloud.care.model.notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class socketController {
    @Autowired
    SimpMessagingTemplate simpMessagingTemplate;

    @MessageMapping("/application")   // message to -> app/application
    @SendTo("/all")
    public notification sendToAll(final notification notification) throws Exception
    {
        return notification;
    }
}
