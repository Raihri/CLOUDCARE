package com.example.cloud.care.controller;
import com.example.cloud.care.model.Patient;
import com.example.cloud.care.model.User;
import com.example.cloud.care.repository.PatientRepository;
import com.example.cloud.care.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
@Controller
@RequestMapping("/patient")
public class PatientDashboardController {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private UserService userService;  // <-- inject UserService

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Get logged-in user's email
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        // Fetch user using Optional
        User user = userService.findByEmail(email)
            .orElse(null);

        if (user == null || user.getPatient() == null) {
            return "redirect:/patient/"; // not found
        }

        model.addAttribute("patient", user.getPatient()); // fixed variable

        return "what"; // your Thymeleaf page
    }
}
