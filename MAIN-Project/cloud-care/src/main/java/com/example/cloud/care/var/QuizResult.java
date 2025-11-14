package com.example.cloud.care.var;

import lombok.Data;

@Data
public class QuizResult {
    private String name;
    private int score;
    private String status;
    private String suggestion;

    // Constructors
    public QuizResult() {}
    public QuizResult(String name, int score, String status, String suggestion) {
        this.name = name;
        this.score = score;
        this.status = status;
        this.suggestion = suggestion;
    }
}