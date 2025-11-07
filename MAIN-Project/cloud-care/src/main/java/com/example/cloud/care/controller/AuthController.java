package com.example.cloud.care.controller;

import com.example.cloud.care.model.User;
import com.example.cloud.care.model.OtpForm;
import com.example.cloud.care.service.UserService;
import com.example.cloud.care.service.doctor_service;
import com.example.cloud.care.service.patient_service;
import com.example.cloud.care.var.patient;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class AuthController {


    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;
    private final patient_service patientService;

    public AuthController(UserService userService, patient_service patientService) {
        this.userService = userService;
        this.patientService = patientService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("user", new User());
        return "index"; // main sign-in/sign-up page
    }

    @PostMapping("/register")
    public String register(@ModelAttribute User user, Model model) {
        try {
            userService.registerUser(user);
            // Redirect to verification page with email param
            return "redirect:/verify?email=" + user.getEmail();
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "index";
        }
    }

    // Corrected route to match the redirect from /register
    @GetMapping("/verify")
    public String showVerificationPage(@RequestParam String email, Model model) {
        model.addAttribute("email", email);
        model.addAttribute("otpForm", new OtpForm());
        return "otp"; // OTP verification page
    }

    @PostMapping("/otpverify")
    public String verifyCode(@RequestParam String email,
            @ModelAttribute OtpForm otpForm,
            Model model) {

        String code = otpForm.getDigit1() + otpForm.getDigit2() + otpForm.getDigit3() +
                otpForm.getDigit4() + otpForm.getDigit5() + otpForm.getDigit6();

        logger.info("Verifying code for email: {}", email);

        boolean verified = userService.verify(email, code);

        if (verified) {
            logger.info("User {} successfully verified.", email);
            System.out.println("User " + email + " successfully verified.");
            patient newPatient = new patient();
            newPatient.setEmail(email);
            // save via patient service and capture saved entity (to get generated ID)
            patient saved = patientService.savePatient(newPatient);
            if (saved != null) {
                model.addAttribute("toastMessage", "Congratulations, your ID is - " + saved.getPatientId());
            } else {
                model.addAttribute("toastMessage", "Congratulations — patient created.");
            }

            return "dashboard";
        } else {
            logger.warn("Verification failed for user {} with code {}", email, code);
            model.addAttribute("errorMessage", "Invalid or expired verification code!");
            model.addAttribute("email", email);
            model.addAttribute("otpForm", otpForm);
            model.addAttribute("otp", code);
            return "otp";
        }
    }

    @PostMapping("/login")
    public String login(@ModelAttribute User user, Model model) {
        // Authentication is handled by Spring Security
        return "redirect:/dashboard";
    }

    
    

    // // Dev-only debug endpoint
    // @GetMapping("/debug/verification-status")
    // @ResponseBody
    // public ResponseEntity<?> debugVerificationStatus(@RequestParam String email)
    // {
    // return userService.findByEmailOptional(email)
    // .map(u -> ResponseEntity.ok().body(
    // java.util.Map.of(
    // "email", u.getEmail(),
    // "enabled", u.isEnabled(),
    // "verificationCode", u.getVerificationCode()
    // )
    // ))
    // .orElseGet(() -> ResponseEntity.status(404)
    // .body(java.util.Map.of("error", "user not found")));
    // }
}