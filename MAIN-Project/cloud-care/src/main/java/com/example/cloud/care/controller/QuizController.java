package com.example.cloud.care.controller;

import com.example.cloud.care.var.QuizResult;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

@Controller
public class QuizController {

    private static final String[] OPTIONS = {"Never", "Sometimes", "Often", "Neutral"};
    private static final int[] SCORES = {0, 1, 2, 1};

    @GetMapping("/quiz_index")
    public String index() {
        return "mental_health_index";
    }

    @GetMapping("/quiz")
    public String quiz(@RequestParam String name, Model model) throws IOException {
        List<String> questions = loadQuestions();
        model.addAttribute("name", name.isEmpty() ? "Anonymous" : name);
        model.addAttribute("questions", questions);
        model.addAttribute("options", OPTIONS);
        return "quiz";
    }

    @PostMapping("/result")
    public String result(@RequestParam Map<String, String> answers, Model model) {
        String name = answers.get("name");
        if (name == null || name.isEmpty()) name = "Anonymous";

        int score = 0;
        for (int i = 0; i < 20; i++) {
            String ans = answers.get("q" + i);
            int idx = Arrays.asList(OPTIONS).indexOf(ans);
            score += (idx >= 0) ? SCORES[idx] : 1; // default Neutral
        }

        String status = getStatus(score);
        String suggestion = getSuggestion(score);

        QuizResult result = new QuizResult(name, score, status, suggestion);
        model.addAttribute("result", result);
        model.addAttribute("shareLink", generateShareLink(result));

        return "result";
    }

    @GetMapping("/download-pdf")
    public void downloadPdf(@ModelAttribute QuizResult result, HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Mental_Health_Report_" + result.getName().replaceAll("\\s+", "_") + ".pdf";
        response.setHeader(headerKey, headerValue);

        PdfWriter writer = new PdfWriter(response.getOutputStream());
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("Mental Health Assessment Report")
                .setFontSize(18).setBold());
        document.add(new Paragraph("Name: " + result.getName()));
        document.add(new Paragraph("Score: " + result.getScore() + " / 40"));
        document.add(new Paragraph("Status: " + result.getStatus()));
        document.add(new Paragraph("Recommendation:"));
        document.add(new Paragraph(result.getSuggestion()));
        document.add(new Paragraph("Helpline: 1800-XXX-XXXX"));
        document.add(new Paragraph("Generated on: " + new Date()));

        document.close();
    }

    private List<String> loadQuestions() throws IOException {
        ClassPathResource resource = new ClassPathResource("questions.txt");
        return Files.readAllLines(resource.getFile().toPath())
                .stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .limit(20)
                .toList();
    }

    private String getStatus(int score) {
        if (score <= 10) return "Excellent Mental Health";
        if (score <= 20) return "Good Mental Health";
        if (score <= 30) return "Moderate - Needs Attention";
        if (score <= 40) return "Concerning - Seek Support";
        return "Critical - Immediate Help Needed";
    }

    private String getSuggestion(int score) {
        if (score <= 10) return "Keep up the great work! Continue self-care.";
        if (score <= 20) return "You're doing well. Consider mindfulness or journaling.";
        if (score <= 30) return "Talk to a friend or counselor. Try relaxation techniques.";
        if (score <= 40) return "Please consult a mental health professional soon.";
        return "Call a helpline immediately. You're not alone.";
    }

    private String generateShareLink(QuizResult r) {
        return "/result?name=" + r.getName() + "&score=" + r.getScore();
    }
}