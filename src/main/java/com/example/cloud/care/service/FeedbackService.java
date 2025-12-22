package com.example.cloud.care.service;

import com.example.cloud.care.model.Feedback;
import com.example.cloud.care.repository.FeedbackRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeedbackService {

    private final FeedbackRepository repo;

    public FeedbackService(FeedbackRepository repo) {
        this.repo = repo;
    }

    public List<Feedback> findAll() {
        return repo.findAll();
    }

    public void save(Feedback feedback) {
        if (feedback.getRating() < 1 || feedback.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        repo.save(feedback);
    }
}