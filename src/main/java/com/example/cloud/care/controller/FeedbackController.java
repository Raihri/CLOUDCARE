package com.example.cloud.care.controller;

import com.example.cloud.care.model.Feedback;
import com.example.cloud.care.service.FeedbackService;
import com.example.cloud.care.service.loggedInUserFind;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/patient/feedbacks")
public class FeedbackController {

    @Autowired
    loggedInUserFind logger;

    private final FeedbackService service;

    public FeedbackController(FeedbackService service) {
        this.service = service;
    }

    // Feedback Dashboard
    @GetMapping
    public String feedbackDashboard(Model model) {
        model.addAttribute("feedbacks", service.findAll());
        model.addAttribute("patient", logger.logger());
        return "feedback_dashboard";
    }

    // Open Feedback Form
    @GetMapping("/new")
    public String feedbackForm(Model model) {
        model.addAttribute("feedback", new Feedback());
        model.addAttribute("patient", logger.logger());

        return "feedback_form";
    }

    // Save Feedback
    @PostMapping("/save")
    public String saveFeedback(@ModelAttribute Feedback feedback,Model model) {
        service.save(feedback);
        model.addAttribute("patient", logger.logger());

        return "redirect:/patient/feedbacks";
    }
}