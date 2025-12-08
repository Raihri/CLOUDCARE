package com.example.cloud.care.controller;

import com.example.cloud.care.model.notification;
import com.example.cloud.care.service.loggedInUserFind;
import com.example.cloud.care.service.patient_service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/patient")
public class notificationController {

    @Autowired
    patient_service patientService;

    @Autowired
    loggedInUserFind logger;

    @GetMapping("/notifications")
    public String notifcations1(Model model) {
        try {
            List<notification> notifications = logger.logger().getAllNotification();

            model.addAttribute("notifications", notifications);
            model.addAttribute("patient",logger.logger());


            return "notification"; // This will load notification.html
        } catch (Exception e) {
            model.addAttribute("error", "Patient not found");
            return "error";
        }
    }


    @PostMapping ("/notifications")
    public String notifcations2(Model model) {
        try {
            List<notification> notifications = logger.logger().getAllNotification();

            model.addAttribute("notifications", notifications);
            model.addAttribute("patient",logger.logger());


            return "notification"; // This will load notification.html
        } catch (Exception e) {
            model.addAttribute("error", "Patient not found");
            return "error";
        }
    }



}
