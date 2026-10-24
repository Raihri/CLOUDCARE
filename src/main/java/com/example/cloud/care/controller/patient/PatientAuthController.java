package com.example.cloud.care.controller.patient;

import com.example.cloud.care.model.User;
import com.example.cloud.care.service.StorageService;
import com.example.cloud.care.service.UserService;
import com.example.cloud.care.service.PatientService;
import com.example.cloud.care.model.Patient;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import java.io.IOException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/patient")
public class PatientAuthController {
    @Autowired
    private com.example.cloud.care.service.CustomUserDetailsService customUserDetailsService;
    // Remove field injection for session; use method parameter instead

    private static final Logger logger = LoggerFactory.getLogger(PatientAuthController.class);

    private final UserService userService;
    private final StorageService storageService;
    private final PatientService patientService;

    public PatientAuthController(UserService userService, PatientService patientService, StorageService storageService) {
        this.userService = userService;
        this.patientService = patientService;
        this.storageService = storageService;
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

            // Create Patient linked to User
            Patient patient = new Patient();
            patient.setUser(user);
            user.setPatient(patient); // link both sides
            patientService.save(patient); // save patient

            // No OTP step: the account is active immediately. Send the user to
            // the sign-in page, where the Next.js demo buttons and the normal
            // form both work.
            return "redirect:/patient/login?registered=1";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "index";
        }
    }

    // Path must be "/logout" only — the class-level @RequestMapping("/patient")
    // already supplies the prefix. "/patient/logout" here resolved to
    // /patient/patient/logout and was unreachable.
    @PostMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        SecurityContextLogoutHandler securityContextLogoutHandler = new SecurityContextLogoutHandler();
        // Invalidate the session and clear authentication
        securityContextLogoutHandler.logout(request, response, null);
        System.out.println("User logged out successfully. -----------------------------");
        return "redirect:/patient/"; // Redirect to home page after logout
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

        // No OTP gate: every registered account can sign in immediately.
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
                String imageUrl = storageService.uploadBase64(base64Image, "users");
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
