package com.example.cloud.care.controller;

import com.example.cloud.care.dao.patient_dao;
import com.example.cloud.care.service.doctor_service;
import com.example.cloud.care.service.patient_service;
import com.example.cloud.care.var.patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
    patient_service patientService;

    @Autowired
    doctor_service doctor_service;

    @Autowired
    patient_dao patient_dao;

//    @GetMapping({ "/dashboard", "", "/" })
//    public String home(Model m) {
//
//        m.addAttribute("doctor", doctor_service.getDoctorByID(1));
//
//        return "dashboard";
//    }


    @GetMapping({"/patient"})
    public String patient(Model m){

        return "patient";
    }

    @PostMapping("/patient/getPatientData")
    public String getPatientData(int patientId, Model m) {

        patient patientData = patientService.getPatientData(patientId);
        m.addAttribute("patient", patientData);

        return "patient";
    }

    @GetMapping("/patient_data_entry")
    public String showForm(Model model) {
        model.addAttribute("patient", new patient()); // prevents Thymeleaf binding errors
        return "patient_data_entry";
    }

    @GetMapping("/patient/getPatientProfileData")
    public String getPatientProfileData(@RequestParam int patientId, Model model) {
        try {
            patient p = patientService.getPatientData(patientId);
            if (p == null) {
                model.addAttribute("error", "No patient found with ID: " + patientId);
                return "patient"; // stays on same page with message
            }
            model.addAttribute("patient", p);
            return "patient";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "An error occurred while fetching data.");
            return "patient";
        }
    }


    @PostMapping("/editPatientData")
    public String editPatientData(@ModelAttribute patient patient, Model model) {
        try {
            patient existingPatient = patientService.getPatientData(patient.getPatientId());
            if (existingPatient != null) {
                model.addAttribute("patient", existingPatient);
            } else {
                model.addAttribute("error", "No patient found with ID: " + patient.getPatientId());
            }
        } catch (Exception e) {
            model.addAttribute("error", "Something went wrong while fetching patient data.");
        }
        return "patient_data_entry";
    }


    @PostMapping("/patient/fetch/patientId")
    public String fetchPatientData(@RequestParam("patientId") int patientId, Model model) {
        try {
            patient p = patientService.getPatientData(patientId);
            if (p != null) {
                model.addAttribute("patient", p);
            } else {
                model.addAttribute("error", "No patient found with ID: " + patientId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "An error occurred while fetching patient data.");
        }
        return "patient_data_entry";
    }

    @GetMapping("/patient/fetch/patientId")
    public String fetchPatientDataget(@RequestParam("patientId") int patientId, Model model) {
        try {
            patient p = patientService.getPatientData(patientId);
            if (p != null) {
                model.addAttribute("patient", p);
            } else {
                model.addAttribute("error", "No patient found with ID: " + patientId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "An error occurred while fetching patient data.");
        }
        return "patient_data_entry";
    }

    // Patient photo upload
    @PostMapping("/patient/uploadPhoto")
    public String uploadProfileImage(
            Model model,
            @RequestParam("patientId") int patientId,
            @RequestParam("profileImage") MultipartFile file) {

        try {
            if (file.isEmpty()) {
                model.addAttribute("error", "Please select a file to upload");
                return "redirect:/patient/fetch/patientId?patientId=" + patientId;
            }

            // Create the upload directory if it doesn't exist
            String uploadDir = "uploads/images/";
            Files.createDirectories(Paths.get(uploadDir));

            // Generate a unique filename to prevent overwriting
            String originalFilename = file.getOriginalFilename();
            String fileName = patientId + "_" + System.currentTimeMillis();
            
            // Add file extension if present
            if (originalFilename != null && originalFilename.contains(".")) {
                fileName += originalFilename.substring(originalFilename.lastIndexOf("."));
            } else {
                fileName += ".jpg"; // Default extension if none provided
            }
            
            Path filePath = Paths.get(uploadDir, fileName);

            // Save the file
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Update database record
            patient patient = patientService.getPatientData(patientId);
            if (patient == null) {
                throw new RuntimeException("Patient not found with ID: " + patientId);
            }
            
            // Delete old image if it exists
            if (patient.getProfileImage() != null) {
                Path oldFilePath = Paths.get(uploadDir, patient.getProfileImage());
                Files.deleteIfExists(oldFilePath);
            }
            
            patient.setProfileImage(fileName);
            patient_dao.save(patient);
            
            model.addAttribute("success", "Profile image updated successfully");
            return "redirect:/patient/fetch/patientId?patientId=" + patientId;

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to upload image: " + e.getMessage());
            return "redirect:/patient/fetch/patientId?patientId=" + patientId;
        }
    }
}