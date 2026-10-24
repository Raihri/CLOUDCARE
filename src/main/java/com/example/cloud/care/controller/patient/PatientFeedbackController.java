package com.example.cloud.care.controller.patient;

import com.example.cloud.care.model.Feedback;
import com.example.cloud.care.service.FeedbackService;
import com.example.cloud.care.service.CurrentPatientService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/patient/feedbacks")
public class PatientFeedbackController {

    @Autowired
    CurrentPatientService logger;

    private final FeedbackService service;

    public PatientFeedbackController(FeedbackService service) {
        this.service = service;
    }

    // Feedback Dashboard
    @GetMapping
    public String feedbackDashboard(Model model) {
        model.addAttribute("feedbacks", service.findAll());
        model.addAttribute("patient", logger.currentPatient());
        return "feedback_dashboard";
    }

    // Open Feedback Form
    @GetMapping("/new")
    public String feedbackForm(Model model) {
        model.addAttribute("feedback", new Feedback());
        model.addAttribute("patient", logger.currentPatient());

        return "feedback_form";
    }

    // Save Feedback
    @PostMapping("/save")
    public String saveFeedback(@ModelAttribute Feedback feedback,Model model) {
        service.save(feedback);
        model.addAttribute("patient", logger.currentPatient());

        return "redirect:/patient/feedbacks";
    }
}