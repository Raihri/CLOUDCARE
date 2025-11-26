package com.example.cloud.care.controller;

import com.example.cloud.care.service.doctor_service;
import com.example.cloud.care.model.Doctor;
import com.example.cloud.care.service.DoctorUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
@RequestMapping("/doctor")
public class doctor_controller {

    private final doctor_service doctorService;

    @Autowired
    public doctor_controller(doctor_service doctorService) {
        this.doctorService = doctorService;
    }

    // Show signup form
    @GetMapping("/signup")
    public String showSignupForm(Model model) {
        model.addAttribute("doctor", new Doctor());
        return "doctor_signup"; // Thymeleaf template
    }

    // Handle signup request
    @PostMapping("/signup/request")
    public String handleSignupRequest(@ModelAttribute Doctor doctorRequest,
                                      @RequestParam("profileImageFile") MultipartFile profileImage,
                                      @RequestParam("certificateFile") MultipartFile certificateFile,
                                      Model model) {
        try {
            doctorService.saveSignupRequest(doctorRequest, profileImage, certificateFile);
            model.addAttribute("success", "Signup request submitted! Wait for approval.");
            return "redirect:/doctor/login?signupSuccess=true";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "doctor_signup";
        } catch (IOException e) {
            model.addAttribute("error", "File upload failed: " + e.getMessage());
            return "doctor_signup";
        }
    }

    // Show login page
    @GetMapping("/login")
    public String showLoginForm() {
        return "doctor_login"; // Thymeleaf template
    }

    // Dashboard page - only accessible for authenticated & approved doctors
    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return "redirect:/doctor/login";
        }

        DoctorUserDetails userDetails = (DoctorUserDetails) auth.getPrincipal();
        Doctor loggedInDoctor = userDetails.getDoctor();
        model.addAttribute("doctor", loggedInDoctor);
        return "doctor_dashboard"; // Thymeleaf template showing doctor info
    }

    // AJAX: check if email exists
    @GetMapping("/check-email")
    @ResponseBody
    public boolean checkEmail(@RequestParam String email) {
        return doctorService.emailExists(email);
    }
    @GetMapping("/check-bmdc")
@ResponseBody
public boolean checkBmdc(@RequestParam String bmdc) {
    return doctorService.bmdcExists(bmdc);
}
    // Logout is handled by Spring Security automatically via /doctor/logout
}