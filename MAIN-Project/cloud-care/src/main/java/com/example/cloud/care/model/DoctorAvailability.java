package com.example.cloud.care.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalTime;

@Data
@Entity
@Table(name = "doctor_availability")
public class DoctorAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Day of the week: "MONDAY", "TUESDAY", ...
    private String day;

    private LocalTime startTime;
    private LocalTime endTime;

    private Boolean telemedicineAvailable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;
}