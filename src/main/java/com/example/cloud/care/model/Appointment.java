package com.example.cloud.care.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;
import org.springframework.format.annotation.DateTimeFormat;

@Data
@Entity
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // Link to Doctor
    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    // Link to Patient (User entity)
    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date appointmentDate;

    private String timeSlot; // "09:00-10:00"

    @Enumerated(EnumType.STRING)
    private AppointmentType type; // physical / telemedicine

    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    private String telemedicineLink; // optional per session

    public enum AppointmentType {
        PHYSICAL,
        TELEMEDICINE
    }

    public enum Status {
        PENDING,
        CONFIRMED,
        CANCELLED,
        COMPLETED
    }
}