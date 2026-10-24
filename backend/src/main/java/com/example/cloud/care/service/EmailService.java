package com.example.cloud.care.service;

import com.example.cloud.care.model.Appointment;
import com.example.cloud.care.model.Doctor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The single outbound-mail service, backed by the Brevo transactional API.
 *
 * <p>Email is optional: when {@code BREVO_API_KEY} is not configured every send
 * is skipped with a log line, so the rest of the app runs without credentials.
 * Failures are logged and swallowed — a mail outage must never fail a request.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final String BREVO_URL = "https://api.brevo.com/v3/smtp/email";

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${brevo.api.key:}")
    private String apiKey;

    @Value("${brevo.sender.email:no-reply@cloudcare.local}")
    private String senderEmail;

    @Value("${brevo.sender.name:CloudCare}")
    private String senderName;

    /** False when no Brevo API key is set; sends are then skipped. */
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    /** Plain-text email, sent in the background. */
    @Async
    public void sendEmail(String to, String subject, String body) {
        send(to, subject, "textContent", body);
    }

    /** HTML email, sent in the background. */
    @Async
    public void sendHtmlEmailAsync(String to, String subject, String html) {
        send(to, subject, "htmlContent", html);
    }

    public void sendAppointmentConfirmation(Appointment appt) {
        Doctor doctor = appt.getDoctor();
        String patientName = appt.getPatient().getUser().getName();

        String body;
        if (appt.getType() == Appointment.AppointmentType.TELEMEDICINE) {
            body = String.format(
                    "Hello %s,\n\nYour telemedicine appointment with Dr. %s has been confirmed.\n"
                            + "Date: %s\nTime: %s\n\nJoin via this link: %s\n\nRegards,\nCloudCare Team",
                    patientName, doctor.getName(), appt.getAppointmentDate(),
                    appt.getTimeSlot(), appt.getTelemedicineLink());
        } else {
            body = String.format(
                    "Hello %s,\n\nYour appointment with Dr. %s has been confirmed.\n"
                            + "Date: %s\nTime: %s\nHospital: %s\n\nRegards,\nCloudCare Team",
                    patientName, doctor.getName(), appt.getAppointmentDate(),
                    appt.getTimeSlot(), doctor.getHospitalName());
        }
        send(appt.getPatient().getUser().getEmail(), "Appointment Confirmed - CloudCare", "textContent", body);
    }

    public void sendAppointmentCancellation(Appointment appt) {
        Doctor doctor = appt.getDoctor();
        String body = String.format(
                "Hello %s,\n\nYour appointment with Dr. %s on %s at %s has been cancelled.\n\nRegards,\nCloudCare Team",
                appt.getPatient().getUser().getName(), doctor.getName(),
                appt.getAppointmentDate(), appt.getTimeSlot());
        send(appt.getPatient().getUser().getEmail(), "Appointment Cancelled - CloudCare", "textContent", body);
    }

    private void send(String to, String subject, String contentField, String content) {
        if (!isConfigured()) {
            log.info("Email skipped (BREVO_API_KEY not set): '{}' to {}", subject, to);
            return;
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", apiKey);

            Map<String, Object> payload = new HashMap<>();
            payload.put("sender", Map.of("email", senderEmail, "name", senderName));
            payload.put("to", List.of(Map.of("email", to)));
            payload.put("subject", subject);
            payload.put(contentField, content);

            ResponseEntity<String> response =
                    restTemplate.postForEntity(BREVO_URL, new HttpEntity<>(payload, headers), String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Email sent to {}", to);
            } else {
                log.warn("Brevo returned {}: {}", response.getStatusCode(), response.getBody());
            }
        } catch (Exception e) {
            log.warn("Email to {} failed: {}", to, e.getMessage());
        }
    }
}
