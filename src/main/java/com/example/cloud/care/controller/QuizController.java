package com.example.cloud.care.controller;

import com.example.cloud.care.model.QuizResult;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/patient")
public class QuizController {

    private static final String[] OPTIONS = {"Not at all", "Several days", "More than half the days", "Nearly every day"};
    private static final int[] SCORES = {0, 1, 2, 3};
    private static final Pattern QUESTION_KEY = Pattern.compile("^q(\\d+)$");

    @GetMapping({ "/quiz_index1"})
    public String index1() {
        return "index1";
    }

    @GetMapping("/quiz")
    public String quiz(@RequestParam(name = "name", required = false) String name, Model model) {
        List<String> questions;
        try {
            questions = loadQuestions();
        } catch (IOException e) {
            e.printStackTrace();
            questions = Collections.emptyList();
        }

        model.addAttribute("name", (name == null || name.isEmpty()) ? "Anonymous" : name);
        model.addAttribute("questions", questions);
        model.addAttribute("options", OPTIONS);
        return "quiz";
    }

    @PostMapping("/result")
    public String result(@RequestParam Map<String, String> answers, Model model, HttpSession session) {
        String name = answers.getOrDefault("name", "Anonymous");

        int score = 0;
        int questionCount = 0;

        for (Map.Entry<String, String> e : answers.entrySet()) {
            Matcher m = QUESTION_KEY.matcher(e.getKey());
            if (m.matches()) {
                questionCount++;
                String ans = e.getValue();
                int idx = Arrays.asList(OPTIONS).indexOf(ans);
                score += (idx >= 0) ? SCORES[idx] : 0;
            }
        }

        String status = getStatus(score);
        String suggestion = getSuggestion(score);

        QuizResult result = new QuizResult(name, score, status, suggestion);

        // Example suicidal-risk check (adjust key/name to match your form if you have such a question)
        if (answers.containsKey("q_suicidal")) {
            String val = answers.get("q_suicidal");
            if ("Nearly every day".equals(val) || "More than half the days".equals(val)) {
                result.setSuicidalRisk(true);
            }
        }

        session.setAttribute("lastQuizResult", result);

        model.addAttribute("result", result);
        model.addAttribute("shareLink", generateShareLink(result));
        model.addAttribute("questionCount", questionCount);

        return "result";
    }

    @GetMapping("/result")
    public String getResultView(@RequestParam(required = false) String name,
                                @RequestParam(required = false) Integer score,
                                @RequestParam(required = false) String status,
                                @RequestParam(required = false) String suggestion,
                                Model model,
                                HttpSession session) {
        QuizResult result = (QuizResult) session.getAttribute("lastQuizResult");

        if (result == null && name != null && score != null && status != null && suggestion != null) {
            result = new QuizResult(name, score, status, suggestion);
        }

        if (result == null) {
            model.addAttribute("message", "No result found. Please take the quiz first.");
            return "index1";
        }

        model.addAttribute("result", result);
        model.addAttribute("shareLink", generateShareLink(result));
        return "result";
    }

    @GetMapping("/download-pdf")
    public void downloadPdf(@RequestParam(required = false) String name,
                            @RequestParam(required = false) Integer score,
                            @RequestParam(required = false) String status,
                            @RequestParam(required = false) String suggestion,
                            HttpServletResponse response,
                            HttpSession session) throws IOException {

        QuizResult result = (QuizResult) session.getAttribute("lastQuizResult");
        if (result == null && name != null) {
            result = new QuizResult(name,
                    score == null ? 0 : score,
                    status == null ? getStatus(score == null ? 0 : score) : status,
                    suggestion == null ? getSuggestion(score == null ? 0 : score) : suggestion);
        }

        if (result == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No result available for PDF generation");
            return;
        }

        response.setContentType("application/pdf");
        String fileNameSafe = result.getName() == null ? "report" : result.getName().replaceAll("\\s+", "_");
        response.setHeader("Content-Disposition", "attachment; filename=Mental_Health_Report_" + fileNameSafe + ".pdf");

        String html = buildHtmlForResult(result);

        ByteArrayOutputStream baos = null;
        OutputStream os = null;
        try {
            baos = new ByteArrayOutputStream();
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(baos);
            renderer.finishPDF();

            byte[] pdfBytes = baos.toByteArray();
            response.setContentLength(pdfBytes.length);
            os = response.getOutputStream();
            os.write(pdfBytes);
            os.flush();
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "PDF generation failed: " + e.getMessage());
        } finally {
            if (baos != null) try { baos.close(); } catch (IOException ignored) {}
            if (os != null) try { os.close(); } catch (IOException ignored) {}
        }
    }

    private String buildHtmlForResult(QuizResult result) {
        return "<html><head><meta charset='utf-8'/><style>"
                + "body{font-family: Arial, Helvetica, sans-serif; font-size:12px;}"
                + ".title{font-size:18px; font-weight:bold; margin-bottom:10px}" 
                + ".section{margin-top:8px}" 
                + "</style></head><body>"
                + "<div class='title'>Mental Health Assessment Report</div>"
                + "<div class='section'><strong>Name:</strong> " + escapeHtml(result.getName()) + "</div>"
                + "<div class='section'><strong>Score:</strong> " + result.getScore() + "</div>"
                + "<div class='section'><strong>Status:</strong> " + escapeHtml(result.getStatus()) + "</div>"
                + "<div class='section'><strong>Recommendation:</strong><br/>" + escapeHtml(result.getSuggestion()) + "</div>"
                + "<div class='section'><strong>Helpline:</strong> 1800-XXX-XXXX</div>"
                + "<div class='section'><em>Generated on: " + new Date() + "</em></div>"
                + "</body></html>";
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;")
                .replace("'", "&#39;").replace("\n", "<br/>");
    }

    private List<String> loadQuestions() throws IOException {
        List<String> questions = new ArrayList<>();
        ClassPathResource resource = new ClassPathResource("questions.txt");
        InputStream in = null;
        BufferedReader reader = null;
        try {
            in = resource.getInputStream();
            reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null && questions.size() < 200) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) questions.add(trimmed);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (reader != null) try { reader.close(); } catch (IOException ignored) {}
            if (in != null) try { in.close(); } catch (IOException ignored) {}
        }
        return questions;
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
        return "/result?name=" + encode(r.getName()) + "&score=" + r.getScore()
                + "&status=" + encode(r.getStatus()) + "&suggestion=" + encode(r.getSuggestion());
    }

    private String encode(String s) {
        if (s == null) return "";
        return s.replaceAll(" ", "%20").replaceAll("[^A-Za-z0-9%_\\-\\.]", "");
    }
}