package com.example.cloud.care.controller.patient;

import com.example.cloud.care.model.Feedback;
import com.example.cloud.care.service.FeedbackService;

import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/patient/feedbacks")
public class PatientFeedbackController {

    private final FeedbackService service;

    public PatientFeedbackController(FeedbackService service) {
        this.service = service;
    }

    @PostMapping("/save")
    public Map<String, Object> saveFeedback(@ModelAttribute Feedback feedback) {
        service.save(feedback);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("success", true);
        out.put("message", "Thanks for the feedback!");
        return out;
    }
}
