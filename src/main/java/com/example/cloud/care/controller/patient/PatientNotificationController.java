package com.example.cloud.care.controller.patient;

import com.example.cloud.care.model.Notification;
import com.example.cloud.care.service.CurrentPatientService;
import com.example.cloud.care.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/patient")
public class PatientNotificationController {

    @Autowired
    PatientService patientService;

    @Autowired
    CurrentPatientService logger;

    @GetMapping("/notifications")
    public String notifcations1(Model model) {
        try {
            List<Notification> notifications = logger.currentPatient().getAllNotification();

            model.addAttribute("notifications", notifications);
            model.addAttribute("patient",logger.currentPatient());


            return "notification"; // This will load notification.html
        } catch (Exception e) {
            model.addAttribute("error", "Patient not found");
            return "error_page";
        }
    }


    @PostMapping ("/notifications")
    public String notifcations2(Model model) {
        try {
            List<Notification> notifications = logger.currentPatient().getAllNotification();

            model.addAttribute("notifications", notifications);
            model.addAttribute("patient",logger.currentPatient());


            return "notification"; // This will load notification.html
        } catch (Exception e) {
            model.addAttribute("error", "Patient not found");
            return "error_page";
        }
    }



}
