package com.example.cloud.care.controller;

import com.example.cloud.care.model.Doctor;
import com.example.cloud.care.model.Request;
import com.example.cloud.care.service.RequestService;
import com.example.cloud.care.service.doctor_service;
import com.example.cloud.care.service.loggedInUserFind;
import com.example.cloud.care.model.Patient;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * JSON endpoints backing the Next.js patient pages.
 *
 * Scoped to /patient/api/** so they sit inside the existing patient security
 * chain (/patient/**, Order 1) and therefore require an authenticated session.
 * That means no extra authorization wiring is needed — an anonymous caller
 * never reaches this class.
 */
@RestController
public class PatientApiController {

    private final RequestService requestService;
    private final doctor_service doctorService;
    private final loggedInUserFind logger;

    public PatientApiController(RequestService requestService,
                                doctor_service doctorService,
                                loggedInUserFind logger) {
        this.requestService = requestService;
        this.doctorService = doctorService;
        this.logger = logger;
    }

    /**
     * Blood requests belonging to the signed-in patient.
     *
     * The legacy view endpoint (showPendingRequests) filtered "Pending" across
     * EVERY request in the database, so any patient could see every other
     * patient's requests. Ownership is matched on email, which is what
     * Request records and what the SecurityContext gives us.
     */
    @GetMapping("/patient/api/requests")
    public ResponseEntity<Map<String, Object>> myRequests() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth != null ? auth.getName() : null;

        if (email == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        Patient patient = logger.logger();
        String ownerEmail = patient != null && patient.getUser() != null
                ? patient.getUser().getEmail()
                : email;

        List<Request> mine = requestService.getAllRequests().stream()
                .filter(r -> ownerEmail.equalsIgnoreCase(r.getEmail()))
                .toList();

        return ResponseEntity.ok(Map.of(
                "success", true,
                "count", mine.size(),
                "data", mine
        ));
    }

    /**
     * Doctors for the browse/book flow.
     *
     * Only APPROVED doctors are returned — the old doctor_list view passed
     * every doctor through, including ones an admin had not yet approved.
     */
    @GetMapping("/patient/api/doctors")
    public ResponseEntity<Map<String, Object>> doctors() {
        List<Doctor> approved = doctorService.getDoctors().stream()
                .filter(d -> d.getStatus() == Doctor.Status.APPROVED)
                .toList();

        return ResponseEntity.ok(Map.of(
                "success", true,
                "count", approved.size(),
                "doctors", approved
        ));
    }

    @GetMapping("/patient/api/doctors/{id}")
    public ResponseEntity<Map<String, Object>> doctor(@PathVariable Long id) {
        Doctor doctor = doctorService.getDoctorByID(id);

        if (doctor == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Doctor not found"));
        }

        // Return an explicit DTO rather than the entity: the Doctor model also
        // carries password/confirmPassword and its availability collection,
        // which we neither want to expose nor want to trigger a lazy load for.
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", doctor.getId());
        dto.put("name", doctor.getName());
        dto.put("specialization", doctor.getSpecialization());
        dto.put("degrees", doctor.getDegrees());
        dto.put("experienceYears", doctor.getExperienceYears());
        dto.put("email", doctor.getEmail());
        dto.put("phoneNumber", doctor.getPhoneNumber());
        dto.put("zilla", doctor.getZilla());
        dto.put("address", doctor.getAddress());
        dto.put("bloodGroup", doctor.getBloodGroup());
        dto.put("profileImage", doctor.getProfileImage());

        return ResponseEntity.ok(Map.of("success", true, "data", dto));
    }
}