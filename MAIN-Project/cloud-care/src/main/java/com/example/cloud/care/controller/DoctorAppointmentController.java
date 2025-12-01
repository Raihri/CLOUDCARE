package com.example.cloud.care.controller;

import com.example.cloud.care.model.Appointment;
import com.example.cloud.care.model.Doctor;
import com.example.cloud.care.repository.AppointmentRepository;
import com.example.cloud.care.dao.doctor_dao;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/doctor")
public class DoctorAppointmentController {

    private final AppointmentRepository appointmentRepository;
    private final doctor_dao doctorRepository;

    public DoctorAppointmentController(AppointmentRepository appointmentRepository, doctor_dao doctorRepository) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
    }

    // Show all appointments for this doctor
    @GetMapping("/{doctorId}/appointments")
    public String viewAppointments(@PathVariable int doctorId, Model model) {
        List<Appointment> appointments = appointmentRepository.findByDoctorId(doctorId);
        model.addAttribute("appointments", appointments);
        model.addAttribute("doctorId", doctorId);
        return "doctor-appointments"; // Thymeleaf template
    }

    // Confirm appointment
    @PostMapping("/{doctorId}/appointments/{appointmentId}/confirm")
    public String confirmAppointment(@PathVariable int doctorId, @PathVariable int appointmentId) {
        Appointment appt = appointmentRepository.findById(appointmentId).orElse(null);
        if (appt != null) {
            appt.setStatus(Appointment.Status.CONFIRMED);
            appointmentRepository.save(appt);
        }
        return "redirect:/doctor/" + doctorId + "/appointments";
    }

    // Cancel appointment
    @PostMapping("/{doctorId}/appointments/{appointmentId}/cancel")
    public String cancelAppointment(@PathVariable int doctorId, @PathVariable int appointmentId) {
        Appointment appt = appointmentRepository.findById(appointmentId).orElse(null);
        if (appt != null) {
            appt.setStatus(Appointment.Status.CANCELLED);
            appointmentRepository.save(appt);
        }
        return "redirect:/doctor/" + doctorId + "/appointments";
    }
}