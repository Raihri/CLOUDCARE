package com.example.cloud.care.controller;

import com.example.cloud.care.dao.patient_dao;
import com.example.cloud.care.service.EmailService;
import com.example.cloud.care.service.doctor_service;
import com.example.cloud.care.service.patient_service;

import com.example.cloud.care.model.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

// imports trimmed

@Controller

public class homeController {

    @Autowired
    patient_service PatientService;

    @Autowired
    doctor_service doctor_service;

    @Autowired
    patient_dao Patient_dao;

    @Autowired
    EmailService emailService;

    @GetMapping({ "/doc" })
    public String home(Model m) {

        m.addAttribute("doctor", doctor_service.getDoctorByID(1));

        return "dashboard";
    }

    @GetMapping({ "/Patient" })
    public String Patient(Model m) {

        return "Patient";
    }

    // @PostMapping("/Patient/getPatientData")
    // public String getPatientData(int PatientId, Model m) {

    //     Patient PatientData = PatientService.getPatientData(PatientId);
    //     m.addAttribute("Patient", PatientData);

    //     return "Patient";
    // }

    // @GetMapping("/dashboard")
    // public String dashboard(Model model) {
    //     model.addAttribute("message", "Welcome to your CloudCare Dashboard!");
    //     // model.addAttribute("doctor", doctor_service.getDoctorByID(1));
    //     model.addAttribute("doctor", doctor_service.getDoctorByID(1));

    //     return "dashboard";
    // }

    @GetMapping("/list")
    public String listPatients(Model m) {
        var doctors = doctor_service.getDoctors();
        System.out.println("--------------------------------");
        System.out.println("Doctors List:");
        System.out.println("--------------------------------");

        System.out.println("Found " + doctors.size() + " doctors in database");
        m.addAttribute("doctors", doctors);
        return "list";
    }

    @GetMapping("/Patient_data_entry")
    public String showForm(Model model) {
        model.addAttribute("Patient", new Patient()); // prevents Thymeleaf binding errors
        return "Patient_data_entry";
    }

    // @GetMapping("/Patient/getPatientProfileData")
    // public String getPatientProfileData(@RequestParam int PatientId, Model model) {
    //     try {
    //         Patient p = PatientService.getPatientData(PatientId);
    //         if (p == null) {
    //             model.addAttribute("error", "No Patient found with ID: " + PatientId);
    //             return "Patient"; // stays on same page with message
    //         }
    //         model.addAttribute("Patient", p);
    //         return "Patient";
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //         model.addAttribute("error", "An error occurred while fetching data.");
    //         return "Patient";
    //     }
    // }

    // @PostMapping("/editPatientData")
    // public String editPatientData(@ModelAttribute Patient Patient, Model model) {
    //     try {
    //         Patient existingPatient = PatientService.getPatientData(Patient.getId());
    //         if (existingPatient != null) {
    //             model.addAttribute("Patient", existingPatient);
    //         } else {
    //             model.addAttribute("error", "No Patient found with ID: " + Patient.getId());
    //         }
    //     } catch (Exception e) {
    //         model.addAttribute("error", "Something went wrong while fetching Patient data.");
    //     }
    //     return "Patient_data_entry";
    // }

    // @PostMapping("/Patient/fetch/PatientId")
    // public String fetchPatientData(@RequestParam("PatientId") int PatientId, Model model) {
    //     try {
    //         Patient p = PatientService.getPatientData(PatientId);
    //         if (p != null) {
    //             model.addAttribute("Patient", p);
    //         } else {
    //             model.addAttribute("error", "No Patient found with ID: " + PatientId);
    //         }
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //         model.addAttribute("error", "An error occurred while fetching Patient data.");
    //     }
    //     return "Patient_data_entry";
    // }

  

   

    // @GetMapping("/Patient/fetch/PatientId")
    // public String fetchPatientDataget(@RequestParam("PatientId") int PatientId, Model model) {
    //     try {
    //         Patient p = PatientService.getPatientData(PatientId);
    //         if (p != null) {
    //             model.addAttribute("Patient", p);
    //         } else {
    //             model.addAttribute("error", "No Patient found with ID: " + PatientId);
    //         }
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //         model.addAttribute("error", "An error occurred while fetching Patient data.");
    //     }
    //     return "Patient_data_entry";
    // }

   
}