package com.example.cloud.care.model;

public class QuizResult {
    private String name;
    private int score;
    private String status;
    private String suggestion;
    private boolean suicidalRisk = false;

    public QuizResult() {}

    public QuizResult(String name, int score, String status, String suggestion) {
        this.name = name;
        this.score = score;
        this.status = status;
        this.suggestion = suggestion;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getSuggestion() { return suggestion; }
    public void setSuggestion(String suggestion) { this.suggestion = suggestion; }
    public boolean hasSuicidalRisk() { return suicidalRisk; }
    public void setSuicidalRisk(boolean suicidalRisk) { this.suicidalRisk = suicidalRisk; }
}