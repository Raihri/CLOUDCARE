package com.example.cloud.care.controller;

import com.example.cloud.care.model.notification;
import com.example.cloud.care.service.notificationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class socketController {
    @Autowired
    SimpMessagingTemplate simpMessagingTemplate;

    // @MessageMapping("/sendToUser")
    // public void sendToSpecificUser(notification message) {
    //     notificationService.sendToSpecific(message.get, new notification(message.getMessage()));
    // }
}
