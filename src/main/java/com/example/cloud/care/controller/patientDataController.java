package com.example.cloud.care.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.cloud.care.model.Patient;
import com.example.cloud.care.service.PhotoUploadService;
import com.example.cloud.care.service.UserService;
import com.example.cloud.care.service.loggedInUserFind;
import com.example.cloud.care.service.patient_service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@Controller
@RequestMapping("/patient")
public class patientDataController {

    @Autowired
    private patient_service patientService;

    @Autowired
    private UserService userService;



    @Autowired
    private PhotoUploadService photoUploadService;

    @Autowired
    private loggedInUserFind logger;



    @Autowired
    private Cloudinary cloudinary;

    @GetMapping("/dataEntry")
    public String showPatientDataEntryForm(Model model) {

        Patient patient = logger.logger();
        model.addAttribute("patient", patient);

        // You can add a default patient or handle patient selection logic here
        return "patient_data_intry_form";
    }


    @PostMapping("/picEdit")
    public String handleSelfieUpload(
            @RequestParam(required = false) String base64Image,
            @RequestParam(required = false) String skip,
            RedirectAttributes redirectAttributes) {

        Patient patient = logger.logger();
        System.out.println("CAME TO UPLOAD PART------------------");

        // ---- UPLOAD ----
        if (base64Image != null && !base64Image.isEmpty()) {
            try {
                String cleanBase64 = base64Image.replaceFirst("^data:image/\\w+;base64,", "");
                byte[] imageBytes = Base64.getDecoder().decode(cleanBase64);

                Map upload = cloudinary.uploader().upload(
                        imageBytes,
                        ObjectUtils.asMap("folder", "patients")
                );

                patient.getUser().setPhotoUrl(upload.get("secure_url").toString());
                patientService.save(patient);

                System.out.println("Image Uploaded----------------------");
                redirectAttributes.addFlashAttribute("successMessage", "Photo uploaded successfully!");
                return "redirect:/patient/dataEntry";

            } catch (Exception e) {
                System.out.println("Image upload failed------------------------");
                redirectAttributes.addFlashAttribute("errorMessage", "Upload failed: " + e.getMessage());
                return "redirect:/patient/dataEntry";
            }
        }

        redirectAttributes.addFlashAttribute("errorMessage", "No image found!");
        return "redirect:/patient/dataEntry";
    }

    // Update Personal Information
    @PostMapping("/UpdatePersonalInfo")
    public String updatePersonalInfo(
            @RequestParam(required = false) String bloodGroup,
            @RequestParam(required = false) Integer age,
            @RequestParam(required = false) Character gender,
            @RequestParam(required = false) String division,
            @RequestParam(required = false) String zilla,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) Double weight,
            @RequestParam(required = false) Double height,
            @RequestParam(required = false) String nationalId,
            @RequestParam(required = false) String emergencyContact,
            @RequestParam(required = false) String fatherName,
            @RequestParam(required = false) String motherName,
            @RequestParam(required = false) String maritalStatus,
            @RequestParam(required = false) String dateOfBirth,
            @RequestParam(required = false) String lastDonated,
            RedirectAttributes redirectAttributes) {

        try {
            Patient patient = logger.logger(); // Get logged in patient
            if (patient != null) {
                // Update personal information fields
                if (bloodGroup != null) patient.setBloodGroup(bloodGroup);
                if (age != null) patient.setAge(age);
                if (gender != null) patient.setGender(gender);
                if (division != null) patient.setDivision(division);
                if (zilla != null) patient.setZilla(zilla);
                if (address != null) patient.setAddress(address);
                if (weight != null) patient.setWeight(weight);
                if (height != null) patient.setHeight(height);
                if (nationalId != null) patient.setNationalId(nationalId);
                if (emergencyContact != null) patient.setEmergencyContact(emergencyContact);
                if (fatherName != null) patient.setFatherName(fatherName);
                if (motherName != null) patient.setMotherName(motherName);
                if (maritalStatus != null) patient.setMaritalStatus(maritalStatus);
                if(height != null && weight != null && height > 0) {
                    double heightInMeters = height / 100.0; // Convert cm to meters
                    double bmi = weight / (heightInMeters * heightInMeters);
                    patient.setBmi(bmi);
                }
                else {
                    patient.setBmi(null);
                }
                // Parse dates
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                if (dateOfBirth != null && !dateOfBirth.isEmpty()) {
                    patient.setDateOfBirth(dateFormat.parse(dateOfBirth));
                }
                if (lastDonated != null && !lastDonated.isEmpty()) {
                    patient.setLastDonated(dateFormat.parse(lastDonated));
                }

                patient.setUpdatedAt(new Date());
                patientService.save(patient);

                redirectAttributes.addFlashAttribute("successMessage", "Personal information updated successfully!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Patient not found!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating personal information: " + e.getMessage());
        }

        return "redirect:/patient/dataEntry";
    }

    // Update Vital Signs
    @PostMapping("/VitalSign")
    public String updateVitalSigns(
            @RequestParam(required = false) Integer systolicBP,
            @RequestParam(required = false) Integer diastolicBP,
            @RequestParam(required = false) Integer heartRate,
            @RequestParam(required = false) Integer oxygenSaturation,
            @RequestParam(required = false) Double respiratoryRate,
            @RequestParam(required = false) Double bodyTemperature,
            @RequestParam(required = false) Double bloodSugarLevel,
            RedirectAttributes redirectAttributes) {

        try {
            Patient patient = logger.logger(); // Get logged in patient
            if (patient != null) {
                // Update vital signs
                if (systolicBP != null) patient.setSystolicBP(systolicBP);
                if (diastolicBP != null) patient.setDiastolicBP(diastolicBP);
                if (heartRate != null) patient.setHeartRate(heartRate);
                if (oxygenSaturation != null) patient.setOxygenSaturation(oxygenSaturation);
                if (respiratoryRate != null) patient.setRespiratoryRate(respiratoryRate);
                if (bodyTemperature != null) patient.setBodyTemperature(bodyTemperature);
                if (bloodSugarLevel != null) patient.setBloodSugarLevel(bloodSugarLevel);

                patient.setUpdatedAt(new Date());
                patientService.save(patient);

                redirectAttributes.addFlashAttribute("successMessage", "Vital signs updated successfully!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Patient not found!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating vital signs: " + e.getMessage());
        }

        return "redirect:/patient/dataEntry";
    }

    // Update Lifestyle Information
    @PostMapping("/LifeStyleInfo")
    public String updateLifestyleInfo(
            @RequestParam(required = false) Double sleepHours,
            @RequestParam(required = false) Double waterIntake,
            @RequestParam(required = false) Integer physicalActivity,
            @RequestParam(defaultValue = "false") boolean smoker,
            @RequestParam(defaultValue = "false") boolean alcoholConsumer,
            RedirectAttributes redirectAttributes) {

        try {
            Patient patient = logger.logger(); // Get logged in patient
            if (patient != null) {
                // Update lifestyle information
                if (sleepHours != null) patient.setSleepHours(sleepHours);
                if (waterIntake != null) patient.setWaterIntake(waterIntake);
                if (physicalActivity != null) patient.setPhysicalActivity(physicalActivity);
                patient.setSmoker(smoker);
                patient.setAlcoholConsumer(alcoholConsumer);

                patient.setUpdatedAt(new Date());
                patientService.save(patient);

                redirectAttributes.addFlashAttribute("successMessage", "Lifestyle information updated successfully!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Patient not found!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating lifestyle information: " + e.getMessage());
        }

        return "redirect:/patient/dataEntry";
    }

    // Update Menstrual Cycle Information
    @PostMapping("/MenstrualCycleInfo")
    public String updateMenstrualCycleInfo(
            @RequestParam(defaultValue = "false") boolean pregnant,
            @RequestParam(defaultValue = "false") boolean breastFeeding,
            @RequestParam(defaultValue = "false") boolean menstruationRegular,
            @RequestParam(required = false) Integer pregnancyWeek,
            @RequestParam(required = false) String lastMenstrualDate,
            RedirectAttributes redirectAttributes) {

        try {
            Patient patient = logger.logger(); // Get logged in patient
            if (patient != null && patient.getGender() == 'F') {
                // Update menstrual cycle information
                patient.setPregnant(pregnant);
                patient.setBreastFeeding(breastFeeding);
                patient.setMenstruationRegular(menstruationRegular);
                if (pregnancyWeek != null) patient.setPregnancyWeek(pregnancyWeek);

                // Parse date
                if (lastMenstrualDate != null && !lastMenstrualDate.isEmpty()) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    patient.setLastMenstrualDate(dateFormat.parse(lastMenstrualDate));
                }

                patient.setUpdatedAt(new Date());
                patientService.save(patient);

                redirectAttributes.addFlashAttribute("successMessage", "Menstrual cycle information updated successfully!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Patient not found or not female!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating menstrual cycle information: " + e.getMessage());
        }

        return "redirect:/patient/dataEntry";
    }

    // Update Environment & Family Information
    @PostMapping("/EnviromentFamilyInfo")
    public String updateEnvironmentFamilyInfo(
            @RequestParam(required = false) String parentalCareState,
            @RequestParam(required = false) String homeEnvironment,
            @RequestParam(defaultValue = "false") boolean haveBeenBullied,
            @RequestParam(defaultValue = "false") boolean feelingLeftOut,
            RedirectAttributes redirectAttributes) {

        try {
            Patient patient = logger.logger(); // Get logged in patient
            if (patient != null) {
                // Update environment and family information
                if (parentalCareState != null) patient.setParentalCareState(parentalCareState);
                if (homeEnvironment != null) patient.setHomeEnvironment(homeEnvironment);
                patient.setHaveBeenBullied(haveBeenBullied);
                patient.setFeelingLeftOut(feelingLeftOut);

                patient.setUpdatedAt(new Date());
                patientService.save(patient);

                redirectAttributes.addFlashAttribute("successMessage", "Environment & family information updated successfully!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Patient not found!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating environment & family information: " + e.getMessage());
        }

        return "redirect:/patient/dataEntry";
    }
}