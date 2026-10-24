package com.example.cloud.care.dto;

import com.example.cloud.care.model.Doctor;
import com.example.cloud.care.model.Patient;
import com.example.cloud.care.model.Prescription;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Wire shape for a prescription. Built explicitly instead of serialising the
 * entity so the lazy patient/doctor graphs and password fields never leak.
 */
public record PrescriptionDto(
        Long id,
        Long patientId,
        String patientName,
        String patientEmail,
        Long doctorId,
        String doctorName,
        String doctorSpecialization,
        String hospitalName,
        String diagnosis,
        String medicines,
        String instructions,
        String followUpDate,
        String createdAt,
        Long daysUntilFollowUp,
        String followUpStatus
) {

    public static PrescriptionDto from(Prescription p) {
        Patient patient = p.getPatient();
        Doctor doctor = p.getDoctor();

        String patientName = null;
        String patientEmail = null;
        if (patient != null && patient.getUser() != null) {
            patientName = patient.getUser().getName();
            patientEmail = patient.getUser().getEmail();
        }

        Long daysUntil = null;
        String status = "NONE";
        LocalDate followUp = p.getFollowUpDate();
        if (followUp != null) {
            daysUntil = ChronoUnit.DAYS.between(LocalDate.now(), followUp);
            if (daysUntil < 0) {
                status = "OVERDUE";
            } else if (daysUntil == 0) {
                status = "DUE_TODAY";
            } else if (daysUntil <= 3) {
                status = "UPCOMING";
            } else {
                status = "SCHEDULED";
            }
        }

        return new PrescriptionDto(
                p.getId(),
                patient == null ? null : patient.getId(),
                patientName,
                patientEmail,
                doctor == null ? null : doctor.getId(),
                doctor == null ? null : doctor.getName(),
                doctor == null ? null : doctor.getSpecialization(),
                doctor == null ? null : doctor.getHospitalName(),
                p.getDiagnosis(),
                p.getMedicines(),
                p.getInstructions(),
                followUp == null ? null : followUp.toString(),
                p.getCreatedAt() == null ? null : p.getCreatedAt().toString(),
                daysUntil,
                status
        );
    }
}
