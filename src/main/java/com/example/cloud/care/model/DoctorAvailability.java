package com.example.cloud.care.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalTime;
import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

@Data
@Entity
@Table(name = "doctor_availability")
public class DoctorAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    

    private LocalTime startTime;
    private LocalTime endTime;
    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date date;

    private Boolean telemedicineAvailable=false;
    private boolean booked;

// getter & setter
public boolean isBooked() { return booked; }
public void setBooked(boolean booked) { this.booked = booked; }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;
}