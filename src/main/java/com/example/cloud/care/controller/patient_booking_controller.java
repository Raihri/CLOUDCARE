package com.example.cloud.care.controller;
import com.example.cloud.care.model.Appointment;
import com.example.cloud.care.model.Doctor;
import com.example.cloud.care.service.AppointmentService;
import com.example.cloud.care.service.EmailServiceD;
import com.example.cloud.care.service.UserService;
import com.example.cloud.care.service.doctor_service;
import com.example.cloud.care.service.patient_service;

import jakarta.servlet.http.HttpSession;

import com.example.cloud.care.dao.doctor_dao;

import org.springframework.security.access.method.P;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.cloud.care.model.DoctorAvailability;
import com.example.cloud.care.model.Patient;
import com.example.cloud.care.model.User;
import com.example.cloud.care.service.DoctorAvailabilityService;

import java.net.http.HttpConnectTimeoutException;
import java.security.Principal;
import java.util.List;
@Controller
@RequestMapping("/patient")
public class patient_booking_controller {
    private final DoctorAvailabilityService availabilityService ;
    private final doctor_service doctorService;
    private final AppointmentService appointmentService;
    private final patient_service patientService;

    public patient_booking_controller(DoctorAvailabilityService availabilityService, doctor_service doctorService, AppointmentService appointmentService, patient_service patientService) {
        this.availabilityService = availabilityService;
        this.doctorService = doctorService;
        this.appointmentService = appointmentService;
        this.patientService = patientService;
    }

    @GetMapping("/{doctorId}/availability")
    public String viewAvailability(@PathVariable long doctorId, Model model) {
        Doctor doctor = doctorService.getDoctorByID(doctorId);
        model.addAttribute("doctor", doctor);

        model.addAttribute("availabilityList",
                availabilityService.getAvailabilityForDoctor(doctor));

        return "shitshitshit"; // your availability view template
    }

   @PostMapping("/{doctorId}/book/{slotId}")
public String bookSlot(@PathVariable long doctorId,
                       @PathVariable long slotId,
                       Principal principal,
                       RedirectAttributes redirectAttributes) {
    
    // Fetch patient
    if (principal == null) {
        return "redirect:/patient/";
    }
    String email = principal.getName();

    // Fetch patient using email
    Patient patient = patientService.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Patient not found"));

    // Fetch doctor and slot
    Doctor doctor = doctorService.getDoctorByID(doctorId);
    DoctorAvailability slot = availabilityService.getSlotById(slotId);

    if (slot.isBooked()) {
        redirectAttributes.addFlashAttribute("errorMessage", "❌ This slot is already booked!");
        return "redirect:/patient/" + doctorId + "/availability";
    }

    // Mark slot as booked
    slot.setBooked(true);
    availabilityService.save(slot);

    // Create appointment
    Appointment appt = new Appointment();
    appt.setDoctor(doctor);
    appt.setPatient(patient);      // <-- Assign patient here
    appt.setAppointmentDate(slot.getDate());
    appt.setTimeSlot(slot.getStartTime() + "-" + slot.getEndTime());
    appt.setType(slot.getTelemedicineAvailable() ? 
                        Appointment.AppointmentType.TELEMEDICINE : 
                        Appointment.AppointmentType.PHYSICAL);
    appt.setStatus(Appointment.Status.PENDING);

   

    appointmentService.save(appt); // Save appointment

    redirectAttributes.addFlashAttribute(
            "successMessage",
            "✅ Your booking request has been saved successfully!"
    );

    return "redirect:/patient/" + doctorId + "/availability";
}

}