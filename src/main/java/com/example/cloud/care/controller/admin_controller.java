package com.example.cloud.care.controller;

import com.example.cloud.care.dao.doctor_dao;
import com.example.cloud.care.model.Doctor;
import com.example.cloud.care.model.Doctor.Status;
import com.example.cloud.care.service.EmailServiceD;
import com.example.cloud.care.service.doctor_service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class admin_controller {
    @Autowired
    private doctor_dao doctordao ;

    @Autowired
    private doctor_service doctorService;
    @Autowired
private EmailServiceD emailService;

    @Autowired
    private com.example.cloud.care.service.DonorService donorService;

    @Autowired
    private com.example.cloud.care.service.RequestService requestService;

    // Show all doctors (pending, approved, rejected) for Thymeleaf
    @GetMapping("/doctor/status")
    public String showAllDoctors(Model model) {
        List<Doctor> doctors = doctorService.getDoctors(); // include all statuses
        List<Doctor> doctors_pending = doctorService.getDoctorsPending();
        List<Doctor> doctors_rejected = doctorService.getDoctorsRejected();
        if(doctors==null){
            doctors=new ArrayList<>();
        }
        if(doctors_pending==null){
            doctors_pending=new ArrayList<>();        
        }
        if(doctors_rejected==null){
            doctors_rejected=new ArrayList<>();
        }
        model.addAttribute("doctors", doctors);
        model.addAttribute("doctors_pending", doctors_pending);
        model.addAttribute("doctors_rejected", doctors_rejected);
        return "admin_pending_doctors"; // reuse the same Thymeleaf template
    }

    // Admin donor management (mirror doctor flow)
@GetMapping("/donor-management")
public String showDonorManagement(Model model) {
    try {
        List<com.example.cloud.care.model.Donor> donors = donorService.getAllDonors();
        List<com.example.cloud.care.model.Request> requests = requestService.getAllRequests();

        if (donors == null) donors = new ArrayList<>();
        if (requests == null) requests = new ArrayList<>();

        // ---------- SAFE ELIGIBILITY COUNT ----------
        long eligible = donors.stream()
                .filter(this::safeEligibilityCheck)
                .count();

        // ---------- SAFE MAPS ----------
        Map<String, Long> donorsByBlood = donors.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        d -> d.getBloodGroup() == null ? "Unknown" : d.getBloodGroup(),
                        java.util.stream.Collectors.counting()
                ));

        Map<String, Long> requestsByStatus = requests.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        r -> r.getStatus() == null ? "Pending" : r.getStatus(),
                        java.util.stream.Collectors.counting()
                ));

        model.addAttribute("totalDonors", donors.size());
        model.addAttribute("eligibleDonors", eligible);
        model.addAttribute("donorsByBlood", donorsByBlood);
        model.addAttribute("requestsByStatus", requestsByStatus);

        // ---------- RECENT DONORS ----------
        model.addAttribute("recentDonors", donors.stream()
                .sorted((a, b) -> {
                    if (a.getCreatedAt() == null && b.getCreatedAt() == null) return 0;
                    if (a.getCreatedAt() == null) return 1;
                    if (b.getCreatedAt() == null) return -1;
                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                })
                .limit(20)
                .toList()
        );

        // ---------- ELIGIBILITY MAP (SAFE) ----------
        Map<Long, Boolean> eligibilityMap = donors.stream()
                .filter(d -> d.getId() != null)
                .collect(java.util.stream.Collectors.toMap(
                        com.example.cloud.care.model.Donor::getId,
                        this::safeEligibilityCheck
                ));

        model.addAttribute("eligibilityMap", eligibilityMap);

        return "admin_donor_analysis";

    } catch (Exception e) {
        e.printStackTrace();
        model.addAttribute("error", "Failed to load donor management");
        return "error";
    }
}
private boolean safeEligibilityCheck(com.example.cloud.care.model.Donor donor) {
    try {
        Map<String, Object> result = donorService.checkEligibility(donor);
        return Boolean.TRUE.equals(result.get("eligible"));
    } catch (Exception e) {
        return false;
    }
}


    // Approve a doctor via AJAX
    @PostMapping("/approve-doctor")
public String approveDoctor(@RequestParam Long doctorId, RedirectAttributes redirectAttributes) {
    Optional<Doctor> optDoc = doctordao.findById(doctorId);

    if(optDoc.isEmpty()) {
        redirectAttributes.addFlashAttribute("notificationType", "error");
        redirectAttributes.addFlashAttribute("notificationMessage", "Doctor not found");
        return "redirect:/admin/doctor/status";
    }

    Doctor doc = optDoc.get();
    doc.setStatus(Status.APPROVED);
    doctordao.save(doc);

    emailService.sendEmail(doc.getEmail(),
        "Your Doctor Account Has Been Approved!",
        "Hello " + doc.getName() + ",\n\nYour account has been approved. You can now log in.\n\nThanks!");

    redirectAttributes.addFlashAttribute("notificationType", "success");
    redirectAttributes.addFlashAttribute("notificationMessage", "Doctor approved successfully!");
    return "redirect:/admin/doctor/status";
}

@PostMapping("doctor/reject")
public String rejectDoctor(@RequestParam Long doctorId, @RequestParam String reason,
                           RedirectAttributes redirectAttributes) {

    Optional<Doctor> optDoc = doctordao.findById(doctorId);
    if(optDoc.isEmpty()){
        redirectAttributes.addFlashAttribute("notificationType", "error");
        redirectAttributes.addFlashAttribute("notificationMessage", "Doctor not found");
        return "redirect:/admin/doctor/status";
    }

    Doctor doc = optDoc.get();
    doc.setStatus(Status.REJECTED);
    doctordao.save(doc);

    emailService.sendEmail(doc.getEmail(), "Your signup was rejected", reason);

    redirectAttributes.addFlashAttribute("notificationType", "warning");
    redirectAttributes.addFlashAttribute("notificationMessage", "Doctor rejected successfully!");
    return "redirect:/admin/doctor/status";
}
    // Update doctor status dynamically via PATCH
    @PatchMapping("/doctor/{id}/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateDoctorStatus(
            @PathVariable Long id,
            @RequestParam String status
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (status.equalsIgnoreCase("APPROVED")) {
                doctorService.approveDoctor(id);
            } else if (status.equalsIgnoreCase("REJECTED")) {
                doctorService.rejectDoctor(id, "Rejected by admin");
                doctorService.deleteDoctor(id);
            } else if (!status.equalsIgnoreCase("PENDING")) {
                response.put("success", false);
                response.put("message", "Invalid status: " + status);
                return ResponseEntity.badRequest().body(response);
            }

            response.put("success", true);
            response.put("message", "Status updated successfully to " + status);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error updating status: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}