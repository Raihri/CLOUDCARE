package com.example.cloud.care.var;

import java.util.Date;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Generated;

@Data
@Entity
@Table(name = "doctor")
public class doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // Basic
    private String name;
    private String profileImage;
    private String gender;
    private Date dob;
    private String bloodGroup;

    // Contact
    private String email;
    private String phoneNumber;
    private String altPhone;
    private String address;
    private String division;
    private String zilla;
    private String street;
    private String postalCode;
    private String emergencyContact;

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

}
