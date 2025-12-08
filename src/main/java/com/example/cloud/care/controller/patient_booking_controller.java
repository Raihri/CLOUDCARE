package com.example.cloud.care.controller;
import com.example.cloud.care.model.Appointment;
import com.example.cloud.care.model.Doctor;
import com.example.cloud.care.service.AppointmentService;
import com.example.cloud.care.service.EmailServiceD;
import com.example.cloud.care.service.doctor_service;
import com.example.cloud.care.dao.doctor_dao;

import org.springframework.security.access.method.P;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.cloud.care.model.DoctorAvailability;
import com.example.cloud.care.model.User;
import com.example.cloud.care.service.DoctorAvailabilityService;

import java.security.Principal;
import java.util.List;
@Controller
@RequestMapping("/patient")
public class patient_booking_controller {
    private final DoctorAvailabilityService availabilityService ;
    private final doctor_service doctorService;
    private final AppointmentService appointmentService;
    

    public patient_booking_controller(DoctorAvailabilityService availabilityService, doctor_service doctorService, AppointmentService appointmentService) {
        this.availabilityService = availabilityService;
        this.doctorService = doctorService;
        this.appointmentService = appointmentService;
    }

    @GetMapping("/{doctorId}/availability")
    public String viewAvailability(@PathVariable long doctorId, Model model) {
        Doctor doctor = doctorService.getDoctorByID(doctorId);
        model.addAttribute("doctor", doctor);

        model.addAttribute("availabilityList",
                availabilityService.getAvailabilityForDoctor(doctor));

        return "shitshitshit"; // your availability view template
    }

    @GetMapping("/{doctorId}/book/{slotId}")
public String bookSlot(@PathVariable long doctorId,
                       @PathVariable long slotId,
                      @AuthenticationPrincipal User loggedInUser,
                       RedirectAttributes redirectAttributes) {

    Doctor doctor = doctorService.getDoctorByID(doctorId);
    DoctorAvailability slot = availabilityService.getSlotById(slotId);

    // Mark slot as booked
    slot.setBooked(true);
    availabilityService.save(slot); // make sure you save the change
    Appointment appt = appointmentService.bookAppointment(
        loggedInUser,     // User patient
    doctor.getId(),   // long doctorId
    slot.getId()     // long slotId
     // Date

        
    );
    appointmentService.save(appt); // save to DB


    redirectAttributes.addFlashAttribute(
            "successMessage",
            "✅ Your booking request has been saved successfully!"
    );

    return "redirect:/patient/" + doctorId + "/availability";
}
}
