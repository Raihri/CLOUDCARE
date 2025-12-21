package com.example.cloud.care.controller;

import com.example.cloud.care.model.Donor;
import com.example.cloud.care.service.DonorService;
import com.example.cloud.care.service.RequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/patient/donor-admin")
public class AdminControllerDonor {

    @Autowired
    private DonorService donorService;

    @Autowired
    private RequestService requestService;

    @GetMapping("")
    public String adminPage() {
        return "redirect:/patient/donor";
    }

    @GetMapping("/dashboard")
    public String donorDashboard(org.springframework.ui.Model model) {
        return "admin_dashboardDonor";
    }

    @GetMapping("/api/summary")
    @ResponseBody
    public Map<String, Object> summary() {
        Map<String, Object> res = new HashMap<>();

        List<Donor> allDonors = donorService.getAllDonors();
        int totalDonors = allDonors.size();

        long eligible = allDonors.stream()
                .map(donorService::checkEligibility)
                .filter(m -> Boolean.TRUE.equals(m.get("eligible")))
                .count();

        List<com.example.cloud.care.model.Request> allRequests = requestService.getAllRequests();
        long pending = allRequests.stream().filter(r -> r.getStatus() == null || r.getStatus().equalsIgnoreCase("Pending")).count();
        long matched = allRequests.stream().filter(r -> r.getStatus() != null && r.getStatus().equalsIgnoreCase("Matched")).count();

        // Shortage per blood group (simple counts)
        Map<String, Long> byBlood = allDonors.stream()
                .collect(Collectors.groupingBy(d -> d.getBloodGroup() == null ? "Unknown" : d.getBloodGroup(), Collectors.counting()));

        // Build response
        res.put("success", true);
        res.put("totalDonors", totalDonors);
        res.put("eligibleDonors", eligible);
        res.put("pendingRequests", pending);
        res.put("matchedRequests", matched);
        res.put("byBloodGroup", byBlood);

        return res;
    }

    @GetMapping("/api/summary/trends")
    @ResponseBody
    public Map<String, Object> trends() {
        Map<String, Object> out = new HashMap<>();
        // produce last 14 days donors and requests counts by date (yyyy-MM-dd)
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate from = today.minusDays(13);

        List<Donor> donors = donorService.getAllDonors();
        List<com.example.cloud.care.model.Request> requests = requestService.getAllRequests();

        Map<String, Long> donorByDay = donors.stream()
                .filter(d -> d.getCreatedAt() != null && !d.getCreatedAt().toLocalDate().isBefore(from))
                .collect(Collectors.groupingBy(d -> d.getCreatedAt().toLocalDate().toString(), Collectors.counting()));

        Map<String, Long> reqByDay = requests.stream()
                .filter(r -> r.getCreatedAt() != null && !r.getCreatedAt().toLocalDate().isBefore(from))
                .collect(Collectors.groupingBy(r -> r.getCreatedAt().toLocalDate().toString(), Collectors.counting()));

        java.util.List<String> labels = new java.util.ArrayList<>();
        java.util.List<Long> donorSeries = new java.util.ArrayList<>();
        java.util.List<Long> requestSeries = new java.util.ArrayList<>();

        for (int i = 0; i < 14; i++) {
            java.time.LocalDate d = from.plusDays(i);
            String key = d.toString();
            labels.add(key);
            donorSeries.add(donorByDay.getOrDefault(key, 0L));
            requestSeries.add(reqByDay.getOrDefault(key, 0L));
        }

        out.put("labels", labels);
        out.put("donors", donorSeries);
        out.put("requests", requestSeries);
        out.put("success", true);
        return out;
    }

    @GetMapping("/api/requests/urgent")
    @ResponseBody
    public Map<String, Object> urgentRequests() {
        Map<String, Object> out = new HashMap<>();
        java.time.LocalDate now = java.time.LocalDate.now();
        java.time.LocalDate window = now.plusDays(3);
        List<com.example.cloud.care.model.Request> all = requestService.getAllRequests();
        List<com.example.cloud.care.model.Request> urgent = all.stream()
            .filter(r -> r.getStatus() == null || r.getStatus().equalsIgnoreCase("Pending"))
            .filter(r -> r.getRequiredDate() == null || !r.getRequiredDate().isAfter(window))
            .sorted((a, b) -> {
                if (a.getRequiredDate() == null && b.getRequiredDate() == null) return 0;
                if (a.getRequiredDate() == null) return 1;
                if (b.getRequiredDate() == null) return -1;
                return a.getRequiredDate().compareTo(b.getRequiredDate());
            })
            .toList();
        out.put("success", true);
        out.put("data", urgent);
        return out;
    }

    @org.springframework.web.bind.annotation.PostMapping("/api/request/{id}/mark-matched")
    @ResponseBody
    public Map<String, Object> markMatched(@org.springframework.web.bind.annotation.PathVariable Long id) {
        Map<String, Object> out = new HashMap<>();
        try {
            requestService.updateRequestStatus(id, "Matched");
            out.put("success", true);
            out.put("message", "Request marked matched");
        } catch (Exception e) {
            out.put("success", false);
            out.put("message", e.getMessage());
        }
        return out;
    }
}
