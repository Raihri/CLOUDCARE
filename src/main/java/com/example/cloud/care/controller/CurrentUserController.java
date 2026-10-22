package com.example.cloud.care.controller;

import com.example.cloud.care.dao.AdminDao;
import com.example.cloud.care.model.Doctor;
import com.example.cloud.care.model.Patient;
import com.example.cloud.care.service.DoctorUserDetails;
import com.example.cloud.care.service.patient_service;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Role-agnostic identity endpoint for the Next.js frontend.
 *
 * Lives at /api/me (not /patient/current) because it must serve all three
 * roles — the three SecurityFilterChains cover /patient/**, /doctor/** and
 * /admin/** separately, but the public catch-all chain permits /api/me for
 * every one of them. An authenticated request falls through to here; an
 * anonymous one gets 401, which the frontend reads as "logged out".
 *
 * This is the frontend's single bootstrap call: without it Next.js cannot tell
 * who is signed in, so every page guard and sidebar depends on it.
 */
@RestController
public class CurrentUserController {

    private final patient_service patientService;
    private final DoctorUserDetailsLookup doctorLookup;
    private final AdminDao adminDao;

    public CurrentUserController(patient_service patientService,
                                 DoctorUserDetailsLookup doctorLookup,
                                 AdminDao adminDao) {
        this.patientService = patientService;
        this.doctorLookup = doctorLookup;
        this.adminDao = adminDao;
    }

    @GetMapping("/api/me")
    public ResponseEntity<Map<String, Object>> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Spring installs an AnonymousAuthenticationToken for unauthenticated
        // requests, and that token reports isAuthenticated() == true. Checking
        // only that flag would report role "ANONYMOUS" to the frontend, so we
        // must test the token type explicitly.
        if (auth == null
                || !auth.isAuthenticated()
                || auth instanceof AnonymousAuthenticationToken) {
            // Explicitly typed so the generic matches the method's return type.
            return ResponseEntity.status(401).body(new LinkedHashMap<String, Object>());
        }

        String principal = auth.getName();
        String role = resolveRole(auth);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("role", role);

        switch (role) {
            case "DOCTOR" -> {
                Doctor doc = doctorLookup.find(principal);
                // getId() is a primitive long, so box it explicitly before the
                // null-ternary to avoid an unboxing NPE.
                body.put("id", doc != null ? Long.valueOf(doc.getId()) : null);
                body.put("name", doc != null ? doc.getName() : principal);
                body.put("email", principal);
                body.put("avatarUrl", doc != null ? doc.getProfileImage() : null);
            }
            case "ADMIN" -> {
                adminDao.findByUsername(principal).ifPresentOrElse(admin -> {
                    body.put("id", admin.getId());
                    body.put("name", admin.getUsername());
                    body.put("email", principal);
                    body.put("avatarUrl", null);
                }, () -> {
                    body.put("id", null);
                    body.put("name", principal);
                    body.put("email", principal);
                    body.put("avatarUrl", null);
                });
            }
            default -> {
                Optional<Patient> patient = patientService.findByEmail(principal);
                body.put("id", patient.map(Patient::getId).orElse(null));
                body.put("name", principal);
                body.put("email", principal);
                body.put("avatarUrl", null);
            }
        }

        return ResponseEntity.ok(body);
    }

    /**
     * Roles come from the granted authorities (ROLE_PATIENT / ROLE_DOCTOR /
     * ROLE_ADMIN), which every UserDetails implementation sets. Reading the
     * authority is more reliable than inspecting the UserDetails subclass,
     * because the admin chain builds a plain Spring User.
     */
    private String resolveRole(Authentication auth) {
        for (GrantedAuthority authority : auth.getAuthorities()) {
            String value = authority.getAuthority();
            if (value.startsWith("ROLE_")) {
                return value.substring(5);
            }
        }
        return "PATIENT";
    }

    /**
     * Small indirection so we can reach the Doctor entity without widening
     * the controller's coupling to the service layer.
     */
    @org.springframework.stereotype.Component
    public static class DoctorUserDetailsLookup {
        private final com.example.cloud.care.dao.doctor_dao doctorDao;

        public DoctorUserDetailsLookup(com.example.cloud.care.dao.doctor_dao doctorDao) {
            this.doctorDao = doctorDao;
        }

        public Doctor find(String email) {
            return doctorDao.findByEmail(email)
                    .map(DoctorUserDetails::new)
                    .map(DoctorUserDetails::getDoctor)
                    .orElse(null);
        }
    }
}