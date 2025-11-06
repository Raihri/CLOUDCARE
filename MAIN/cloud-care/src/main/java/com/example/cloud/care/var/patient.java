package com.example.cloud.care.var;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Date;
import java.util.List;

@Data
@Entity
@Table(name = "patient")
public class patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int patientId;

    // Image storage in PostgreSQL

    @Column(name = "profile_image")
    private String profileImage;

    // Basic Info
    // Email associated with patient (added so verification can link user->patient)
    private String email;
    private String name;
    private String parents;
    private String bloodGroup;
    private Integer age;
    private String gender;
    private String division;
    private String zilla;
    private Double weight;
    private Double height;
    private String emergencyContact;
    private String university;
    private String fatherName;
    private String motherName;
    private String fatherId;
    private String motherId;
    private String contact;
    private String status;
    private Date lastDonated;

    // Blood & Biochemistry
    private String bloodTest;
    private Double rbc;
    private Double platelet;
    private Double triglyceride;
    private Double hb;
    private Double creatinine;
    private Double wbc;
    private Double sugarLevel;
    private String rhFactor;
    private Double hemoglobin;

    // Common Criterion
    private String bloodPressure;
    private Double bmi;
    private Double bmr;
    private Double cholesterol;
    private Double bloodSugar;

    // Lifestyle Info
    private Integer sleepHours;
    private Double waterIntake;
    private String dietNotes;
    private String habits;

    // Mental Health Corner
    private Double serotonin;
    private Double dopamine;
    private Double norepinephrine;
    private Double cortisol;
    private Double bdnf;
    private Double mental_disease;

    // Environment / Family Info
    private String parentalCareState;
    private String parentsJob;
    private String homeEnvironment;
    private String friendCircle;
    private String stateRN;

    // Timestamps
    private Date createdAt;
    private Date updatedAt;

    // Doctor relationship
    @ManyToMany
    @JoinTable(name = "patient_doctor", joinColumns = @JoinColumn(name = "patient_id"), inverseJoinColumns = @JoinColumn(name = "doctor_id"))
    private List<doctor> assignedDoctors;

    // Getter for patientId (Lombok already provides, but okay to keep)
    public int getPatientId() {
        return patientId;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getProfileImage() {
        return profileImage;
    }
}