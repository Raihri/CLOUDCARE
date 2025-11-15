package com.example.cloud.care.controller;

import com.example.cloud.care.service.NotificationController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class DoctorController {

    @Autowired
    private NotificationController notificationController;

    @GetMapping("/doctorS")
    public String doctorForm() {
        return "doctor";
    }

    @PostMapping("/doctorS/send")
    public String sendNotification(@RequestParam String patientId) {
        notificationController.sendNotification(patientId);
        return "doctor";
    }
}
