package com.example.cloud.care.controller;

import com.example.cloud.care.dao.patient_dao;
import com.example.cloud.care.service.doctor_service;
import com.example.cloud.care.service.patient_service;
import com.example.cloud.care.var.doctor;
import com.example.cloud.care.var.patient;
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
    patient_service patientService;

    @Autowired
    doctor_service doctor_service;

    @Autowired
    patient_dao patient_dao;

    @GetMapping({ "/doc" })
    public String home(Model m) {

        m.addAttribute("doctor", doctor_service.getDoctorByID(1));

        return "dashboard";
    }

    @GetMapping({ "/patient" })
    public String patient(Model m) {

        return "patient";
    }

    @PostMapping("/patient/getPatientData")
    public String getPatientData(int patientId, Model m) {

        patient patientData = patientService.getPatientData(patientId);
        m.addAttribute("patient", patientData);

        return "patient";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("message", "Welcome to your CloudCare Dashboard!");
        // model.addAttribute("doctor", doctor_service.getDoctorByID(1));
        model.addAttribute("doctor", doctor_service.getDoctorByID(1));

        return "dashboard";
    }

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

    @PostMapping("/doctor/{id}/upload-photo")
    public String uploadDoctorPhoto(
            @PathVariable("id") int id,
            @RequestParam("profileImage") MultipartFile file,
            Model model) {
        try {
            if (file.isEmpty()) {
                model.addAttribute("error", "Please select a file to upload");
                return "redirect:/doctor/" + id;
            }

            // Get the doctor
            doctor doc = doctor_service.getDoctorByID(id);
            if (doc == null) {
                return "redirect:/list";
            }

            // Create doctor-pic-uploads directory if it doesn't exist
            Path uploadDir = Paths.get(System.getProperty("user.dir"), "MAIN", "cloud-care", "doctor-pic-uploads");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // Generate unique filename
            String fileName = id + "_" + System.currentTimeMillis() + "_"
                    + file.getOriginalFilename().replaceAll("\\s+", "_");
            Path filePath = uploadDir.resolve(fileName);

            // Delete old image if exists
            if (doc.getProfileImage() != null) {
                Path oldFilePath = uploadDir.resolve(doc.getProfileImage());
                Files.deleteIfExists(oldFilePath);
            }

            // Copy the file
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Update doctor profile
            doc.setProfileImage(fileName);
            doctor_service.saveDoctor(doc);

            return "redirect:/doctor/" + id;
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to upload image: " + e.getMessage());
            return "redirect:/doctor/" + id;
        }
    }

    @GetMapping("/doctor/{id}")
    public String getDoctorById(@PathVariable("id") int id, Model model) {
        doctor doc = doctor_service.getDoctorByID(id);
        if (doc == null) {
            // Handle case when doctor is not found
            return "redirect:/list";
        }
        System.out.println("Doctor found with ID: " + id);
        System.out.println("Doctor name: " + doc.getName());
        System.out.println("Doctor profile image: " + doc.getProfileImage());
        model.addAttribute("doctor", doc);
        return "view"; // Your Thymeleaf view for a single doctor
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

            // Create the upload directory inside MAIN if it doesn't exist
            String uploadDir = System.getProperty("user.dir") + "/MAIN/cloud-care/doctor-pic-uploads/";
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