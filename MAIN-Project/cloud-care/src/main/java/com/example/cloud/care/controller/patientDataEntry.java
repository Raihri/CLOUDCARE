package com.example.cloud.care.controller;


import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.cloud.care.model.Patient;
import com.example.cloud.care.service.loggedInUserFind;
import com.example.cloud.care.service.patient_service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Base64;
import java.util.Map;

@Controller
@RequestMapping("/patient")
public class patientDataEntry {
    @Autowired
    private loggedInUserFind logger;

    @Autowired
    private patient_service patientService;



    @Autowired
    private Cloudinary cloudinary;

    @GetMapping("/dataEntry")
    public String showPatientDataEntryForm(Model model) {

        Patient patient = logger.logger();
        model.addAttribute("patient", patient);

        System.out.println("DATA ENTRY PART----------------");

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
                return "redirect:/dataEntry";

            } catch (Exception e) {
                System.out.println("Image upload failed------------------------");
                redirectAttributes.addFlashAttribute("errorMessage", "Upload failed: " + e.getMessage());
                return "redirect:/dataEntry";
            }
        }

        redirectAttributes.addFlashAttribute("errorMessage", "No image found!");
        return "redirect:/dataEntry";
    }
}
