package com.example.cloud.care.var;

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
    private String name;
    private String specialization;
    private String profileImage;
    private String degrees;
    private String doctorId;
    private String medicalCollege;
    private String hospitalName;
    private String phoneNumber;
    private String email;
    private String workingHours;
    private String credentials;
    private String specialities;
    private String description;

}
