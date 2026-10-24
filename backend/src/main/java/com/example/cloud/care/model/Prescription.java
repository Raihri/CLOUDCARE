package com.example.cloud.care.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * A prescription written by a doctor for a patient.
 *
 * <p>The clinical content is deliberately free text. A structured medicine
 * table (dose, frequency, duration per row) would be better for a production
 * e-prescribing system, but the demo needs the doctor to write a few lines and
 * pick the next visit date without a formulary.
 *
 * <p>{@code reminderSent} / {@code dueReminderSent} make the scheduled
 * notification job idempotent: a reminder is written to the patient's
 * notification feed exactly once for each stage.
 */
@Data
@Entity
@NoArgsConstructor
@Table(name = "prescriptions")
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    @JsonIgnore
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    @JsonIgnore
    private Doctor doctor;

    @Column(columnDefinition = "TEXT")
    private String diagnosis;

    @Column(columnDefinition = "TEXT")
    private String medicines;

    @Column(columnDefinition = "TEXT")
    private String instructions;

    /** The date the doctor wants to see the patient again. */
    private LocalDate followUpDate;

    private LocalDateTime createdAt = LocalDateTime.now();

    /** Set once the "coming up in a few days" notification has been sent. */
    private boolean reminderSent = false;

    /** Set once the "due today" notification has been sent. */
    private boolean dueReminderSent = false;

    public Prescription(Patient patient, Doctor doctor, String diagnosis,
                        String medicines, String instructions, LocalDate followUpDate) {
        this.patient = patient;
        this.doctor = doctor;
        this.diagnosis = diagnosis;
        this.medicines = medicines;
        this.instructions = instructions;
        this.followUpDate = followUpDate;
        this.createdAt = LocalDateTime.now();
    }
}
