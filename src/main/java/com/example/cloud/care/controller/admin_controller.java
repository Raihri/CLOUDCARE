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
import java.util.stream.Collectors;

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
        return "error_page";
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
            @RequestParam String status,
            @RequestParam(required = false) String reason
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (status.equalsIgnoreCase("APPROVED")) {
                doctorService.approveDoctor(id);
            } else if (status.equalsIgnoreCase("REJECTED")) {
                // Rejection only changes status. This used to call
                // deleteDoctor(id) as well, which destroyed the application the
                // doctor had submitted along with their credentials — they could
                // never see why they were turned down, and re-applying was
                // impossible without re-entering everything. The reason the
                // caller supplies is also now recorded instead of a fixed string.
                doctorService.rejectDoctor(id, reason != null && !reason.isBlank()
                        ? reason
                        : "Rejected by admin");
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

    /** JSON listing of doctors, for the Next.js admin pages. */
    @GetMapping("/api/doctors")
    @ResponseBody
    public Map<String, Object> apiDoctors(
            @RequestParam(required = false, defaultValue = "ALL") String status) {

        List<Doctor> doctors;
        if ("PENDING".equalsIgnoreCase(status)) {
            doctors = doctorService.getDoctorsPending();
        } else if ("REJECTED".equalsIgnoreCase(status)) {
            doctors = doctorService.getDoctorsRejected();
        } else if ("APPROVED".equalsIgnoreCase(status)) {
            // getDoctors() returns everything; filter to approved so this
            // status actually narrows the list.
            doctors = doctorService.getDoctors().stream()
                    .filter(d -> d.getStatus() == com.example.cloud.care.model.Doctor.Status.APPROVED)
                    .toList();
        } else {
            doctors = doctorService.getDoctors();
        }

        List<Map<String, Object>> list = new ArrayList<>();
        if (doctors != null) {
            for (Doctor d : doctors) {
                if (d == null) continue;
                Map<String, Object> m = new HashMap<>();
                m.put("id", d.getId());
                m.put("name", d.getName());
                m.put("email", d.getEmail());
                m.put("bmdcRegNo", d.getBmdcRegNo());
                m.put("specialization", d.getSpecialization());
                m.put("degrees", d.getDegrees());
                m.put("hospitalName", d.getHospitalName());
                m.put("zilla", d.getZilla());
                m.put("profileImage", d.getProfileImage());
                m.put("experienceYears", d.getExperienceYears());
                m.put("status", d.getStatus() != null ? d.getStatus().name() : null);
                // Explicitly not exposed: password, resetToken. This map is
                // built by hand rather than serialising the entity so those
                // can't be added by accident later.
                list.add(m);
            }
        }

        Map<String, Object> out = new HashMap<>();
        out.put("success", true);
        out.put("count", list.size());
        out.put("data", list);
        return out;
    }

    /** Headline counts for the admin dashboard. */
    @GetMapping("/api/dashboard")
    @ResponseBody
    public Map<String, Object> apiDashboard() {
        Map<String, Object> out = new HashMap<>();

        List<Doctor> all = doctorService.getDoctors();
        List<Doctor> pending = doctorService.getDoctorsPending();
        List<Doctor> rejected = doctorService.getDoctorsRejected();

        long approved = all == null ? 0 : all.stream()
                .filter(d -> d.getStatus() == com.example.cloud.care.model.Doctor.Status.APPROVED)
                .count();

        out.put("success", true);
        out.put("totalDoctors", all == null ? 0 : all.size());
        out.put("pendingDoctors", pending == null ? 0 : pending.size());
        out.put("approvedDoctors", approved);
        out.put("rejectedDoctors", rejected == null ? 0 : rejected.size());
        return out;
    }

    /** Donor + request totals, for the admin donor analysis page. */
    @GetMapping("/api/donor-summary")
    @ResponseBody
    public Map<String, Object> apiDonorSummary() {
        Map<String, Object> out = new HashMap<>();
        try {
            List<com.example.cloud.care.model.Donor> donors = donorService.getAllDonors();
            out.put("success", true);
            out.put("totalDonors", donors == null ? 0 : donors.size());

            if (donors != null) {
                Map<String, Long> byGroup = donors.stream()
                        .filter(d -> d != null && d.getBloodGroup() != null)
                        .collect(Collectors.groupingBy(
                                d -> d.getBloodGroup().trim().toUpperCase(),
                                Collectors.counting()));
                out.put("byBloodGroup", byGroup);

                // Reuses the existing safeEligibilityCheck, which already
                // swallows per-row errors so one bad record can't 500 the page.
                long eligible = donors.stream()
                        .filter(d -> d != null)
                        .filter(this::safeEligibilityCheck)
                        .count();
                out.put("eligibleDonors", eligible);
            }
            return out;
        } catch (Exception e) {
            out.put("success", false);
            out.put("message", "Could not load donor summary: " + e.getMessage());
            return out;
        }
    }
}