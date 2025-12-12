package com.example.cloud.care.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description = "";
    private String category = "General";
    private String priority = "Medium";
    private String status = "Pending";

    private LocalDate createdDate = LocalDate.now();
    private LocalDate dueDate = LocalDate.now();   // ← Default to today → appears in today’s list

    private boolean completed = false;
    private int streak = 0;

    // Constructors
    public Task() {
        this.dueDate = LocalDate.now(); // Default to today if not set
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDate createdDate) { this.createdDate = createdDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public int getStreak() { return streak; }
    public void setStreak(int streak) { this.streak = streak; }

    // Fixed: Handle null dueDate safely
    public boolean isActive() {
        LocalDate today = LocalDate.now();
        return !completed && dueDate != null && dueDate.isEqual(today);
    }

    public double getWeeklyFulfillment() {
        return completed ? 100.0 : 0.0;
    }
}