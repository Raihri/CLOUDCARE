package com.example.cloud.care.controller;

import com.example.cloud.care.model.Feedback;
import com.example.cloud.care.service.FeedbackService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin/feedbacks")
public class AdminFeedbackController {

    private final FeedbackService service;

    public AdminFeedbackController(FeedbackService service) {
        this.service = service;
    }

    // View all feedbacks with optional filtering
    @GetMapping
    public String adminFeedbackList(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            Model model) {

        List<Feedback> feedbacks = service.findFiltered(name, email, category, rating, fromDate, toDate);
        model.addAttribute("feedbacks", feedbacks);

        // Preserve filter values for form retention
        model.addAttribute("name", name);
        model.addAttribute("email", email);
        model.addAttribute("category", category);
        model.addAttribute("rating", rating);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);

        return "admin_feedback_list";
    }

    // Delete a feedback
    @PostMapping("/delete/{id}")
    public String deleteFeedback(@PathVariable Long id) {
        service.deleteById(id);
        return "redirect:/admin/feedbacks";
    }
}