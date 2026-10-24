package com.example.cloud.care.controller.patient;

import com.example.cloud.care.model.Patient;
import com.example.cloud.care.model.User;
import com.example.cloud.care.service.PatientService;
import com.example.cloud.care.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Patient registration and logout.
 *
 * <p>Sign-in itself is handled entirely by Spring Security's {@code formLogin}
 * (see {@code SecurityConfig.patientSecurity}), which intercepts
 * {@code POST /patient/login} before it would ever reach a controller — so
 * there is deliberately no {@code login()} method here.
 */
@RestController
@RequestMapping("/patient")
public class PatientAuthController {

    private final UserService userService;
    private final PatientService patientService;

    public PatientAuthController(UserService userService, PatientService patientService) {
        this.userService = userService;
        this.patientService = patientService;
    }

    /** Registers and immediately activates a patient account (no email OTP). */
    @PostMapping("/register")
    public Map<String, Object> register(@RequestParam String name,
                                        @RequestParam String email,
                                        @RequestParam String password,
                                        @RequestParam(required = false) String confirmPassword) {
        if (name == null || name.isBlank()) {
            return result(false, "Name is required.");
        }
        if (confirmPassword != null && !password.equals(confirmPassword)) {
            return result(false, "Passwords do not match.");
        }

        try {
            User user = new User();
            user.setName(name);
            user.setEmail(email);
            user.setPassword(password);
            userService.registerUser(user);

            Patient patient = new Patient();
            patient.setUser(user);
            user.setPatient(patient);
            patientService.save(patient);

            return result(true, "Account created. You can now sign in.");
        } catch (Exception e) {
            return result(false, e.getMessage());
        }
    }

    @PostMapping("/logout")
    @ResponseBody
    public Map<String, Object> logout(HttpServletRequest request, HttpServletResponse response) {
        new SecurityContextLogoutHandler().logout(request, response, null);
        return result(true, "Signed out.");
    }

    private static Map<String, Object> result(boolean ok, String message) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("success", ok);
        out.put("message", message);
        return out;
    }
}
