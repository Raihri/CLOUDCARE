package com.example.cloud.care.controller;

import com.example.cloud.care.model.Donor;
import com.example.cloud.care.service.DonorService;
import com.example.cloud.care.service.EmailServiceDonor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/donor")
public class DonorController {

    @Autowired
    private DonorService donorService;

    @Autowired
    private EmailServiceDonor emailServiceDonor;
    
    @Autowired
    private com.example.cloud.care.service.RequestService requestService;

    public DonorController(DonorService donorService) {
        this.donorService = donorService;
    }

    // Simple HTML-escape helper used for constructing email bodies
    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }

    @GetMapping("")
    public String donorDashboard() {
        return "donor_dashboard";
    }

    @GetMapping("/email-select")
    public String showEmailSelectForm(Model model, @RequestParam(required = false) String loginEmail) {
        model.addAttribute("loginEmail", loginEmail);
        return "donor_email_select";
    }

    @PostMapping("/email-select")
    public String processEmailSelect(@RequestParam String loginEmail,
                                    @RequestParam(required = false) String newEmail,
                                    Model model) {
        String selectedEmail = (newEmail != null && !newEmail.isBlank()) ? newEmail : loginEmail;
        model.addAttribute("selectedEmail", selectedEmail);
        model.addAttribute("msg", "Selected email: " + selectedEmail + " will be used for all donor communications.");
        return "donor_email_select";
    }

    // REST API Endpoints for Pop-up Form
    @PostMapping("/api/create")
    @ResponseBody
    public ResponseEntity<?> createDonor(@RequestBody Donor donor) {
        try {
            // Validate required fields
            if (donor.getName() == null || donor.getName().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Name is required"));
            }
            if (donor.getBloodGroup() == null || donor.getBloodGroup().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Blood group is required"));
            }
            if (donor.getAge() < 18) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Age must be at least 18"));
            }
            if (donor.getLastDonated() != null && donor.getLastDonated().isAfter(LocalDate.now())) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Last donated date cannot be in the future"));
            }

            // Eligibility check
            Map<String, Object> eligibility = new HashMap<>();
            eligibility.put("eligible", true);
            java.util.List<String> reasons = new java.util.ArrayList<>();
            
            // Check age
            if (donor.getAge() < 18) {
                eligibility.put("eligible", false);
                reasons.add("Age must be at least 18");
            }
            
            // Check last donated (must be > 3 months ago for eligibility)
            if (donor.getLastDonated() != null) {
                LocalDate threeMonthsAgo = LocalDate.now().minusMonths(3);
                if (donor.getLastDonated().isAfter(threeMonthsAgo)) {
                    eligibility.put("eligible", false);
                    reasons.add("Must wait at least 3 months since last donation");
                }
            }
            
            // Check for critical diseases (level 3 = HIV)
            if (donor.getDiseases() != null && !donor.getDiseases().isEmpty()) {
                for (com.example.cloud.care.model.Disease disease : donor.getDiseases()) {
                    if (disease.getLevel() >= 3 || "HIV".equalsIgnoreCase(disease.getName())) {
                        eligibility.put("eligible", false);
                        reasons.add("Cannot donate due to: " + disease.getName());
                        break;
                    }
                }
            }
            
            eligibility.put("reasons", reasons);

            // Set default status
            donor.setStatus("Pending");

            // Save donor
            com.example.cloud.care.model.Donor savedDonor = donorService.saveDonor(donor);

            // Send email notification if email provided
            if (donor.getEmail() != null && !donor.getEmail().isEmpty()) {
                String eligibilityText = (boolean)eligibility.get("eligible") ? 
                    "You are <strong>ELIGIBLE</strong> to donate blood." : 
                    "You are <strong>NOT CURRENTLY ELIGIBLE</strong> to donate blood.";
                
                String reasonsText = "";
                if (!reasons.isEmpty()) {
                    reasonsText = "<p><strong>Details:</strong><ul>";
                    for (String reason : reasons) {
                        reasonsText += "<li>" + reason + "</li>";
                    }
                    reasonsText += "</ul></p>";
                }
                
                String emailBody = "<div style=\"font-family:Arial,Helvetica,sans-serif;color:#333;line-height:1.4;\">"
                    + "<table width=\"100%\" cellpadding=0 cellspacing=0 style=\"max-width:600px;margin:0 auto;background:#ffffff;border-radius:8px;overflow:hidden;border:1px solid #eee;\">"
                    + "<tr><td style=\"background:linear-gradient(90deg,#d32f2f,#ef5350);padding:18px 20px;color:#fff;text-align:center;\">"
                    + "<h2 style=\"margin:0;font-family:Arial,Helvetica,sans-serif;\">Thank you for registering as a blood donor!</h2></td></tr>"
                    + "<tr><td style=\"padding:18px 20px;\">"
                    + "<p style=\"margin:0 0 10px 0;\">Dear <strong>" + escapeHtml(donor.getName()) + "</strong>,</p>"
                    + "<p style=\"margin:0 0 16px 0;color:#555;\">We have received your donor profile. Below are the details we have recorded:</p>"
                    + "<table width=\"100%\" cellpadding=6 cellspacing=0 style=\"border-collapse:collapse;background:#fafafa;border-radius:6px;\">"
                    + "<tr><td style=\"width:40%;font-weight:600;color:#333;border-bottom:1px solid #eee;\">Blood Group</td><td style=\"border-bottom:1px solid #eee;\">" + escapeHtml(donor.getBloodGroup()) + "</td></tr>"
                    + "<tr><td style=\"font-weight:600;color:#333;border-bottom:1px solid #eee;\">Status</td><td style=\"border-bottom:1px solid #eee;\">" + escapeHtml(donor.getStatus()) + "</td></tr>"
                    + "<tr><td style=\"font-weight:600;color:#333;border-bottom:1px solid #eee;\">Age</td><td style=\"border-bottom:1px solid #eee;\">" + donor.getAge() + "</td></tr>"
                    + "<tr><td style=\"font-weight:600;color:#333;\">Contact</td><td>" + (donor.getContact() != null ? escapeHtml(donor.getContact()) : "(not provided)") + "</td></tr>"
                    + "</table>"
                    + "<p style=\"margin:14px 0 6px 0;color:#333;\">" + eligibilityText + "</p>"
                    + (reasonsText != null && !reasonsText.isBlank() ? ("<div style=\"padding:8px 12px;margin:10px 0;border-left:4px solid #f5c6cb;background:#fff7f7;color:#7b1f1f;\">" + reasonsText + "</div>") : "")
                    + "<div style=\"margin-top:18px;text-align:center;\">"
                    + "<a href=\"mailto:" + (donor.getEmail() != null ? donor.getEmail() : "") + "\" style=\"display:inline-block;padding:10px 16px;border-radius:6px;background:linear-gradient(90deg,#d32f2f,#c62828);color:#fff;text-decoration:none;font-weight:700;\">Contact Preferences</a>"
                    + "</div>"
                    + "</td></tr>"
                    + "<tr><td style=\"padding:12px 20px;font-size:12px;color:#999;text-align:center;background:#fafafa;\">Thank you for supporting the community — CloudCare</td></tr>"
                    + "</table></div>";
                emailServiceDonor.sendHtmlEmailAsync(donor.getEmail(), "Blood Donor Registration Confirmation", emailBody);
            }

            // If donor is eligible, try to match with pending requests of the same blood group
            java.util.List<com.example.cloud.care.model.Request> matched = new java.util.ArrayList<>();
            try {
                boolean isEligible = eligibility.get("eligible") instanceof Boolean && (Boolean) eligibility.get("eligible");
                if (isEligible && donor.getBloodGroup() != null && !donor.getBloodGroup().isEmpty()) {
                    String donorBg = donor.getBloodGroup().trim();
                    java.util.List<com.example.cloud.care.model.Request> requests = requestService.getAllRequests();
                    for (com.example.cloud.care.model.Request req : requests) {
                        if (req == null) continue;
                        String reqStatus = req.getStatus();
                        String reqBg = req.getBloodGroup();
                        if ((reqStatus == null || reqStatus.equalsIgnoreCase("Pending")) && reqBg != null
                                && reqBg.equalsIgnoreCase(donorBg)) {
                            // build email to requester with donor details and contact link
                            String donorEmailLink = (donor.getEmail() != null && !donor.getEmail().isEmpty()) ?
                                    "<a href=\"mailto:" + donor.getEmail() + "\">" + donor.getEmail() + "</a>" : "(no email)";
                            String donorPhone = donor.getContact() != null ? donor.getContact() : "(no contact)";
                            String donorLast = donor.getLastDonated() != null ? donor.getLastDonated().toString() : "(not provided)";

                                String matchBody = "<div style=\"font-family:Arial,Helvetica,sans-serif;color:#333;line-height:1.4;\">"
                                    + "<table width=\"100%\" cellpadding=0 cellspacing=0 style=\"max-width:600px;margin:0 auto;background:#ffffff;border-radius:8px;overflow:hidden;border:1px solid #eee;\">"
                                    + "<tr><td style=\"background:linear-gradient(90deg,#1976d2,#42a5f5);padding:18px 20px;color:#fff;text-align:center;\">"
                                    + "<h2 style=\"margin:0;font-family:Arial,Helvetica,sans-serif;\">A matching donor is available</h2></td></tr>"
                                    + "<tr><td style=\"padding:18px 20px;\">"
                                    + "<p style=\"margin:0 0 10px 0;\">Dear <strong>" + escapeHtml(req.getName()) + "</strong>,</p>"
                                    + "<p style=\"margin:0 0 12px 0;color:#555;\">We have found a registered donor whose blood group matches your request. You can contact the donor directly using the contact details below.</p>"
                                    + "<table width=\"100%\" cellpadding=6 cellspacing=0 style=\"border-collapse:collapse;background:#fafafa;border-radius:6px;\">"
                                    + "<tr><td style=\"width:40%;font-weight:600;color:#333;border-bottom:1px solid #eee;\">Name</td><td style=\"border-bottom:1px solid #eee;\">" + escapeHtml(donor.getName()) + "</td></tr>"
                                    + "<tr><td style=\"font-weight:600;color:#333;border-bottom:1px solid #eee;\">Blood Group</td><td style=\"border-bottom:1px solid #eee;\">" + escapeHtml(donor.getBloodGroup()) + "</td></tr>"
                                    + "<tr><td style=\"font-weight:600;color:#333;border-bottom:1px solid #eee;\">Age</td><td style=\"border-bottom:1px solid #eee;\">" + donor.getAge() + "</td></tr>"
                                    + "<tr><td style=\"font-weight:600;color:#333;border-bottom:1px solid #eee;\">Contact</td><td style=\"border-bottom:1px solid #eee;\">" + escapeHtml(donorPhone) + "</td></tr>"
                                    + "<tr><td style=\"font-weight:600;color:#333;border-bottom:1px solid #eee;\">Last Donated</td><td style=\"border-bottom:1px solid #eee;\">" + escapeHtml(donorLast) + "</td></tr>"
                                    + "<tr><td style=\"font-weight:600;color:#333;\">Email</td><td>" + donorEmailLink + "</td></tr>"
                                    + "</table>"
                                    + "<div style=\"margin-top:16px;text-align:center;\">"
                                    + "<a href=\"mailto:" + (donor.getEmail() != null ? donor.getEmail() : "") + "\" style=\"display:inline-block;padding:10px 16px;border-radius:6px;background:linear-gradient(90deg,#1976d2,#42a5f5);color:#fff;text-decoration:none;font-weight:700;\">Contact Donor</a>"
                                    + "</div>"
                                    + "</td></tr>"
                                    + "<tr><td style=\"padding:12px 20px;font-size:12px;color:#999;text-align:center;background:#fafafa;\">This request has been marked as <strong>Matched</strong> in the CloudCare system.</td></tr>"
                                    + "</table></div>";

                            try {
                                if (req.getEmail() != null && !req.getEmail().isEmpty()) {
                                    emailServiceDonor.sendHtmlEmailAsync(req.getEmail(), "Matched Donor Available for your Request", matchBody);
                                }
                            } catch (Exception e) {
                                // log and continue
                                System.err.println("Failed to send matching email to request " + req.getId() + ": " + e.getMessage());
                            }

                            // mark request as matched and save
                            req.setStatus("Matched");
                            requestService.saveRequest(req);
                            matched.add(req);
                        }
                    }
                }
            } catch (Exception matchEx) {
                System.err.println("Error while matching donor to requests: " + matchEx.getMessage());
                matchEx.printStackTrace();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Donor registered successfully");
            response.put("donor", savedDonor);
            response.put("eligibility", eligibility);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", "Error creating donor: " + e.getMessage()));
        }
    }

    @GetMapping("/api/list")
    @ResponseBody
    public ResponseEntity<?> getAllDonors() {
        try {
            List<Donor> donors = donorService.getAllDonors();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "donors", donors,
                    "count", donors.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error fetching donors: " + e.getMessage()));
        }
    }

    @GetMapping("/api/list/{id}")
    @ResponseBody
    public ResponseEntity<?> getDonorById(@PathVariable Long id) {
        try {
            Donor donor = donorService.getDonor(id);
            if (donor == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Donor not found"));
            }
            return ResponseEntity.ok(Map.of("success", true, "data", donor));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error fetching donor: " + e.getMessage()));
        }
    }

    @GetMapping("/api/get/{id}")
    @ResponseBody
    public ResponseEntity<?> getDonorByIdSimple(@PathVariable Long id) {
        try {
            Donor donor = donorService.getDonor(id);
            if (donor == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Donor not found"));
            }
            return ResponseEntity.ok(Map.of("success", true, "data", donor));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error fetching donor: " + e.getMessage()));
        }
    }

    @PutMapping("/api/update/{id}")
    @ResponseBody
    public ResponseEntity<?> updateDonor(@PathVariable Long id, @RequestBody Donor donorDetails) {
        try {
            Donor donor = donorService.getDonor(id);
            if (donor == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Donor not found"));
            }

            // Update fields
            if (donorDetails.getName() != null && !donorDetails.getName().isEmpty()) donor.setName(donorDetails.getName());
            if (donorDetails.getAge() > 0) donor.setAge(donorDetails.getAge());
            if (donorDetails.getGender() != null && !donorDetails.getGender().isEmpty()) donor.setGender(donorDetails.getGender());
            if (donorDetails.getBloodGroup() != null && !donorDetails.getBloodGroup().isEmpty()) donor.setBloodGroup(donorDetails.getBloodGroup());
            if (donorDetails.getContact() != null && !donorDetails.getContact().isEmpty()) donor.setContact(donorDetails.getContact());
            if (donorDetails.getLastDonated() != null) donor.setLastDonated(donorDetails.getLastDonated());
            if (donorDetails.getEmail() != null && !donorDetails.getEmail().isEmpty()) donor.setEmail(donorDetails.getEmail());
            if (donorDetails.getDiseases() != null) donor.setDiseases(donorDetails.getDiseases());
            if (donorDetails.getStatus() != null && !donorDetails.getStatus().isEmpty()) donor.setStatus(donorDetails.getStatus());

            Donor updatedDonor = donorService.saveDonor(donor);
            return ResponseEntity.ok(Map.of("success", true, "message", "Donor updated", "donor", updatedDonor));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error updating donor: " + e.getMessage()));
        }
    }

    @DeleteMapping("/api/delete/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteDonor(@PathVariable Long id) {
        try {
            Donor donor = donorService.getDonor(id);
            if (donor == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Donor not found"));
            }
            donorService.deleteDonor(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Donor deleted"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error deleting donor: " + e.getMessage()));
        }
    }

    // View request details page for donors
    @GetMapping("/view-request/{id}")
    public String viewRequestDetails(@PathVariable Long id, Model model) {
        com.example.cloud.care.model.Request request = requestService.getRequest(id);
        if (request == null) {
            model.addAttribute("error", "Request not found");
            return "error_page";
        }
        model.addAttribute("request", request);
        model.addAttribute("today", LocalDate.now());
        return "donor_view_request";
    }
    
    // Handle donor response (confirm or ignore)
    @PostMapping("/response")
    public String processDonorResponse(@RequestParam Long requestId,
                                       @RequestParam String action,
                                       Model model) {
        com.example.cloud.care.model.Request request = requestService.getRequest(requestId);
        if (request == null) {
            model.addAttribute("error", "Request not found");
            return "error_page";
        }

        if ("confirm".equalsIgnoreCase(action)) {
            request.setStatus("Confirmed");
            requestService.saveRequest(request);
            model.addAttribute("msg", "Thank you — your confirmation has been recorded.");
        } else if ("ignore".equalsIgnoreCase(action)) {
            // Keep as Pending; optionally record ignore in logs
            model.addAttribute("msg", "You have chosen to ignore this request.");
        } else {
            model.addAttribute("msg", "Unknown action");
        }

        model.addAttribute("request", request);
        return "donor_response";
    }
    @GetMapping("/form")
public String donorForm(@RequestParam(required = false) Long id, Model model) {

    Donor donor;

    if (id != null) {
        donor = donorService.getDonor(id);
        if (donor == null) {
            donor = new Donor();
        }
    } else {
        donor = new Donor();
    }

    model.addAttribute("donor", donor);

    model.addAttribute("genders", List.of("Male", "Female", "Other"));
    model.addAttribute("bloodGroups", List.of("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"));
    model.addAttribute("statuses", List.of("Available", "Not Available", "Pending"));

    return "donor_form";
}

}
