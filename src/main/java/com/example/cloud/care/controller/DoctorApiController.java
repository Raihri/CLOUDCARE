package com.example.cloud.care.controller;

import com.example.cloud.care.model.Appointment;
import com.example.cloud.care.model.Doctor;
import com.example.cloud.care.model.DoctorAvailability;
import com.example.cloud.care.service.AppointmentService;
import com.example.cloud.care.service.doctor_service;
import com.example.cloud.care.service.DoctorAvailabilityService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON API behind the Next.js doctor portal.
 *
 * <p>Every handler resolves "who am I" from the SecurityContext rather than
 * taking a doctor id from the request, so a doctor can't read or mutate another
 * doctor's calendar by editing a URL. Status changes additionally verify that
 * the target appointment is in the caller's own list.
 *
 * <p>Responses are explicit maps rather than serialised {@link Doctor}
 * entities, which carry {@code password} and {@code resetToken}.
 */
@Controller
@RequestMapping("/doctor/api")
public class DoctorApiController {

    private static final String DATE_FMT = "yyyy-MM-dd";
    private static final String TIME_FMT = "HH:mm";

    @Autowired
    private doctor_service doctorService;

    @Autowired
    private DoctorAvailabilityService availabilityService;

    @Autowired
    private AppointmentService appointmentService;

    // ------------------------------------------------------------------
    // Identity
    // ------------------------------------------------------------------

    /** The signed-in doctor, resolved from the authenticated email. */
    private Doctor currentDoctor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) return null;
        return doctorService.findByEmail(auth.getName());
    }

    /** Returns an error envelope when the caller isn't a known doctor, else null. */
    private Map<String, Object> denied() {
        if (currentDoctor() != null) return null;
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("success", false);
        m.put("message", "Doctor not found.");
        return m;
    }

    @GetMapping("/me")
    @ResponseBody
    public Map<String, Object> me() {
        Doctor doc = currentDoctor();
        if (doc == null) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("success", false);
            m.put("message", "Doctor not found.");
            return m;
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("success", true);
        out.putAll(profileDto(doc));
        return out;
    }

    // ------------------------------------------------------------------
    // Dashboard
    // ------------------------------------------------------------------

    @GetMapping("/dashboard")
    @ResponseBody
    public Map<String, Object> dashboard() {
        Map<String, Object> denied = denied();
        if (denied != null) return denied;

        Doctor doc = currentDoctor();
        List<Appointment> appointments = appointmentService.getAppointmentsByDoctor(doc.getId());

        long pending = appointments.stream()
                .filter(a -> a.getStatus() == Appointment.Status.PENDING)
                .count();
        long confirmed = appointments.stream()
                .filter(a -> a.getStatus() == Appointment.Status.CONFIRMED)
                .count();
        long cancelled = appointments.stream()
                .filter(a -> a.getStatus() == Appointment.Status.CANCELLED)
                .count();

        List<DoctorAvailability> slots = availabilityService.getAvailabilityForDoctor(doc);
        long openSlots = slots.stream().filter(s -> !s.isBooked()).count();

        // Today's onward agenda, soonest first — the doctor's "what's next" list.
        // Compared as Dates so a time component doesn't skew the cutoff.
        Date today = new Date();
        List<Appointment> upcoming = appointments.stream()
                .filter(a -> a.getAppointmentDate() != null
                        && !a.getAppointmentDate().before(today))
                .sorted((x, y) -> x.getAppointmentDate().compareTo(y.getAppointmentDate()))
                .limit(5)
                .toList();

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("success", true);
        out.put("name", doc.getName());
        out.put("status", doc.getStatus() != null ? doc.getStatus().name() : null);
        out.put("totalAppointments", appointments.size());
        out.put("pendingAppointments", pending);
        out.put("confirmedAppointments", confirmed);
        out.put("cancelledAppointments", cancelled);
        out.put("openSlots", openSlots);
        out.put("upcoming", upcoming.stream().map(this::appointmentDto).toList());
        return out;
    }

    // ------------------------------------------------------------------
    // Profile
    // ------------------------------------------------------------------

    @PostMapping("/profile")
    @ResponseBody
    public Map<String, Object> updateProfile(
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String division,
            @RequestParam(required = false) String zilla,
            @RequestParam(required = false) String hospitalName,
            @RequestParam(required = false) String hospitalAddress,
            @RequestParam(required = false) String consultationFee,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String workingDays,
            @RequestParam(required = false) String workingHours,
            @RequestParam(required = false) String languages,
            @RequestParam(required = false) String certifications,
            @RequestParam(required = false) String specialInterests) {

        Map<String, Object> denied = denied();
        if (denied != null) return denied;

        Doctor doc = currentDoctor();
        // Only contact/practice fields are writable here. Name, BMDC number and
        // status gate who may practise and are an admin's call, not a
        // self-service edit.
        if (phoneNumber != null) doc.setPhoneNumber(phoneNumber);
        if (address != null) doc.setAddress(address);
        if (division != null) doc.setDivision(division);
        if (zilla != null) doc.setZilla(zilla);
        if (hospitalName != null) doc.setHospitalName(hospitalName);
        if (hospitalAddress != null) doc.setHospitalAddress(hospitalAddress);
        if (consultationFee != null) doc.setConsultationFee(consultationFee);
        if (description != null) doc.setDescription(description);
        if (workingDays != null) doc.setWorkingDays(workingDays);
        if (workingHours != null) doc.setWorkingHours(workingHours);
        if (languages != null) doc.setLanguages(languages);
        if (certifications != null) doc.setCertifications(certifications);
        if (specialInterests != null) doc.setSpecialInterests(specialInterests);

        doctorService.saveDoctor(doc);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("success", true);
        out.put("message", "Profile updated successfully!");
        return out;
    }

    // ------------------------------------------------------------------
    // Availability
    // ------------------------------------------------------------------

    @GetMapping("/availability")
    @ResponseBody
    public Map<String, Object> availability() {
        Map<String, Object> denied = denied();
        if (denied != null) return denied;

        Doctor doc = currentDoctor();
        List<DoctorAvailability> slots = availabilityService.getAvailabilityForDoctor(doc);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("success", true);
        out.put("count", slots.size());
        out.put("data", slots.stream().map(this::slotDto).toList());
        return out;
    }

    @PostMapping("/availability")
    @ResponseBody
    public Map<String, Object> addSlot(
            @RequestParam @DateTimeFormat(pattern = DATE_FMT) Date date,
            @RequestParam String startTime,
            @RequestParam String endTime,
            @RequestParam(required = false, defaultValue = "false") boolean telemedicine) {

        Map<String, Object> denied = denied();
        if (denied != null) return denied;

        Doctor doctor = currentDoctor();
        LocalTime start;
        LocalTime end;
        try {
            start = LocalTime.parse(startTime);
            end = LocalTime.parse(endTime);
        } catch (Exception e) {
            return error("Times must be in HH:mm format.");
        }

        if (!start.isBefore(end)) {
            return error("The start time must be before the end time.");
        }

        // Same overlap rule as the Thymeleaf form: a slot that partially covers
        // an existing one would produce a double-bookable calendar.
        for (DoctorAvailability existing : availabilityService.getAvailabilityForDoctor(doctor)) {
            if (date.equals(existing.getDate())
                    && start.isBefore(existing.getEndTime())
                    && end.isAfter(existing.getStartTime())) {
                return error("That overlaps a slot you've already added.");
            }
        }

        DoctorAvailability slot = new DoctorAvailability();
        slot.setDate(date);
        slot.setStartTime(start);
        slot.setEndTime(end);
        slot.setTelemedicineAvailable(telemedicine);
        slot.setDoctor(doctor);
        availabilityService.save(slot);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("success", true);
        out.put("message", "Slot added.");
        out.put("slot", slotDto(slot));
        return out;
    }

    @DeleteMapping("/availability/{slotId}")
    @ResponseBody
    public Map<String, Object> removeSlot(@PathVariable Long slotId) {
        Map<String, Object> denied = denied();
        if (denied != null) return denied;

        Doctor doctor = currentDoctor();
        DoctorAvailability slot = availabilityService.getSlotById(slotId);

        // Confirm ownership before deleting, so an id from another doctor can't
        // be used to clear their calendar.
        if (slot == null || slot.getDoctor() == null
                || slot.getDoctor().getId() != doctor.getId()) {
            return error("That slot does not belong to you.");
        }
        if (slot.isBooked()) {
            return error("That slot is booked by a patient. Cancel the appointment first.");
        }

        availabilityService.delete(slotId);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("success", true);
        out.put("message", "Slot removed.");
        return out;
    }

    // ------------------------------------------------------------------
    // Appointments
    // ------------------------------------------------------------------

    @GetMapping("/appointments")
    @ResponseBody
    public Map<String, Object> appointments() {
        Map<String, Object> denied = denied();
        if (denied != null) return denied;

        Doctor doc = currentDoctor();
        List<Appointment> list = appointmentService.getAppointmentsByDoctor(doc.getId());

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("success", true);
        out.put("count", list.size());
        out.put("data", list.stream().map(this::appointmentDto).toList());
        return out;
    }

    @PostMapping("/appointments/{appointmentId}/confirm")
    @ResponseBody
    public Map<String, Object> confirm(@PathVariable Long appointmentId) {
        Map<String, Object> denied = denied();
        if (denied != null) return denied;
        return setAppointmentStatus(appointmentId, Appointment.Status.CONFIRMED);
    }

    @PostMapping("/appointments/{appointmentId}/cancel")
    @ResponseBody
    public Map<String, Object> cancel(@PathVariable Long appointmentId) {
        Map<String, Object> denied = denied();
        if (denied != null) return denied;
        return setAppointmentStatus(appointmentId, Appointment.Status.CANCELLED);
    }

    /**
     * Applies a status change, but only after confirming the appointment is in
     * the caller's own list — otherwise any doctor could confirm or cancel
     * another doctor's patient by guessing an id.
     */
    private Map<String, Object> setAppointmentStatus(Long appointmentId, Appointment.Status status) {
        Doctor doc = currentDoctor();

        // Appointment.id is a primitive int, so comparing with Long.equals
        // would always be false — compare as numbers instead.
        Appointment target = appointmentService.getAppointmentsByDoctor(doc.getId()).stream()
                .filter(a -> a.getId() == appointmentId.longValue())
                .findFirst()
                .orElse(null);

        if (target == null) {
            return error("That appointment does not belong to you.");
        }

        try {
            if (status == Appointment.Status.CONFIRMED) {
                appointmentService.confirmAppointment(target);
            } else {
                appointmentService.cancelAppointment(target);
            }
        } catch (Exception e) {
            return error(e.getMessage());
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("success", true);
        out.put("message", "Appointment " + status.name().toLowerCase() + ".");
        out.put("appointment", appointmentDto(target));
        return out;
    }

    /**
     * The distinct patients this doctor has appointments with.
     *
     * <p>Derived from the doctor's own appointment list rather than a separate
     * patient query, so the result can't include someone the doctor has never
     * actually seen. Sorted by name for a scannable list.
     */
    @GetMapping("/patients")
    @ResponseBody
    public Map<String, Object> patients() {
        Map<String, Object> denied = denied();
        if (denied != null) return denied;

        Doctor doc = currentDoctor();
        Map<Long, Map<String, Object>> byPatient = new LinkedHashMap<>();

        for (Appointment a : appointmentService.getAppointmentsByDoctor(doc.getId())) {
            if (a.getPatient() == null || a.getPatient().getId() == null) continue;

            Map<String, Object> entry = byPatient.computeIfAbsent(
                    a.getPatient().getId(), id -> {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("id", id);
                        m.put("name", a.getPatient().getUser() != null
                                ? a.getPatient().getUser().getName() : null);
                        m.put("email", a.getPatient().getUser() != null
                                ? a.getPatient().getUser().getEmail() : null);
                        m.put("bloodGroup", a.getPatient().getBloodGroup());
                        m.put("gender", a.getPatient().getGender() != null
                                ? String.valueOf(a.getPatient().getGender()) : null);
                        m.put("age", a.getPatient().getAge());
                        m.put("zilla", a.getPatient().getZilla());
                        m.put("appointments", 0L);
                        m.put("lastVisit", null);
                        return m;
                    });

            entry.put("appointments", ((Long) entry.get("appointments")) + 1L);
            if (a.getAppointmentDate() != null) {
                // Tracked as a Date and formatted at the end — comparing
                // pre-formatted strings would work here but wouldn't survive a
                // change to the date format.
                Date current = (Date) entry.get("lastVisitDate");
                if (current == null || a.getAppointmentDate().after(current)) {
                    entry.put("lastVisitDate", a.getAppointmentDate());
                }
            }
        }

        List<Map<String, Object>> list = new ArrayList<>(byPatient.values());
        SimpleDateFormat dateFmt = new SimpleDateFormat(DATE_FMT);
        for (Map<String, Object> entry : list) {
            Date last = (Date) entry.remove("lastVisitDate");
            entry.put("lastVisit", last == null ? null : dateFmt.format(last));
        }

        list.sort((x, y) -> {
            String nx = (String) x.get("name");
            String ny = (String) y.get("name");
            if (nx == null) return 1;
            if (ny == null) return -1;
            return nx.compareToIgnoreCase(ny);
        });

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("success", true);
        out.put("count", list.size());
        out.put("data", list);
        return out;
    }

    // ------------------------------------------------------------------
    // DTOs
    // ------------------------------------------------------------------

    private Map<String, Object> profileDto(Doctor d) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", d.getId());
        m.put("name", d.getName());
        m.put("email", d.getEmail());
        m.put("profileImage", d.getProfileImage());
        m.put("bloodGroup", d.getBloodGroup());
        m.put("gender", d.getGender());
        m.put("phoneNumber", d.getPhoneNumber());
        m.put("altPhone", d.getAltPhone());
        m.put("address", d.getAddress());
        m.put("division", d.getDivision());
        m.put("zilla", d.getZilla());
        m.put("bmdcRegNo", d.getBmdcRegNo());
        m.put("degrees", d.getDegrees());
        m.put("specialization", d.getSpecialization());
        m.put("education", d.getEducation());
        m.put("medicalCollege", d.getMedicalCollege());
        m.put("experienceYears", d.getExperienceYears());
        m.put("certifications", d.getCertifications());
        m.put("languages", d.getLanguages());
        m.put("hospitalName", d.getHospitalName());
        m.put("hospitalAddress", d.getHospitalAddress());
        m.put("consultationFee", d.getConsultationFee());
        m.put("description", d.getDescription());
        m.put("workingDays", d.getWorkingDays());
        m.put("workingHours", d.getWorkingHours());
        m.put("specialInterests", d.getSpecialInterests());
        m.put("rating", d.getRating());
        m.put("telemedicineAvailable", d.getTelemedicineAvailable());
        m.put("status", d.getStatus() != null ? d.getStatus().name() : null);
        return m;
    }

    private Map<String, Object> slotDto(DoctorAvailability s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", s.getId());
        m.put("date", s.getDate() == null ? null : new SimpleDateFormat(DATE_FMT).format(s.getDate()));
        m.put("startTime", s.getStartTime() == null ? null : s.getStartTime().format(java.time.format.DateTimeFormatter.ofPattern(TIME_FMT)));
        m.put("endTime", s.getEndTime() == null ? null : s.getEndTime().format(java.time.format.DateTimeFormatter.ofPattern(TIME_FMT)));
        m.put("booked", s.isBooked());
        m.put("telemedicineAvailable", s.getTelemedicineAvailable());
        return m;
    }

    private Map<String, Object> appointmentDto(Appointment a) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", a.getId());
        m.put("patientId", a.getPatient() != null ? a.getPatient().getId() : null);
        m.put("patientName", a.getPatient() != null && a.getPatient().getUser() != null
                ? a.getPatient().getUser().getName() : null);
        m.put("appointmentDate", a.getAppointmentDate() == null
                ? null : new SimpleDateFormat(DATE_FMT).format(a.getAppointmentDate()));
        m.put("timeSlot", a.getTimeSlot());
        m.put("type", a.getType() != null ? a.getType().name() : null);
        m.put("status", a.getStatus() != null ? a.getStatus().name() : null);
        m.put("telemedicineLink", a.getTelemedicineLink());
        return m;
    }

    private static Map<String, Object> error(String message) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("success", false);
        m.put("message", message);
        return m;
    }
}