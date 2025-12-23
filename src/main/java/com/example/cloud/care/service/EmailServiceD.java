package com.example.cloud.care.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import com.example.cloud.care.model.Appointment;
import com.example.cloud.care.model.Doctor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmailServiceD {

    @Value("${brevo.api.key}")
    private String apiKey;

    @Value("${brevo.sender.email}")
    private String senderEmail;

    @Value("${brevo.sender.name}")
    private String senderName;

    private static final String BREVO_URL = "https://api.brevo.com/v3/smtp/email";

    private final RestTemplate restTemplate = new RestTemplate();

    @Async
    public void sendEmail(String to, String subject, String body) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", apiKey);

            Map<String, Object> payload = new HashMap<>();
            payload.put("sender", Map.of("email", senderEmail, "name", senderName));
            payload.put("to", List.of(Map.of("email", to)));
            payload.put("subject", subject);
            payload.put("textContent", body);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(BREVO_URL, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("Email sent to " + to);
            } else {
                System.err.println("Brevo error: " + response.getStatusCode() + " - " + response.getBody());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendAppointmentConfirmation(Appointment appt) {
        Doctor doctor = appt.getDoctor();
        String patientEmail = appt.getPatient().getUser().getEmail();
        String patientName = appt.getPatient().getUser().getName();

        String subject = "Appointment Confirmed - CloudCare";
        String body;

        if (appt.getType() == Appointment.AppointmentType.TELEMEDICINE) {
            body = String.format(
                    "Hello %s,\n\nYour telemedicine appointment with Dr. %s has been confirmed.\n" +
                    "Date: %s\nTime: %s\n\nJoin via this link: %s\n\nRegards,\nCloudCare Team",
                    patientName, doctor.getName(), appt.getAppointmentDate(),
                    appt.getTimeSlot(), appt.getTelemedicineLink()
            );
        } else { // Physical appointment
            body = String.format(
                    "Hello %s,\n\nYour appointment with Dr. %s has been confirmed.\n" +
                    "Date: %s\nTime: %s\nHospital: %s\n\nRegards,\nCloudCare Team",
                    patientName, doctor.getName(), appt.getAppointmentDate(),
                    appt.getTimeSlot(), doctor.getHospitalName()
            );
        }

        sendEmail(patientEmail, subject, body);
    }

    public void sendAppointmentCancellation(Appointment appt) {
        Doctor doctor = appt.getDoctor();
        String patientEmail = appt.getPatient().getUser().getEmail();
        String patientName = appt.getPatient().getUser().getName();

        String subject = "Appointment Cancelled - CloudCare";
        String body = String.format(
                "Hello %s,\n\nYour appointment with Dr. %s on %s at %s has been cancelled.\n\nRegards,\nCloudCare Team",
                patientName, doctor.getName(), appt.getAppointmentDate(), appt.getTimeSlot()
        );

        sendEmail(patientEmail, subject, body);
    }
}