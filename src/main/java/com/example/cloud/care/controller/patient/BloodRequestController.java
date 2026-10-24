package com.example.cloud.care.controller.patient;

import com.example.cloud.care.model.Request;
import com.example.cloud.care.service.RequestService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDate;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/patient/request")
public class BloodRequestController {
    @Autowired
    private com.example.cloud.care.service.DonorService donorService;
    @Autowired
    private com.example.cloud.care.service.EmailService emailService;

    private final RequestService requestService;
    private final com.example.cloud.care.service.CurrentPatientService currentPatientService;

    public BloodRequestController(RequestService requestService,
            com.example.cloud.care.service.CurrentPatientService currentPatientService) {
        this.requestService = requestService;
        this.currentPatientService = currentPatientService;
    }

    // ========== OWNERSHIP ==========

    /**
     * The caller's email, or null when nobody usable is signed in.
     *
     * <p>Ownership is matched on email because the request table stores only an
     * email string — the legacy forms wrote it straight from the signed-in
     * account, and there is no foreign key to join on.
     */
    private String currentEmail() {
        try {
            com.example.cloud.care.model.Patient p = currentPatientService.currentPatient();
            // Email hangs off the associated User, not off Patient directly.
            if (p == null || p.getUser() == null) {
                return null;
            }
            return p.getUser().getEmail();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Loads a request addressed by id, but only if the caller owns it.
     *
     * <p>Every id-addressed handler used to call
     * {@code requestService.getRequest(id)} directly, so any signed-in patient
     * could read, edit or confirm somebody else's blood request — including its
     * name, email and medical reason. Returning null for a non-owned request
     * keeps the "not found" shape those handlers already handle, so the change
     * is invisible to legitimate callers.
     */
    private Request getOwnedRequest(Long id) {
        Request request = requestService.getRequest(id);
        if (request == null || request.getEmail() == null) {
            return null;
        }
        String email = currentEmail();
        if (email == null) {
            return null;
        }
        return email.equalsIgnoreCase(request.getEmail()) ? request : null;
    }

    /**
     * Restricts a request list to the caller's own entries, so a
     * patient-facing list never includes another patient's requests.
     */
    private List<Request> ownRequests(List<Request> requests) {
        String email = currentEmail();
        if (email == null) {
            return List.of();
        }
        return requests.stream()
                .filter(r -> r.getEmail() != null && email.equalsIgnoreCase(r.getEmail()))
                .toList();
    }

    // ========== WEB UI ENDPOINTS ==========

    @GetMapping("/create")
    public String showBloodRequestForm(Model model,
                                      @RequestParam(required = false) String fullName,
                                      @RequestParam(required = false) String userBloodGroup,
                                      @RequestParam(required = false) String loginEmail) {
        model.addAttribute("fullName", fullName);
        model.addAttribute("userBloodGroup", userBloodGroup);
        model.addAttribute("loginEmail", loginEmail);
        model.addAttribute("today", LocalDate.now());
        return "blood_request_form";
    }

    @PostMapping("/create")
    public String processBloodRequest(@RequestParam String name,
                                     @RequestParam String bloodGroup,
                                     @RequestParam int units,
                                     @RequestParam String requiredDate,
                                     @RequestParam String loginEmail,
                                     @RequestParam(required = false) String newEmail,
                                     @RequestParam(required = false) String medicalReason,
                                     Model model) {
        LocalDate today = LocalDate.now();
        LocalDate reqDate = LocalDate.parse(requiredDate);
        if (reqDate.isBefore(today)) {
            model.addAttribute("msg", "Required Blood Date must be today or later.");
            model.addAttribute("today", today);
            return "blood_request_form";
        }
        String selectedEmail = (newEmail != null && !newEmail.isBlank()) ? newEmail : loginEmail;
        Request request = new Request();
        request.setName(name);
        request.setEmail(selectedEmail);
        request.setBloodGroup(bloodGroup);
        request.setUnits(units);
        request.setRequiredDate(reqDate);
        request.setMedicalReason(medicalReason);
        request.setStatus("Pending");
        requestService.saveRequest(request);
        // Redirect to matching window to find donors
        return "redirect:/patient/donor-response-list?requestId=" + request.getId();
    }

    @GetMapping("/pending-requests")
    public String showPendingRequests(Model model) {
        // Scoped to the caller: this listed every patient's pending requests
        // system-wide, exposing their names, emails and medical reasons.
        java.util.List<Request> pendingRequests = ownRequests(requestService.getAllRequests()).stream()
            .filter(r -> "Pending".equals(r.getStatus()))
            .toList();
        model.addAttribute("pendingRequests", pendingRequests);
        return "pending_requests";
    }

    @GetMapping("/view-request/{id}")
    public String viewRequestDetails(@PathVariable Long id, Model model) {
        Request request = getOwnedRequest(id);
        if (request == null) {
            model.addAttribute("error", "Request not found");
            return "error_page";
        }
        model.addAttribute("request", request);
        model.addAttribute("today", LocalDate.now());
        return "donor_view_request";
    }

    /**
     * Show edit form for an existing request
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Request request = getOwnedRequest(id);
        if (request == null) {
            model.addAttribute("error", "Request not found");
            return "error_page";
        }
        model.addAttribute("request", request);
        model.addAttribute("editMode", true);
        model.addAttribute("today", LocalDate.now());
        return "blood_request_form";
    }

    /**
     * Process edit submission for an existing request
     */
    @PostMapping("/edit/{id}")
    public String processEdit(@PathVariable Long id,
                              @RequestParam String name,
                              @RequestParam String bloodGroup,
                              @RequestParam int units,
                              @RequestParam String requiredDate,
                              @RequestParam String loginEmail,
                              @RequestParam(required = false) String newEmail,
                              @RequestParam(required = false) String medicalReason,
                              Model model) {
        Request existing = getOwnedRequest(id);
        if (existing == null) {
            model.addAttribute("error", "Request not found");
            return "error_page";
        }

        LocalDate today = LocalDate.now();
        LocalDate reqDate = LocalDate.parse(requiredDate);
        if (reqDate.isBefore(today)) {
            model.addAttribute("msg", "Required Blood Date must be today or later.");
            model.addAttribute("today", today);
            model.addAttribute("request", existing);
            model.addAttribute("editMode", true);
            return "blood_request_form";
        }

        existing.setName(name);
        String selectedEmail = (newEmail != null && !newEmail.isBlank()) ? newEmail : loginEmail;
        existing.setEmail(selectedEmail);
        existing.setBloodGroup(bloodGroup);
        existing.setUnits(units);
        existing.setRequiredDate(reqDate);
        existing.setMedicalReason(medicalReason);

        requestService.saveRequest(existing);

        return "redirect:/request/pending-requests";
    }

    // ========== REST API ENDPOINTS ==========

    @GetMapping("/api/list")
    @ResponseBody
    public ResponseEntity<?> listAllRequests() {
        try {
            // Scoped to the caller — this used to hand back every patient's
            // requests, including names, emails and medical reasons.
            List<Request> requests = ownRequests(requestService.getAllRequests());
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", requests.size());
            response.put("data", requests);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @GetMapping("/api/search")
    @ResponseBody
    public ResponseEntity<?> searchRequests(@RequestParam(required = false) String district,
                                            @RequestParam(required = false) String thana,
                                            @RequestParam(required = false) String status,
                                            @RequestParam(required = false) String bloodGroup,
                                            @RequestParam(required = false) String name) {
        try {
            // Scoped to the caller before any filter runs, so search can't be
            // used to probe other patients' records.
            List<Request> requests = ownRequests(requestService.getAllRequests());
            if (district != null && !district.isBlank()) {
                requests = requests.stream().filter(r -> r.getDistrict() != null && district.equalsIgnoreCase(r.getDistrict())).toList();
            }
            if (thana != null && !thana.isBlank()) {
                requests = requests.stream().filter(r -> r.getThana() != null && thana.equalsIgnoreCase(r.getThana())).toList();
            }
            if (status != null && !status.isBlank()) {
                requests = requests.stream().filter(r -> r.getStatus() != null && status.equalsIgnoreCase(r.getStatus())).toList();
            }
            if (bloodGroup != null && !bloodGroup.isBlank()) {
                requests = requests.stream().filter(r -> r.getBloodGroup() != null && bloodGroup.equalsIgnoreCase(r.getBloodGroup())).toList();
            }
            if (name != null && !name.isBlank()) {
                String lower = name.toLowerCase();
                requests = requests.stream().filter(r -> r.getName() != null && r.getName().toLowerCase().contains(lower)).toList();
            }
            return ResponseEntity.ok(Map.of("success", true, "count", requests.size(), "data", requests));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/api/get/{id}")
    @ResponseBody
    public ResponseEntity<?> getRequestDetails(@PathVariable Long id) {
        try {
            Request request = getOwnedRequest(id);
            if (request == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Request not found");
                return ResponseEntity.status(404).body(error);
            }
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @PostMapping("/api/create")
    @ResponseBody
    public ResponseEntity<?> createRequest(@RequestBody Request request) {
        try {
            if (request.getName() == null || request.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("Requester name is required");
            }
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                throw new IllegalArgumentException("Email is required");
            }
            if (request.getBloodGroup() == null || request.getBloodGroup().trim().isEmpty()) {
                throw new IllegalArgumentException("Blood group is required");
            }
            if (request.getUnits() <= 0) {
                throw new IllegalArgumentException("Units must be greater than 0");
            }
            if (request.getStatus() == null) {
                request.setStatus("Pending");
            }
            // Save first
            requestService.saveRequest(request);

            // Try to find matching donors (by blood group + district/thana)
            java.util.List<com.example.cloud.care.model.Donor> matches = new java.util.ArrayList<>();
            try {
                if (request.getBloodGroup() != null && !request.getBloodGroup().isBlank()) {
                    matches = donorService.findMatchingDonors(request.getBloodGroup(), request.getDistrict(), request.getThana());
                }
            } catch (Exception e) {
                // ignore match errors
            }

            if (matches != null && !matches.isEmpty()) {
                request.setStatus("Matched");
                requestService.saveRequest(request);
            } else {
                request.setStatus("Pending");
                requestService.saveRequest(request);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Request created successfully");
            response.put("data", request);
            response.put("matchedCount", matches == null ? 0 : matches.size());
            response.put("matchedDonors", matches == null ? java.util.Collections.emptyList() : matches);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(400).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @PutMapping("/api/update/{id}")
    @ResponseBody
    public ResponseEntity<?> updateRequest(@PathVariable Long id, @RequestBody Request request) {
        try {
            Request existing = getOwnedRequest(id);
            if (existing == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Request not found");
                return ResponseEntity.status(404).body(error);
            }
            if (request.getName() != null) existing.setName(request.getName());
            // email is deliberately NOT taken from the payload: it is the
            // ownership key, so letting a caller rewrite it would let them
            // hand their request to someone else (or claim somebody else's).
            // Pinned to the signed-in account instead.
            if (request.getBloodGroup() != null) existing.setBloodGroup(request.getBloodGroup());
            if (request.getUnits() > 0) existing.setUnits(request.getUnits());
            if (request.getRequiredDate() != null) existing.setRequiredDate(request.getRequiredDate());
            if (request.getMedicalReason() != null) existing.setMedicalReason(request.getMedicalReason());
            if (request.getStatus() != null) existing.setStatus(request.getStatus());
            requestService.saveRequest(existing);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Request updated successfully");
            response.put("data", existing);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @DeleteMapping("/api/delete/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteRequest(@PathVariable Long id) {
        try {
            Request request = getOwnedRequest(id);
            if (request == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Request not found");
                return ResponseEntity.status(404).body(error);
            }
            requestService.deleteRequest(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Request deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * NEW: Find eligible matching donors for a request
     * Logic: blood group match + eligibility check
     * Returns: list of eligible donors without sending emails
     */
    @GetMapping("/api/find-match/{requestId}")
    @ResponseBody
    public ResponseEntity<?> findMatchingDonors(@PathVariable Long requestId) {
        try {
            System.out.println("🔍 [DEBUG] GET /request/api/find-match/" + requestId);
            
            Request req = getOwnedRequest(requestId);
            System.out.println("🔍 [DEBUG] Request found: " + (req != null ? "YES" : "NO"));
            
            if (req == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Request not found");
                return ResponseEntity.status(404).body(error);
            }

            System.out.println("🔍 [DEBUG] Request ID: " + req.getId());
            System.out.println("🔍 [DEBUG] Request Name: " + req.getName());
            System.out.println("🔍 [DEBUG] Request Blood Group: '" + req.getBloodGroup() + "'");
            System.out.println("🔍 [DEBUG] Request Status: " + req.getStatus());
            
            String bloodGroup = req.getBloodGroup();
            java.util.List<com.example.cloud.care.model.Donor> eligibleDonors = new java.util.ArrayList<>();

            System.out.println("🔍 [DEBUG] Blood group is null: " + (bloodGroup == null));
            System.out.println("🔍 [DEBUG] Blood group is blank: " + (bloodGroup != null && bloodGroup.isBlank()));

            if (bloodGroup != null && !bloodGroup.isBlank()) {
                // Find donors with matching blood group and location
                // Fetch all donors matching the blood group (across locations).
                // Frontend will provide location filtering if needed.
                java.util.List<com.example.cloud.care.model.Donor> bloodMatched = donorService.findMatchingDonors(bloodGroup);
                System.out.println("🔍 [DEBUG] Blood group matched donors count: " + bloodMatched.size());
                
                // Filter by eligibility status
                for (com.example.cloud.care.model.Donor donor : bloodMatched) {
                    System.out.println("🔍 [DEBUG] Checking eligibility for donor: " + donor.getName() + " (ID: " + donor.getId() + ")");
                    try {
                        Map<String, Object> eligibility = donorService.checkEligibility(donor);
                        System.out.println("🔍 [DEBUG]   - Eligibility Check Result: " + eligibility);
                        boolean isEligible = Boolean.TRUE.equals(eligibility.get("eligible"));
                        System.out.println("🔍 [DEBUG]   - Is Eligible: " + isEligible);
                        if (isEligible) {
                            eligibleDonors.add(donor);
                            System.out.println("🔍 [DEBUG]   - ✅ Added to eligible list");
                        } else {
                            System.out.println("🔍 [DEBUG]   - ❌ Not eligible. Reason: " + eligibility.get("reason"));
                        }
                    } catch (Exception ex) {
                        System.out.println("🔍 [DEBUG]   - ⚠️ Exception during eligibility check: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                }
            } else {
                System.out.println("🔍 [DEBUG] Blood group is null or blank. Skipping donor search.");
            }

            System.out.println("🔍 [DEBUG] Final eligible donors count: " + eligibleDonors.size());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("request", req);
            response.put("bloodGroup", bloodGroup);
            response.put("matchedCount", eligibleDonors.size());
            response.put("donors", eligibleDonors);
            
            System.out.println("🔍 [DEBUG] Sending response with " + eligibleDonors.size() + " donors");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("🔍 [DEBUG] Exception caught: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * NEW: Send matching notification emails to all eligible donors
     * Payload: { requestId, donors: [...] }
     * Effect: emails sent to donors; request status updated to "Matched"
     */
    @PostMapping("/api/send-match-emails")
    @ResponseBody
    public ResponseEntity<?> sendMatchingEmails(@RequestBody Map<String, Object> payload) {
        try {
            Long requestId = Long.parseLong(payload.get("requestId").toString());
            List<?> donorList = (List<?>) payload.get("donors");
            
            Request request = getOwnedRequest(requestId);
            if (request == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Request not found");
                return ResponseEntity.status(404).body(error);
            }

            int emailsSent = 0;
            for (Object donorObj : donorList) {
                Map<String, Object> donorMap = (Map<String, Object>) donorObj;
                String donorEmail = (String) donorMap.get("email");
                String donorName = (String) donorMap.get("name");

                if (donorEmail != null && !donorEmail.isBlank()) {
                    String emailBody = buildMatchingEmailBody(
                        donorName,
                        request.getName(),
                        request.getEmail(),
                        request.getBloodGroup(),
                        request.getUnits(),
                        request.getRequiredDate() != null ? request.getRequiredDate().toString() : "ASAP",
                        request.getMedicalReason(),
                        requestId
                    );
                    
                    emailService.sendHtmlEmailAsync(
                        donorEmail,
                        "🩸 Blood Request Match - Your Help is Needed!",
                        emailBody
                    );
                    emailsSent++;
                }
            }

            // Update request status to "Matched"
            request.setStatus("Matched");
            requestService.saveRequest(request);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Emails sent to " + emailsSent + " eligible donors");
            response.put("emailsSent", emailsSent);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Helper: Build beautiful HTML email for donors
     */
    private String buildMatchingEmailBody(String donorName, String requesterName, String requesterEmail,
                                          String bloodGroup, int units, String requiredDate,
                                          String medicalReason, Long requestId) {
        return "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "<meta charset='UTF-8'>\n" +
            "<style>\n" +
            "body { font-family: 'Poppins', Arial, sans-serif; background: #f5f5f5; margin: 0; padding: 0; }\n" +
            ".container { max-width: 600px; margin: 20px auto; background: #fff; border-radius: 12px; box-shadow: 0 4px 15px rgba(0,0,0,0.1); overflow: hidden; }\n" +
            ".header { background: linear-gradient(135deg, #d32f2f, #b71c1c); color: #fff; padding: 30px; text-align: center; }\n" +
            ".header h2 { margin: 0; font-size: 24px; }\n" +
            ".content { padding: 30px; }\n" +
            ".details { background: #f9f9f9; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #d32f2f; }\n" +
            ".detail-row { display: flex; justify-content: space-between; margin-bottom: 12px; }\n" +
            ".detail-label { font-weight: 600; color: #666; }\n" +
            ".detail-value { color: #d32f2f; font-weight: 700; }\n" +
            ".action { text-align: center; margin: 25px 0; }\n" +
            ".btn { display: inline-block; padding: 12px 30px; background: #d32f2f; color: #fff; text-decoration: none; border-radius: 8px; font-weight: 600; transition: background 0.3s; }\n" +
            ".btn:hover { background: #b71c1c; }\n" +
            ".footer { background: #f9f9f9; padding: 20px; text-align: center; border-top: 1px solid #eee; color: #999; font-size: 12px; }\n" +
            "</style>\n" +
            "</head>\n" +
            "<body>\n" +
            "<div class='container'>\n" +
            "<div class='header'>\n" +
            "<h2>🩸 Blood Request Match</h2>\n" +
            "</div>\n" +
            "<div class='content'>\n" +
            "<p>Dear <strong>" + (donorName != null ? donorName : "Donor") + "</strong>,</p>\n" +
            "<p>Great news! A patient has submitted a blood request and your blood type (<strong>" + bloodGroup + "</strong>) matches perfectly!</p>\n" +
            "<div class='details'>\n" +
            "<h3 style='margin-top: 0; color: #d32f2f;'>Request Details:</h3>\n" +
            "<div class='detail-row'><span class='detail-label'>Patient Name:</span> <span class='detail-value'>" + requesterName + "</span></div>\n" +
            "<div class='detail-row'><span class='detail-label'>Contact Email:</span> <span class='detail-value'>" + requesterEmail + "</span></div>\n" +
            "<div class='detail-row'><span class='detail-label'>Blood Group Needed:</span> <span class='detail-value'>" + bloodGroup + "</span></div>\n" +
            "<div class='detail-row'><span class='detail-label'>Units Needed:</span> <span class='detail-value'>" + units + "</span></div>\n" +
            "<div class='detail-row'><span class='detail-label'>Required Date:</span> <span class='detail-value'>" + requiredDate + "</span></div>\n" +
            (medicalReason != null && !medicalReason.isBlank() ? 
                "<div class='detail-row'><span class='detail-label'>Medical Reason:</span> <span class='detail-value'>" + medicalReason + "</span></div>\n" : "") +
            "</div>\n" +
            "<p><strong>What's Next?</strong></p>\n" +
            "<p>Please contact the patient directly at <strong>" + requesterEmail + "</strong> to confirm your donation or for more information. Your donation could save a life!</p>\n" +
            "<div class='action'>\n" +
            "<a href='mailto:" + requesterEmail + "?subject=Blood Donation Match - Request ID " + requestId + "' class='btn'>Respond to Requester</a>\n" +
            "</div>\n" +
            "<p style='color: #666; font-size: 14px;'>Thank you for being a lifesaver! ❤️</p>\n" +
            "</div>\n" +
            "<div class='footer'>\n" +
            "<p><strong>CloudCare Team</strong></p>\n" +
            "<p>Connecting Donors with Lives</p>\n" +
            "</div>\n" +
            "</div>\n" +
            "</body>\n" +
            "</html>";
    }
}
