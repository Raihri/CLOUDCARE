package com.example.cloud.care.controller;

import com.example.cloud.care.dao.doctor_dao;
import com.example.cloud.care.model.Appointment;
import com.example.cloud.care.model.Doctor;
import com.example.cloud.care.model.User;
import com.example.cloud.care.service.AppointmentService;

import com.example.cloud.care.service.EmailServiceD;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/doctor")
public class DoctorAppointmentController {

    private final AppointmentService appointmentService;
    private final doctor_dao doctorRepository;
    private final EmailServiceD emailService;

    public DoctorAppointmentController(AppointmentService appointmentService,
                                       doctor_dao doctorRepository,
                                       EmailServiceD emailService) {
        this.appointmentService = appointmentService;
        this.doctorRepository = doctorRepository;
        this.emailService = emailService;
    }

    // Show all appointments for a doctor
    @GetMapping("/{doctorId}/appointments")
    public String viewAppointments(@PathVariable long doctorId, Model model) {
        List<Appointment> appointments = appointmentService.getAppointmentsByDoctor(doctorId);
        Doctor doctor = doctorRepository.findById(doctorId).orElseThrow();
        model.addAttribute("appointments", appointments);
        model.addAttribute("doctor", doctor);
        return "doctor-appointments"; // Thymeleaf template
    }

    // Confirm appointment
    @PostMapping("/{doctorId}/appointments/{appointmentId}/confirm")
    public String confirmAppointment(@PathVariable long doctorId,
                                     @PathVariable int appointmentId) {

        Appointment appt = appointmentService.getAppointmentById(appointmentId);
        if (appt != null) {
            appointmentService.confirmAppointment(appt); // handles status update & Jitsi link

            // Send email notifications
            emailService.sendAppointmentConfirmation(appt);
        }

        return "redirect:/doctor/" + doctorId + "/appointments";
    }

    // Cancel appointment
    @PostMapping("/{doctorId}/appointments/{appointmentId}/cancel")
    public String cancelAppointment(@PathVariable long doctorId,
                                    @PathVariable int appointmentId) {

        Appointment appt = appointmentService.getAppointmentById(appointmentId);
        if (appt != null) {
            appointmentService.cancelAppointment(appt); // handles status update

            // Optional: send cancellation email
            emailService.sendAppointmentCancellation(appt);
        }

        return "redirect:/doctor/" + doctorId + "/appointments";
    }
}