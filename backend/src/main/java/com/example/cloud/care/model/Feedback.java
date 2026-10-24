package com.example.cloud.care.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "feedbacks")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Size(max = 50, message = "Name too long")
    private String name;

    @Size(max = 100, message = "Email too long")
    private String email;

    @Min(1)
    @Max(5)
    private int rating;

    @Size(max = 20)
    private String category;

    @Size(max = 5000, message = "Feedback must be within 5000 characters")
    @Column(length = 5000)
    private String comments;

    private LocalDate createdDate;
    private String createdDay;

    @PrePersist
    public void onCreate() {
        this.createdDate = LocalDate.now();
        this.createdDay = createdDate.getDayOfWeek().toString();
    }

    // Getters & Setters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public int getRating() { return rating; }
    public String getCategory() { return category; }
    public String getComments() { return comments; }
    public LocalDate getCreatedDate() { return createdDate; }
    public String getCreatedDay() { return createdDay; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setRating(int rating) { this.rating = rating; }
    public void setCategory(String category) { this.category = category; }
    public void setComments(String comments) { this.comments = comments; }
}