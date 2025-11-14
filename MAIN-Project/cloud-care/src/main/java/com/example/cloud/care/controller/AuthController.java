package com.example.cloud.care.controller;

import com.example.cloud.care.model.User;
import com.example.cloud.care.model.ForgotPasswordRequest;
import com.example.cloud.care.model.OtpForm;
import com.example.cloud.care.service.EmailService;
import com.example.cloud.care.service.UserService;
import com.example.cloud.care.service.doctor_service;
import com.example.cloud.care.service.patient_service;
import com.example.cloud.care.var.patient;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.method.P;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private  UserService userService;
    private final patient_service patientService;
    private final EmailService emailService;

    public AuthController(UserService userService, patient_service patientService, EmailService emailService) {
        this.userService = userService;
        this.patientService = patientService;
        this.emailService = emailService;
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
        String code = buildOtpCode(otpForm);
        if (code == null) {
            model.addAttribute("errorMessage", "Please enter the full 6-digit code.");
            model.addAttribute("email", email);
            model.addAttribute("otpForm", otpForm);
            return "otp";
        }

        logger.info("Verifying code for email: {} provided={}", email, code);

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

    @PostMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        SecurityContextLogoutHandler securityContextLogoutHandler = new SecurityContextLogoutHandler();
        // Invalidate the session and clear authentication
        securityContextLogoutHandler.logout(request, response, null);
        return "redirect:/";
    }


    // Show the forgot-password form
    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        // ensure a model attribute if template expects any
        // model.addAttribute("forgotPasswordRequest", 100);
        return "forgot_password";
    }

    @PostMapping("/forgot-password-submit")
    public String handleForgotPassword(@ModelAttribute("forgotPasswordRequest") ForgotPasswordRequest request, Model model) throws Exception {
        String email = request.getEmail();

        if (email == null || email.trim().isEmpty()) {
            model.addAttribute("error", "Please provide a valid email address.");
            return "forgot_password";
        }

        try {
            // Generate and send verification code
            userService.sendPasswordResetCode(email.trim());
            logger.info("Password reset code sent to {}", email);

            // Show OTP page to enter the code
            model.addAttribute("email", email.trim());
            model.addAttribute("otpForm", new OtpForm());
            model.addAttribute("success", "Verification code sent to " + email.trim() + ". Check your inbox.");
            return "otp_reset";
        } catch (Exception e) {
            logger.error("Failed to initiate password reset for {}: {}", email, e.getMessage());
            model.addAttribute("error", e.getMessage() == null ? "Unable to process request." : e.getMessage());
            return "forgot_password";
        }
    


}

    @GetMapping("/reset-otp")
    public String showResetOtpForm(@RequestParam String email, Model model) {
        model.addAttribute("email", email);
        model.addAttribute("otpForm", new OtpForm());
        return "otp_reset";
    }

    @PostMapping("/reset-otp-verify")
    public String verifyResetCode(@RequestParam String email,
            @ModelAttribute OtpForm otpForm,
            Model model) {
        String code = buildOtpCode(otpForm);
        if (code == null) {
            model.addAttribute("errorMessage", "Please enter the full 6-digit code.");
            model.addAttribute("email", email);
            model.addAttribute("otpForm", new OtpForm());
            return "otp_reset";
        }

        logger.info("Verifying reset code for email: {} provided={}", email, code);

        boolean verified = userService.verify(email, code);
        if (verified) {
            logger.info("User {} successfully verified for password reset.", email);
            System.out.println("User " + email + " successfully verified for password reset.");
            model.addAttribute("email", email); // Add email to model so reset_password.html can use it
            return "reset_password"; // Proceed to reset password page
        } else {
            logger.warn("Verification failed for user {} with code {}", email, code);
            model.addAttribute("errorMessage", "Invalid or expired verification code!");
            model.addAttribute("email", email);
            model.addAttribute("otpForm", new OtpForm()); // Reset form for retry
        }
        return "otp_reset";
    }

    // Handle password reset submission
    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam(required = false) String email,
                                @RequestParam String password,
                                @RequestParam String confirmPassword,
                                Model model) {
        // Validate email is provided
        if (email == null || email.trim().isEmpty()) {
            model.addAttribute("error", "Email information missing. Please try again.");
            return "reset_password";
        }

        // Validate passwords match (frontend already validates, but check server-side too)
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match!");
            model.addAttribute("email", email);
            return "reset_password";
        }

        try {
            // Delegate password reset logic to UserService
            userService.resetPassword(email.trim(), password);
            logger.info("Password successfully reset for user: {}", email);
            return "redirect:/?passwordReset=success";
        } catch (Exception e) {
            logger.error("Error resetting password for {}: {}", email, e.getMessage());
            model.addAttribute("error", "An error occurred while resetting your password. Please try again.");
            model.addAttribute("email", email);
            return "reset_password";
        }
    }

    // Helper: build a 6-digit string from OtpForm, return null if any digit missing/invalid
    private String buildOtpCode(OtpForm otpForm) {
        if (otpForm == null) return null;
        String[] parts = {otpForm.getDigit1(), otpForm.getDigit2(), otpForm.getDigit3(), otpForm.getDigit4(), otpForm.getDigit5(), otpForm.getDigit6()};
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p == null || p.trim().isEmpty()) return null;
            String t = p.trim();
            if (t.length() != 1 || !t.matches("\\d")) return null;
            sb.append(t);
        }
        return sb.toString();
    }
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
