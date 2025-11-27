package com.example.cloud.care.model;
import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

@Data
@Entity
@Table(name = "doctor")
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // Basic
    private String name;
    private String profileImage;
    private String gender;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date dob;
    private String bloodGroup;
    
    // Contact
    @Column(unique = true)
    private String email;
    private String phoneNumber;
    private String altPhone;
    private String address;
    private String division;
    private String zilla;
    private String street;
    private String postalCode;
    private String emergencyContact; 
    private String password;
    @Transient
    private String confirmPassword;
    @Column(unique = true)
    private String bmdcRegNo;

    // Professional
    private String degrees;
    private String specialization;
    private String specialities;
    private String education;
    private Integer experienceYears;
    private String certifications;
    private String languages;
    private String hospitalName;
    private String hospitalAddress;
    private String consultationFee;
    private String description;
    private String medicalCollege;

    // Availability & Online
    private String workingDays;
    private String workingHours;
    private String onlineAppointmentLink;
    private String leaveDates;
    private Boolean telemedicineAvailable;

    // Online & Social
    private String website;
    private String linkedin;
    private String facebook;
    private String instagram;
    private String twitter;

    // Additional
    private String awards;
    private String publications;
    private String specialInterests;
    private String notes;
    private String rating;

    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    public enum Status {
        PENDING,
        APPROVED,
        REJECTED
    }
}