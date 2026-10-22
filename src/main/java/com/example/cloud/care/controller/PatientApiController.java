package com.example.cloud.care.controller;

import com.example.cloud.care.model.Appointment;
import com.example.cloud.care.model.Doctor;
import com.example.cloud.care.model.DoctorAvailability;
import com.example.cloud.care.model.Patient;
import com.example.cloud.care.model.Request;
import com.example.cloud.care.model.notification;
import com.example.cloud.care.service.AppointmentService;
import com.example.cloud.care.service.DoctorAvailabilityService;
import com.example.cloud.care.service.RequestService;
import com.example.cloud.care.service.doctor_service;
import com.example.cloud.care.service.loggedInUserFind;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON endpoints backing the Next.js patient pages.
 *
 * Scoped to /patient/api/** so they sit inside the existing patient security
 * chain (/patient/**, Order 1) and therefore require an authenticated session.
 * That means no extra authorization wiring is needed — an anonymous caller
 * never reaches this class.
 *
 * EVERY response body is an explicit DTO built here, never an entity passed
 * straight through. That is deliberate on two counts:
 *
 *   1. Patient, Doctor and Appointment models carry `password` (bcrypt hash)
 *      and `resetToken`. An earlier version of /patient/api/doctors returned
 *      raw Doctor entities, which handed every approved doctor's credential
 *      hash and reset token to any logged-in patient.
 *   2. Appointment holds LAZY @ManyToOne back-references to both Doctor and
 *      Patient, and Doctor holds a collection of Appointments. Serializing
 *      either entity directly risks a lazy-init failure outside a
 *      transaction, and a serialization cycle.
 */
@RestController
public class PatientApiController {

    /** The one date format used across all these responses. */
    private static final String DATE_FMT = "yyyy-MM-dd";

    private final RequestService requestService;
    private final doctor_service doctorService;
    private final AppointmentService appointmentService;
    private final DoctorAvailabilityService availabilityService;
    private final loggedInUserFind logger;

    public PatientApiController(RequestService requestService,
                                doctor_service doctorService,
                                AppointmentService appointmentService,
                                DoctorAvailabilityService availabilityService,
                                loggedInUserFind logger) {
        this.requestService = requestService;
        this.doctorService = doctorService;
        this.appointmentService = appointmentService;
        this.availabilityService = availabilityService;
        this.logger = logger;
    }

    // ------------------------------------------------------------------
    // Blood requests
    // ------------------------------------------------------------------

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
        String ownerEmail = currentEmail();
        if (ownerEmail == null) {
            return unauthorized();
        }

        List<Request> mine = requestService.getAllRequests().stream()
                .filter(r -> ownerEmail.equalsIgnoreCase(r.getEmail()))
                .toList();

        List<Map<String, Object>> dto = new ArrayList<>(mine.size());
        for (Request r : mine) {
            dto.add(requestDto(r));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "count", dto.size(),
                "data", dto
        ));
    }

    // ------------------------------------------------------------------
    // Doctors
    // ------------------------------------------------------------------

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

        List<Map<String, Object>> dto = new ArrayList<>(approved.size());
        for (Doctor d : approved) {
            dto.add(doctorListDto(d));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "count", dto.size(),
                "data", dto
        ));
    }

    @GetMapping("/patient/api/doctors/{id}")
    public ResponseEntity<Map<String, Object>> doctor(@PathVariable Long id) {
        Doctor doctor = doctorService.getDoctorByID(id);

        if (doctor == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Doctor not found"));
        }

        Map<String, Object> dto = doctorProfileDto(doctor);
        return ResponseEntity.ok(Map.of("success", true, "data", dto));
    }

    /**
     * Bookable slots for one doctor.
     *
     * AppointmentService.getDoctorAvailability exists for this but nothing
     * bound it to a route; the only way to read a doctor's slots was to render
     * an HTML page. Already-booked slots are filtered out so the frontend
     * cannot present a slot that is already taken.
     */
    @GetMapping("/patient/api/doctors/{id}/availability")
    public ResponseEntity<Map<String, Object>> availability(@PathVariable Long id) {
        Doctor doctor = doctorService.getDoctorByID(id);

        if (doctor == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Doctor not found"));
        }

        List<DoctorAvailability> slots = availabilityService.getAvailabilityForDoctor(doctor);

        List<Map<String, Object>> dto = new ArrayList<>();
        for (DoctorAvailability slot : slots) {
            if (slot.isBooked()) continue;
            dto.add(slotDto(slot));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "doctorId", doctor.getId(),
                "doctorName", doctor.getName(),
                "count", dto.size(),
                "data", dto
        ));
    }

    // ------------------------------------------------------------------
    // Appointments
    // ------------------------------------------------------------------

    /**
     * The signed-in patient's appointments.
     *
     * The legacy /patient/appointments view was broken: it returned the
     * doctor_list template, so the appointment list it had already loaded into
     * the model was never rendered. This is the working replacement.
     */
    @GetMapping("/patient/api/appointments")
    public ResponseEntity<Map<String, Object>> myAppointments() {
        Patient patient = logger.logger();

        if (patient == null) {
            return unauthorized();
        }

        List<Appointment> mine = appointmentService.getPatientAppointments(patient.getId());

        List<Map<String, Object>> dto = new ArrayList<>(mine.size());
        for (Appointment a : mine) {
            dto.add(appointmentDto(a));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "count", dto.size(),
                "data", dto
        ));
    }

    /**
     * Book a slot.
     *
     * The legacy route was POST /patient/{doctorId}/book/{slotId} with no body
     * and a form redirect. Kept as a JSON endpoint at the same path shape.
     */
    @PostMapping("/patient/api/{doctorId}/book/{slotId}")
    public ResponseEntity<Map<String, Object>> book(@PathVariable Long doctorId,
                                                    @PathVariable Long slotId) {
        Patient patient = logger.logger();

        if (patient == null) {
            return unauthorized();
        }

        try {
            Appointment booked = appointmentService.bookAppointment(patient, doctorId, slotId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Appointment booked successfully",
                    "data", appointmentDto(booked)
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // ------------------------------------------------------------------
    // Notifications
    // ------------------------------------------------------------------

    /**
     * The patient's notifications, newest first.
     *
     * Previously only reachable by rendering the `notification` Thymeleaf page.
     * The WebSocket that used to push these live is fully commented out in
     * socketController, so polling this endpoint matches actual behaviour.
     */
    @GetMapping("/patient/api/notifications")
    public ResponseEntity<Map<String, Object>> notifications() {
        Patient patient = logger.logger();

        if (patient == null) {
            return unauthorized();
        }

        List<notification> all = patient.getAllNotification();

        List<Map<String, Object>> dto = new ArrayList<>(all.size());
        for (notification n : all) {
            dto.add(notificationDto(n));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "count", dto.size(),
                "data", dto
        ));
    }

    // ------------------------------------------------------------------
    // Dashboard
    // ------------------------------------------------------------------

    /**
     * Counts for the dashboard header. Each figure is scoped to this patient
     * where that is meaningful (requests, appointments) and global where the
     * underlying data has no owner (donors are a shared public resource).
     */
    @GetMapping("/patient/api/dashboard")
    public ResponseEntity<Map<String, Object>> dashboard() {
        String email = currentEmail();
        if (email == null) {
            return unauthorized();
        }

        long myRequests = requestService.getAllRequests().stream()
                .filter(r -> email.equalsIgnoreCase(r.getEmail()))
                .count();

        long pendingRequests = requestService.getAllRequests().stream()
                .filter(r -> email.equalsIgnoreCase(r.getEmail()))
                .filter(r -> "Pending".equalsIgnoreCase(r.getStatus()))
                .count();

        Patient patient = logger.logger();
        long appointments = 0;
        if (patient != null) {
            appointments = appointmentService.getPatientAppointments(patient.getId()).size();
        }

        long doctors = doctorService.getDoctors().stream()
                .filter(d -> d.getStatus() == Doctor.Status.APPROVED)
                .count();

        long unreadNotifications = patient == null
                ? 0
                : patient.getAllNotification().stream()
                        .filter(n -> !n.isRead())
                        .count();

        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("myRequests", myRequests);
        dto.put("pendingRequests", pendingRequests);
        dto.put("appointments", appointments);
        dto.put("approvedDoctors", doctors);
        dto.put("unreadNotifications", unreadNotifications);

        return ResponseEntity.ok(dto);
    }

    // ------------------------------------------------------------------
    // DTO builders
    // ------------------------------------------------------------------

    /** Compact shape for a doctor card in the browse list. */
    private Map<String, Object> doctorListDto(Doctor d) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", d.getId());
        dto.put("name", d.getName());
        dto.put("specialization", d.getSpecialization());
        dto.put("degrees", d.getDegrees());
        dto.put("experienceYears", d.getExperienceYears());
        dto.put("zilla", d.getZilla());
        dto.put("hospitalName", d.getHospitalName());
        dto.put("consultationFee", d.getConsultationFee());
        dto.put("profileImage", d.getProfileImage());
        dto.put("rating", d.getRating());
        dto.put("bloodGroup", d.getBloodGroup());
        return dto;
    }

    /** Fuller shape for the profile page. Still no password/resetToken. */
    private Map<String, Object> doctorProfileDto(Doctor d) {
        Map<String, Object> dto = doctorListDto(d);
        dto.put("email", d.getEmail());
        dto.put("phoneNumber", d.getPhoneNumber());
        dto.put("address", d.getAddress());
        dto.put("hospitalAddress", d.getHospitalAddress());
        dto.put("description", d.getDescription());
        dto.put("languages", d.getLanguages());
        dto.put("certifications", d.getCertifications());
        dto.put("workingDays", d.getWorkingDays());
        dto.put("workingHours", d.getWorkingHours());
        dto.put("telemedicineAvailable", d.getTelemedicineAvailable());
        return dto;
    }

    private Map<String, Object> requestDto(Request r) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", r.getId());
        dto.put("name", r.getName());
        dto.put("email", r.getEmail());
        dto.put("bloodGroup", r.getBloodGroup());
        dto.put("units", r.getUnits());
        dto.put("district", r.getDistrict());
        dto.put("thana", r.getThana());
        dto.put("location", r.getLocation());
        dto.put("requiredDate", format(r.getRequiredDate()));
        dto.put("status", r.getStatus());
        dto.put("medicalReason", r.getMedicalReason());
        dto.put("createdAt", r.getCreatedAt() == null ? null : r.getCreatedAt().toString());
        return dto;
    }

    private Map<String, Object> appointmentDto(Appointment a) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", a.getId());

        // The nested doctor/patient are LAZY and mutually referential, so pull
        // only the display fields we need rather than serializing the entity.
        Doctor d = a.getDoctor();
        if (d != null) {
            dto.put("doctorId", d.getId());
            dto.put("doctorName", d.getName());
            dto.put("specialization", d.getSpecialization());
        }

        dto.put("appointmentDate", format(a.getAppointmentDate()));
        dto.put("timeSlot", a.getTimeSlot());
        dto.put("type", a.getType());
        dto.put("status", a.getStatus());
        return dto;
    }

    private Map<String, Object> slotDto(DoctorAvailability slot) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", slot.getId());
        dto.put("startTime", slot.getStartTime() == null ? null : slot.getStartTime().toString());
        dto.put("endTime", slot.getEndTime() == null ? null : slot.getEndTime().toString());
        dto.put("date", format(slot.getDate()));
        dto.put("telemedicineAvailable", slot.getTelemedicineAvailable());
        dto.put("booked", slot.isBooked());
        return dto;
    }

    private Map<String, Object> notificationDto(notification n) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", n.getId());
        dto.put("message", n.getMessage());
        dto.put("date", n.getDate() == null ? null : n.getDate().toString());
        // Lombok @Data on a `boolean isRead` field generates isRead(), so
        // Jackson emits the key as "read", not "isRead".
        dto.put("read", n.isRead());
        return dto;
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private String currentEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }

        Patient patient = logger.logger();
        if (patient != null && patient.getUser() != null) {
            return patient.getUser().getEmail();
        }
        return auth.getName();
    }

    private ResponseEntity<Map<String, Object>> unauthorized() {
        return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
    }

    private static String format(java.util.Date date) {
        return date == null ? null : new SimpleDateFormat(DATE_FMT).format(date);
    }

    /** Request.requiredDate is a LocalDate, unlike the other date columns. */
    private static String format(java.time.LocalDate date) {
        return date == null ? null : date.toString();
    }
}