package com.example.cloud.care.service;

import com.example.cloud.care.model.Feedback;
import com.example.cloud.care.repository.FeedbackRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FeedbackService {
    private final FeedbackRepository repo;

    public FeedbackService(FeedbackRepository repo) {
        this.repo = repo;
    }

    public List<Feedback> findAll() {
        return repo.findAll();
    }

    public List<Feedback> findFiltered(String name, String email, String category, Integer rating,
                                       LocalDate fromDate, LocalDate toDate) {
        List<Feedback> all = repo.findAll();

        return all.stream()
                .filter(f -> name == null || name.isEmpty() || 
                    (f.getName() != null && f.getName().toLowerCase().contains(name.toLowerCase())))
                .filter(f -> email == null || email.isEmpty() || 
                    (f.getEmail() != null && f.getEmail().toLowerCase().contains(email.toLowerCase())))
                .filter(f -> category == null || category.isEmpty() || 
                    (f.getCategory() != null && f.getCategory().equalsIgnoreCase(category)))
                .filter(f -> rating == null || f.getRating() == rating)
                .filter(f -> fromDate == null || !f.getCreatedDate().isBefore(fromDate))
                .filter(f -> toDate == null || !f.getCreatedDate().isAfter(toDate))
                .collect(Collectors.toList());
    }

    public void save(Feedback feedback) {
        if (feedback.getRating() < 1 || feedback.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        repo.save(feedback);
    }

    public void deleteById(Long id) {
        repo.deleteById(id);
    }
}