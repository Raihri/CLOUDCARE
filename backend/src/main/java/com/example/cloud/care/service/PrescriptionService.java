package com.example.cloud.care.service;

import com.example.cloud.care.model.Doctor;
import com.example.cloud.care.model.Patient;
import com.example.cloud.care.model.Prescription;
import com.example.cloud.care.model.Notification;
import com.example.cloud.care.repository.PrescriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Prescription writes and follow-up reminders.
 *
 * <p>Every notification is written to the database first and then pushed over
 * WebSocket. The push is best-effort; the patient still sees the reminder in
 * the notifications page after a refresh or reconnect.
 */
@Service
public class PrescriptionService {

    private static final Logger log = LoggerFactory.getLogger(PrescriptionService.class);

    private final PrescriptionRepository prescriptionRepository;
    private final PatientService patientService;
    private final NotificationService notificationService;

    public PrescriptionService(PrescriptionRepository prescriptionRepository,
                               PatientService patientService,
                               NotificationService notificationService) {
        this.prescriptionRepository = prescriptionRepository;
        this.patientService = patientService;
        this.notificationService = notificationService;
    }

    @Transactional
    public Prescription create(Doctor doctor,
                               Patient patient,
                               String diagnosis,
                               String medicines,
                               String instructions,
                               LocalDate followUpDate) {
        Prescription prescription = new Prescription(
                patient, doctor, trimToNull(diagnosis), medicines.trim(),
                trimToNull(instructions), followUpDate);

        // The creation notification already announces the follow-up date. If
        // that date is inside the reminder window, mark the corresponding
        // reminder as sent so the hourly job does not repeat it an hour later.
        if (followUpDate != null) {
            LocalDate today = LocalDate.now();
            prescription.setReminderSent(!followUpDate.isAfter(today.plusDays(3)));
            prescription.setDueReminderSent(!followUpDate.isAfter(today));
        }

        Prescription saved = prescriptionRepository.save(prescription);

        String message = "A new prescription from " + doctorDisplayName(doctor) + " is available.";
        if (followUpDate != null) {
            message += " Follow-up visit: " + followUpDate + ".";
        }
        notifyPatient(patient, message);

        return saved;
    }

    @Transactional(readOnly = true)
    public List<Prescription> forPatient(Long patientId) {
        return prescriptionRepository.findByPatientIdOrderByCreatedAtDesc(patientId);
    }

    @Transactional(readOnly = true)
    public List<Prescription> forDoctor(Long doctorId, Long patientId) {
        if (patientId == null) {
            return prescriptionRepository.findByDoctorIdOrderByCreatedAtDesc(doctorId);
        }
        return prescriptionRepository.findByDoctorIdAndPatientIdOrderByCreatedAtDesc(
                doctorId, patientId);
    }

    @Transactional(readOnly = true)
    public long countForPatient(Long patientId) {
        return prescriptionRepository.countByPatientId(patientId);
    }

    /**
     * Send the "coming up" and "due" reminders. Safe to call repeatedly: each
     * reminder stage is guarded by a flag on the prescription row.
     *
     * @return how many notifications were created
     */
    @Transactional
    public int sendDueReminders() {
        LocalDate today = LocalDate.now();
        int sent = 0;

        List<Prescription> upcoming = prescriptionRepository
                .findByReminderSentFalseAndFollowUpDateBetween(
                        today.plusDays(1), today.plusDays(3));

        for (Prescription p : upcoming) {
            String message = "Follow-up reminder: your visit with "
                    + doctorDisplayName(p.getDoctor())
                    + " is on " + p.getFollowUpDate()
                    + ". Please book an appointment if you haven't already.";
            notifyPatient(p.getPatient(), message);
            p.setReminderSent(true);
            prescriptionRepository.save(p);
            sent++;
        }

        List<Prescription> due = prescriptionRepository
                .findByDueReminderSentFalseAndFollowUpDateLessThanEqual(today);

        for (Prescription p : due) {
            String message;
            if (p.getFollowUpDate().isEqual(today)) {
                message = "Today is your follow-up date with "
                        + doctorDisplayName(p.getDoctor())
                        + ". Please contact the clinic if you need to reschedule.";
            } else {
                message = "Your follow-up with "
                        + doctorDisplayName(p.getDoctor())
                        + " was due on " + p.getFollowUpDate()
                        + ". Please contact the clinic to arrange a new visit.";
            }
            notifyPatient(p.getPatient(), message);
            p.setDueReminderSent(true);
            prescriptionRepository.save(p);
            sent++;
        }

        if (sent > 0) {
            log.info("Sent {} prescription follow-up reminder(s).", sent);
        }
        return sent;
    }

    private void notifyPatient(Patient patient, String message) {
        if (patient == null) return;

        Notification note = new Notification(message, LocalDateTime.now(), patient);
        patientService.addNotification(patient, note);

        if (patient.getUser() != null) {
            notificationService.sendToSpecific(patient.getUser().getEmail(), note);
        }
    }

    private static String doctorDisplayName(Doctor doctor) {
        String name = doctor == null || doctor.getName() == null
                ? ""
                : doctor.getName().trim();
        if (name.isEmpty()) {
            return "your doctor";
        }
        return name.startsWith("Dr.") ? name : "Dr. " + name;
    }

    private static String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
