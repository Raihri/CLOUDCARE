package com.example.cloud.care.controller.admin;

import com.example.cloud.care.model.Feedback;
import com.example.cloud.care.service.FeedbackService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/feedbacks")
public class AdminFeedbackController {

    private final FeedbackService service;

    public AdminFeedbackController(FeedbackService service) {
        this.service = service;
    }

    /** JSON variant of {@code GET /admin/feedbacks}, same filter parameters. */
    @GetMapping("/api")
    @ResponseBody
    public Map<String, Object> apiList(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {

        List<Feedback> feedbacks = service.findFiltered(name, email, category, rating, fromDate, toDate);

        List<Map<String, Object>> list = new ArrayList<>();
        if (feedbacks != null) {
            for (Feedback f : feedbacks) {
                if (f == null) continue;
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", f.getId());
                m.put("name", f.getName());
                m.put("email", f.getEmail());
                m.put("category", f.getCategory());
                m.put("rating", f.getRating());
                m.put("comments", f.getComments());
                m.put("createdAt", f.getCreatedDate() == null
                        ? null
                        : f.getCreatedDate().toString());
                m.put("createdDay", f.getCreatedDay());
                list.add(m);
            }
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("success", true);
        out.put("count", list.size());
        out.put("data", list);
        return out;
    }

    @DeleteMapping("/api/{id}")
    @ResponseBody
    public Map<String, Object> apiDelete(@PathVariable Long id) {
        Map<String, Object> out = new LinkedHashMap<>();
        try {
            service.deleteById(id);
            out.put("success", true);
            out.put("message", "Feedback deleted.");
            return out;
        } catch (Exception e) {
            out.put("success", false);
            out.put("message", "Could not delete that feedback: " + e.getMessage());
            return out;
        }
    }
}