package com.example.cloud.care.controller;

import com.example.cloud.care.service.doctor_service;
import com.example.cloud.care.model.Doctor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class admin_controller {

    @Autowired
    private doctor_service doctorService;

    // Show all pending doctors
    @GetMapping("/pending-doctors")
    public String showPendingDoctors(Model model) {
        model.addAttribute("doctors", doctorService.getPendingDoctors());
        return "admin_pending_doctors"; // Thymeleaf template
    }

    // Approve a doctor
    @PostMapping("/approve-doctor")
    public String approveDoctor(@RequestParam Long doctorId) {
        doctorService.approveDoctor(doctorId);
        return "redirect:/admin/pending-doctors";
    }
    @PostMapping("/doctor/reject")
    public String rejectDoctor(@RequestParam Long doctorId, @RequestParam String reason) {
    doctorService.rejectDoctor(doctorId, reason); // <-- email is sent here
    doctorService.deleteDoctor(doctorId); // <-- doctor is deleted here
    return "redirect:/admin/pending-doctors";
}
}