package com.example.cloud.care.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name = "PATIENT")
public class Patient {

    // Primary key same as User ID
    @Id
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private User user;

    // Personal Info
    private String bloodGroup = "";
    private Integer age = 0;
    private Character gender = 'U'; // U = unknown
    private String division = "";
    private String zilla = "";
    private String address = "";
    private Double weight = 0.0;
    private Double height = 0.0;
    private String nationalId = "";
    private String emergencyContact = "";
    private String fatherName = "";
    private String motherName = "";
    private String fatherId = "";
    private String motherId = "";
    private String maritalStatus = "";
    private Date lastDonated;
    private Date dateOfBirth;

    // Blood & Biochemistry
    private Double rbc = 0.0;
    private Double platelet = 0.0;
    private Double triglyceride = 0.0;
    private Double hb = 0.0;
    private Double creatinineLevel = 0.0;
    private Double wbc = 0.0;
    private Double sugarLevel = 0.0;
    private String rhFactor = "";
    private Double cholesterolTotal = 0.0;
    private Double cholesterolLDL = 0.0;
    private Double cholesterolHDL = 0.0;
    private Double creatinine = 0.0;
    private Integer systolicBP = 0;
    private Integer diastolicBP = 0;
    private Integer heartRate = 0;
    private Integer oxygenSaturation = 0;
    private Double respiratoryRate = 0.0;
    private Double bloodSugarLevel = 0.0;
    private Double alt = 0.0;
    private Double ast = 0.0;
    private Double bilirubin = 0.0;

    // Common Criterion
    private String bloodPressure = "";
    private Double bmi = 0.0;
    private Double bmr = 0.0;
    private Double cholesterol = 0.0;
    private Double bloodSugar = 0.0;

    // Lifestyle Info
    private Double sleepHours = 0.0;
    private Double waterIntake = 0.0;

    // Menstrual Cycle
    private Boolean pregnant = false;
    private Integer pregnancyWeek = 0;
    private Boolean breastFeeding = false;
    private Boolean menstruationRegular = false;
    private Date lastMenstrualDate;

    // Mental Health
    private Double serotonin = 0.0;
    private Double dopamine = 0.0;
    private Double norepinephrine = 0.0;
    private Double cortisol = 0.0;
    private Double bdnf = 0.0;
    private Integer stressLevel = 0;
    private Integer anxietyScore = 0;
    @ElementCollection
    private List<String> mentalDiseases = new ArrayList<>();

    // Environment / Family Info
    private String parentalCareState = "";
    private String homeEnvironment = "";
    private Boolean haveBeenBullied = false;
    private Boolean feelingLeftOut = false;

    // Timestamps
    private Date createdAt = new Date();
    private Date updatedAt = new Date();

    // Lifestyle
    private boolean smoker = false;
    private boolean alcoholConsumer = false;
    private Integer physicalActivity = 0;

    // Lists
    @ElementCollection
    private List<String> allergies = new ArrayList<>();
    @ElementCollection
    private List<String> diseases = new ArrayList<>();
    @ElementCollection
    private List<String> medications = new ArrayList<>();
    @ElementCollection
    private List<String> symptoms = new ArrayList<>();
    @ElementCollection
    private List<String> familyDiseaseHistory = new ArrayList<>();
    @ElementCollection
    private List<String> vaccinationHistory = new ArrayList<>();
    @ElementCollection
    private List<String> radiologyReports = new ArrayList<>();
    @ElementCollection
    private List<String> labReportFiles = new ArrayList<>();
    @ElementCollection
    private List<String> appointment = new ArrayList<>();
    @ElementCollection
    private List<String> recentUpdates = new ArrayList<>();
    @ElementCollection
    private List<String> pastSurgeries = new ArrayList<>();

    // Vital signs
    private Double bodyTemperature = 0.0;
    private Double respiratoryRateLatest = 0.0;
    private Integer heartRateLatest = 0;

    // Constructor for signup
    public Patient(User user) {
        this.user = user;
    }

    // Default constructor for JPA
    public Patient() {}
}