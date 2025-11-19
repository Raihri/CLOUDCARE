package com.example.cloud.care.model;
import com.example.cloud.care.model.User;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name = "PATIENT")
@Inheritance(strategy = InheritanceType.JOINED)
public class Patient extends User {
    // Personal Information
    private String bloodGroup;
    private Integer age;
    private char gender;
    private String division;
    private String zilla;
    private String address;
    private Double weight;
    private Double height;
    private String nationalId;            // Bangladeshi NID
    private String emergencyContact;
    private String fatherName;
    private String motherName;
    private String fatherId;
    private String motherId;
    private String maritalStatus;
    private Date lastDonated;
    private Date dateOfBirth;

    // Blood & Biochemistry
    private Double rbc;
    private Double platelet;
    private Double triglyceride;
    private Double hb;
    private Double creatinineLevel;
    private Double wbc;
    private Double sugarLevel;
    private String rhFactor;
    private Double cholesterolTotal;
    private Double cholesterolLDL;
    private Double cholesterolHDL;
    private Double creatinine;       // lab value
    private Integer systolicBP;      // upper
    private Integer diastolicBP;     // lower
    private Integer heartRate;
    private Integer oxygenSaturation;      // SpO2 %
    private Double respiratoryRate;
    private Double bloodSugarLevel;        // fasting or random
    private Double alt;                    // liver SGPT
    private Double ast;                    // liver SGOT
    private Double bilirubin;

    // Common Criterion
    private String bloodPressure;
    private Double bmi;
    private Double bmr;
    private Double cholesterol;
    private Double bloodSugar;

    // Lifestyle Info
    private Double sleepHours;
    private Double waterIntake;

    // Menstrual Cycle
    private Boolean pregnant;
    private Integer pregnancyWeek;
    private Boolean breastFeeding;
    private Boolean menstruationRegular;
    private Date lastMenstrualDate;

    // Mental Health Corner
    private Double serotonin;
    private Double dopamine;
    private Double norepinephrine;
    private Double cortisol;
    private Double bdnf;
    private Integer stressLevel;
    private Integer anxietyScore;
    private List<String> mentalDiseases;

    // Environment / Family Info
    private String parentalCareState;
    private String homeEnvironment;
    private Boolean haveBeenBullied;
    private Boolean feelingLeftOut;

    // Timestamps
    private Date createdAt;
    private Date updatedAt;

    // Lifestyle
    private boolean smoker;
    private boolean alcoholConsumer;
    private Integer physicalActivity;

    // List Of All Diseases / Medications / Symptoms
    private List<String> allergies;
    private List<String> diseases;
    private List<String> medications;
    private List<String> symptoms;

    // Vital signs (single authoritative set)
    private Double bodyTemperature;
    private Double respiratoryRateLatest;
    private Integer heartRateLatest;

    // Family history
    private List<String> familyDiseaseHistory;

    // Report Images / Files
    private List<String> vaccinationHistory;
    private List<String> radiologyReports;     // X-ray, MRI URL
    private List<String> labReportFiles;       // uploaded PDF reports

    // Appointments & updates
    private List<String> appointment;
    private List<String> recentUpdates;
    private List<String> pastSurgeries;
}