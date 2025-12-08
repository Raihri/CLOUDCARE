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

    // Approve a doctor via AJAX
    @PostMapping("/approve-doctor")
@ResponseBody
public ResponseEntity<Map<String, Object>> approveDoctor(@RequestParam Long doctorId) {
    Map<String, Object> response = new HashMap<>();

    try {
        // Find the doctor by ID
        Optional<Doctor> optDoc = doctordao.findById(doctorId);
        if (optDoc.isEmpty()) {
            response.put("success", false);
            response.put("message", "Doctor not found");
            return ResponseEntity.ok(response);
        }

        Doctor doc = optDoc.get();
        doc.setStatus(Doctor.Status.APPROVED);
        doctordao.save(doc); // Save the updated status

        // Send approval email
        String subject = "Your Doctor Account Has Been Approved!";
        String body = "Hello " + doc.getName() + ",\n\nYour account has been approved. You can now log in.\n\nThanks!";
        emailService.sendEmail(doc.getEmail(), subject, body);

        response.put("success", true);
        response.put("message", "Doctor approved successfully and email sent");
        return ResponseEntity.ok(response);

    } catch (Exception e) {
        response.put("success", false);
        response.put("message", "Error approving doctor: " + e.getMessage());
        return ResponseEntity.status(500).body(response);
    }
}

    // Reject a doctor via AJAX
    @PostMapping("/reject-doctor")
@ResponseBody
public Map<String, Object> rejectDoctor(@RequestParam long doctorId, @RequestParam String reason) {
    Map<String, Object> response = new HashMap<>();

    Optional<Doctor> optDoc = doctordao.findById(doctorId);
if(optDoc.isEmpty()){
    response.put("success", false);
    response.put("message", "Doctor not found");
    return response;
}
Doctor doc = optDoc.get();

     
    doc.setStatus(Status.REJECTED);
    doctordao.save(doc);       // save the status change

    // Send email asynchronously
    emailService.sendEmail(doc.getEmail(), "Your signup was rejected", reason);

    response.put("success", true);
    response.put("message", "Doctor rejected successfully");
    return response;
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