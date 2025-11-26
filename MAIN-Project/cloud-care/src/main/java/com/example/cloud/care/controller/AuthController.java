package com.example.cloud.care.controller;

import com.example.cloud.care.model.User;
import com.example.cloud.care.repository.UserRepository;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.cloud.care.model.ForgotPasswordRequest;
import com.example.cloud.care.model.OtpForm;
import com.example.cloud.care.service.EmailService;
import com.example.cloud.care.service.UserService;
import com.example.cloud.care.service.doctor_service;
import com.example.cloud.care.service.patient_service;
import com.example.cloud.care.model.Patient;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.method.P;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/patient")
public class AuthController {
    @Autowired
    private com.example.cloud.care.service.CustomUserDetailsService customUserDetailsService;
    // Remove field injection for session; use method parameter instead

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;
    private final Cloudinary cloudinary;
    private final patient_service patientService;
    private final EmailService emailService;

    public AuthController(UserService userService, patient_service patientService, EmailService emailService,
            Cloudinary cloudinary) {
        this.userService = userService;
        this.patientService = patientService;
        this.emailService = emailService;
        this.cloudinary = cloudinary;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("user", new User());
        return "index"; // main sign-in/sign-up page
    }

    @PostMapping("/register")
    public String register(@ModelAttribute User user, Model model) {
        try {
            // Validate email domain
            if (!emailService.isDomainValid(user.getEmail().substring(user.getEmail().indexOf('@') + 1))) {
                model.addAttribute("errorMessage",
                        "Invalid email domain. Please use a valid domain with MX/A records.");
                return "index";
            }

            userService.registerUser(user);

            // Create Patient linked to User
            Patient patient = new Patient();
            patient.setUser(user);
            user.setPatient(patient); // link both sides
            patientService.save(patient); // save patient

            // Redirect to verification page with email param
            return "redirect:/patient/verify?email=" + user.getEmail();
        } catch (Exception e) {
            // If the email already exists but user is not enabled, do NOT resend code if
            // registration already sent one
            if (e.getMessage() != null && e.getMessage().contains("Email already in use.")) {
                try {
                    java.util.Optional<User> existing = userService.findByEmailOptional(user.getEmail().trim());
                    if (existing.isPresent() && !existing.get().isEnabled()) {
                        // Redirect to forgot password page for unverified user
                        return "redirect:/patient/forgot-password?email=" + user.getEmail();
                    }
                } catch (Exception ignored) {
                    // fall through to show original error
                }
            }
            model.addAttribute("errorMessage", e.getMessage());
            return "index";
        }
    }

    @PostMapping("/otpverify")
    public String verifyCode(@RequestParam String email, @ModelAttribute OtpForm otpForm, Model model,
            jakarta.servlet.http.HttpServletRequest request) {
        String code = buildOtpCode(otpForm);
        if (code == null) {
            model.addAttribute("errorMessage", "Please enter the full 6-digit code.");
            model.addAttribute("email", email);
            model.addAttribute("otpForm", otpForm);
            return "otp";
        }

        boolean verified = userService.verify(email, code);

        if (verified) {
            Optional<User> optionalUser = userService.findByEmail(email);
            if (optionalUser.isEmpty()) {
                model.addAttribute("errorMessage", "User not found!");
                return "index";
            }
            User verifiedUser = optionalUser.get();
            verifiedUser.setEnabled(true);
            userService.saveUser(verifiedUser);

            // Authenticate properly
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null,
                    userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authToken);

            // Persist security context in session so authentication survives redirect
            try {
                jakarta.servlet.http.HttpSession session = request.getSession(true);
                session.setAttribute(
                        org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                        SecurityContextHolder.getContext());
            } catch (Exception ex) {
                logger.warn("Failed to store security context in session: {}", ex.getMessage());
            }

            return "redirect:/patient/selfie-upload"; // no userId
        } else {
            model.addAttribute("errorMessage", "Invalid or expired verification code!");
            model.addAttribute("email", email);
            model.addAttribute("otpForm", otpForm);
            return "otp";
        }
    }

    // Corrected route to match the redirect from /register
    @GetMapping("/verify")
    public String showVerificationPage(@RequestParam String email, Model model) {
        model.addAttribute("email", email);
        model.addAttribute("otpForm", new OtpForm());
        return "otp"; // OTP verification page
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        SecurityContextLogoutHandler securityContextLogoutHandler = new SecurityContextLogoutHandler();
        // Invalidate the session and clear authentication
        securityContextLogoutHandler.logout(request, response, null);
        return "redirect:/patient/";
    }

    @GetMapping("/selfie-upload")
    public String selfiePage(Model model) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails userDetails) {
            Optional<User> optionalUser = userService.findByEmail(userDetails.getUsername());
            if (optionalUser.isEmpty()) {
                model.addAttribute("errorMessage", "User not found!");
                return "index";
            }
            model.addAttribute("patient", optionalUser.get());
            return "selfieupload";
        } else {
            return "redirect:/patient/"; // Not authenticated
        }
    }

    // Show the forgot-password form
    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        // ensure a model attribute if template expects any
        // model.addAttribute("forgotPasswordRequest", 100);
        return "forgot_password";
    }

    @PostMapping("/forgot-password-submit")
    public String handleForgotPassword(@ModelAttribute("forgotPasswordRequest") ForgotPasswordRequest request,
            Model model) throws Exception {
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

    @PostMapping("/login")
    public String login(@ModelAttribute User userForm,
            HttpSession session,
            Model model) {

        Optional<User> optionalUser = userService.authenticate(userForm.getEmail(), userForm.getPassword());

        if (optionalUser.isEmpty()) {
            // Redirect to home with error param so index.html can show error
            return "redirect:/?error";
        }

        User user = optionalUser.get();

        if (!user.isEnabled()) {
            return "redirect:/patient/verify?email=" + user.getEmail();
        }

        // Now find if this user is a Patient
        Optional<Patient> patient = patientService.findById(user.getId());
        if (patient.isPresent()) {
            session.setAttribute("loggedPatientId", patient.get().getId());
            session.setAttribute("role", "PATIENT");
            return "redirect:/patient/dashboard";
        }

        // If not patient, maybe doctor or admin
        session.setAttribute("loggedUserId", user.getId());

        return "dashboard";
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

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam(required = false) String email,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            Model model) {
        // Validate email
        if (email == null || email.trim().isEmpty()) {
            model.addAttribute("error", "Email information missing. Please try again.");
            return "reset_password";
        }

        // Validate passwords match
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match!");
            model.addAttribute("email", email);
            return "reset_password";
        }

        try {
            // Reset the password
            userService.resetPassword(email.trim(), password);
            logger.info("Password successfully reset for user: {}", email);

            // Authenticate the user automatically after successful reset
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(email.trim());
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null,
                    userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authToken);

            // Redirect to dashboard or home page
            return "redirect:/patient/dashboard";

        } catch (Exception e) {
            logger.error("Error resetting password for {}: {}", email, e.getMessage());
            model.addAttribute("error", "An error occurred while resetting your password. Please try again.");
            model.addAttribute("email", email);
            return "reset_password";
        }
    }

    // Helper: build a 6-digit string from OtpForm, return null if any digit
    // missing/invalid
    private String buildOtpCode(OtpForm otpForm) {
        if (otpForm == null)
            return null;
        String[] parts = { otpForm.getDigit1(), otpForm.getDigit2(), otpForm.getDigit3(), otpForm.getDigit4(),
                otpForm.getDigit5(), otpForm.getDigit6() };
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p == null || p.trim().isEmpty())
                return null;
            String t = p.trim();
            if (t.length() != 1 || !t.matches("\\d"))
                return null;
            sb.append(t);
        }
        return sb.toString();
    }

    @PostMapping("/selfie-upload")
    public String handleSelfieUpload(@RequestParam Long userId,
            @RequestParam(required = false) String base64Image,
            @RequestParam(required = false) String skip,
            HttpSession session, Model model) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (skip != null) {
            user.setPhotoUrl(null);
            user.setEnabled(true);
            userService.saveUser(user);
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getEmail());
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null,
                    userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authToken);
            return "redirect:/patient/dashboard";
        }

        if (base64Image != null && !base64Image.isEmpty()) {
            try {
                String cleanBase64 = base64Image.replaceAll("^data:image/\\w+;base64,", "");
                byte[] imageBytes = Base64.getDecoder().decode(cleanBase64);
                @SuppressWarnings("unchecked")
                Map<String, Object> uploadResult = (Map<String, Object>) cloudinary.uploader().upload(imageBytes,
                        ObjectUtils.asMap("folder", "users", "resource_type", "image"));
                String imageUrl = uploadResult.get("secure_url").toString();
                user.setPhotoUrl(imageUrl);
                user.setEnabled(true);
                userService.saveUser(user);
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getEmail());
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,
                        null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authToken);
                return "redirect:/patient/dashboard";
            } catch (IOException e) {
                model.addAttribute("errorMessage", "Photo upload failed: " + e.getMessage());
                model.addAttribute("userId", userId);
                return "selfieupload";
            }
        }

        model.addAttribute("errorMessage", "Please upload a photo or click skip.");
        model.addAttribute("userId", userId);
        return "selfieupload";
    }
}
