package com.example.cloud.care.controller;

import com.example.cloud.care.model.Appointment;
import com.example.cloud.care.model.Doctor;
import com.example.cloud.care.repository.AppointmentRepository;
import com.example.cloud.care.dao.doctor_dao;
import com.example.cloud.care.service.EmailServiceD;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/doctor")
public class DoctorAppointmentController {

    private final AppointmentRepository appointmentRepository;
    private final doctor_dao doctorRepository;
    private final EmailServiceD emailService;

    public DoctorAppointmentController(AppointmentRepository appointmentRepository, doctor_dao doctorRepository,
                                       EmailServiceD emailService) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.emailService = emailService;
    }

    // Show all appointments for this doctor
    @GetMapping("/{doctorId}/appointments")
    public String viewAppointments(@PathVariable long doctorId, Model model) {
        List<Appointment> appointments = appointmentRepository.findByDoctorId(doctorId);
        model.addAttribute("appointments", appointments);
        model.addAttribute("doctorId", doctorId);
        return "doctor-appointments"; // Thymeleaf template
    }

    // Confirm appointment
    @PostMapping("/{doctorId}/appointments/{appointmentId}/confirm")
    public String confirmAppointment(@PathVariable long doctorId, @PathVariable int appointmentId) {
        Appointment appt = appointmentRepository.findById(appointmentId).orElse(null);
        if (appt != null) {
            appt.setStatus(Appointment.Status.CONFIRMED);

            Doctor doctor = appt.getDoctor();
            String patientEmail = appt.getPatient().getEmail();

            if (appt.getType() == Appointment.AppointmentType.TELEMEDICINE) {
                // Generate unique Jitsi link
                String roomName = "cloudcare-" + UUID.randomUUID();
                String jitsiLink = "https://meet.jit.si/" + roomName;
                appt.setTelemedicineLink(jitsiLink);

                // Send telemedicine email
                String subject = "Telemedicine Appointment Confirmed";
                String body = String.format(
                        "Hello %s,\n\nYour telemedicine appointment with Dr. %s has been confirmed.\n" +
                        "Date: %s\nTime: %s\n\nJoin via this link: %s\n\nRegards,\nCloudCare Team",
                        appt.getPatient().getName(),
                        doctor.getName(),
                        appt.getAppointmentDate(),
                        appt.getTimeSlot(),
                        jitsiLink
                );
                emailService.sendEmail(patientEmail, subject, body);

            } else { // Physical appointment
                String subject = "Physical Appointment Confirmed";
                String body = String.format(
                        "Hello %s,\n\nYour appointment with Dr. %s has been confirmed.\n" +
                        "Date: %s\nTime: %s\nHospital: %s\n\nRegards,\nCloudCare Team",
                        appt.getPatient().getName(),
                        doctor.getName(),
                        appt.getAppointmentDate(),
                        appt.getTimeSlot(),
                        doctor.getHospitalName()
                );
                emailService.sendEmail(patientEmail, subject, body);
            }

            appointmentRepository.save(appt);
        }
        return "redirect:/doctor/" + doctorId + "/appointments";
    }

    // Cancel appointment
    @PostMapping("/{doctorId}/appointments/{appointmentId}/cancel")
    public String cancelAppointment(@PathVariable long doctorId, @PathVariable int appointmentId) {
        Appointment appt = appointmentRepository.findById(appointmentId).orElse(null);
        if (appt != null) {
            appt.setStatus(Appointment.Status.CANCELLED);

            // Optional: send cancellation email
            Doctor doctor = appt.getDoctor();
            String patientEmail = appt.getPatient().getEmail();
            String subject = "Appointment Cancelled";
            String body = String.format(
                    "Hello %s,\n\nYour appointment with Dr. %s on %s at %s has been cancelled.\n\nRegards,\nCloudCare Team",
                    appt.getPatient().getName(),
                    doctor.getName(),
                    appt.getAppointmentDate(),
                    appt.getTimeSlot()
            );
            emailService.sendEmail(patientEmail, subject, body);

            appointmentRepository.save(appt);
        }
        return "redirect:/doctor/" + doctorId + "/appointments";
    }
}