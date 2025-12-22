package com.example.cloud.care.controller;

import com.example.cloud.care.model.Appointment;
import com.example.cloud.care.model.Patient;
import com.example.cloud.care.model.User;
import com.example.cloud.care.service.AppointmentService;
import com.example.cloud.care.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.example.cloud.care.service.patient_service;

import java.security.Principal;
import java.util.List;

@Controller
public class PatientAppointmentController {

    private final AppointmentService appointmentService;
    private final UserService userService;
    private final patient_service patientService ;
    public PatientAppointmentController(AppointmentService appointmentService,
                                        UserService userService,
                                        patient_service patientService) {
        this.appointmentService = appointmentService;
        this.userService = userService;
        this.patientService = patientService;
    }

    @GetMapping("/patient/appointments")
    public String viewAppointments(Model model, Principal principal) {
        // Fetch the currently logged-in patient
         String email = principal.getName();
         Patient patient = patientService.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Patient not found"));


        // Get all appointments for this patient
        List<Appointment> appointments = appointmentService.getPatientAppointments(patient.getId());

        model.addAttribute("appointments", appointments);
        model.addAttribute("patient", patient);

        return "shitpatient"; // Thymeleaf template name
    }
}