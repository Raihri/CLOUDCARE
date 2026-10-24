package com.example.cloud.care.controller.admin;

import com.example.cloud.care.model.Doctor;
import com.example.cloud.care.service.DoctorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")
public class AdminDoctorController {

    private final DoctorService doctorService;
    private final com.example.cloud.care.service.DonorService donorService;

    public AdminDoctorController(DoctorService doctorService,
                                 com.example.cloud.care.service.DonorService donorService) {
        this.doctorService = doctorService;
        this.donorService = donorService;
    }

    private boolean safeEligibilityCheck(com.example.cloud.care.model.Donor donor) {
        try {
            Map<String, Object> result = donorService.checkEligibility(donor);
            return Boolean.TRUE.equals(result.get("eligible"));
        } catch (Exception e) {
            return false;
        }
    }

    // Update doctor status dynamically via PATCH
    @PatchMapping("/doctor/{id}/status")
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
