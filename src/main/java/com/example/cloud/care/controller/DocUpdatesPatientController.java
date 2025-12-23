package com.example.cloud.care.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.cloud.care.model.Doctor;
import com.example.cloud.care.model.Patient;
import com.example.cloud.care.service.DoctorUserDetails;
import com.example.cloud.care.service.notificationService;
import com.example.cloud.care.service.patient_service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import com.example.cloud.care.model.notification;

@Controller
@RequestMapping("/doctor/patientData")
public class DocUpdatesPatientController {

    @Autowired
    private patient_service patientService;

    @Autowired
    private notificationService notificationService;

    @Autowired
    private Cloudinary cloudinary;

        



    // Helper method to update timestamp
    private Map<String, Object> updateTimestampHelper(Long patientId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Optional<Patient> patientOptional = patientService.findById(patientId);
            if (!patientOptional.isPresent()) {
                response.put("success", false);
                response.put("message", "Patient not found");
                return response;
            }

            Patient patient = patientOptional.get();
            patient.setUpdatedAt(new Date());


            patientService.save(patient);

            response.put("success", true);
            response.put("message", "Timestamp updated successfully");
            response.put("updatedAt", patient.getUpdatedAt());

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }
        return response;
    }

    // Display patient profile
    @GetMapping("/{id}")
    public String showPatientProfile(@PathVariable Long id, Model model) {
        Optional<Patient> patientOptional = patientService.findById(id);

        if (patientOptional.isPresent()) {
            model.addAttribute("patient", patientOptional.get());
        } else {
            model.addAttribute("patient", null);
            model.addAttribute("errorMessage", "Patient not found with ID: " + id);
        }

        return "patient_data_entry_doc";
    }

    // Update Blood & Biochemistry
    @PostMapping("/update-biochemistry")
    @ResponseBody
    public Map<String, Object> updateBiochemistry(
            @RequestParam Long patientId,
            @RequestParam(required = false) Double rbc,
            @RequestParam(required = false) Double platelet,
            @RequestParam(required = false) Double triglyceride,
            @RequestParam(required = false) Double hb,
            @RequestParam(required = false) Double wbc,
            @RequestParam(required = false) Double sugarLevel,
            @RequestParam(required = false) String rhFactor,
            @RequestParam(required = false) Double cholesterolTotal,
            @RequestParam(required = false) Double creatinine,
            @RequestParam(required = false) Double alt,
            @RequestParam(required = false) Double ast,
            @RequestParam(required = false) Double bilirubin) {

        Map<String, Object> response = new HashMap<>();

        try {
            Optional<Patient> patientOptional = patientService.findById(patientId);
            if (!patientOptional.isPresent()) {
                response.put("success", false);
                response.put("message", "Patient not found");
                return response;
            }

            Patient patient = patientOptional.get();

            // Update biochemistry values
            if (rbc != null)
                patient.setRbc(rbc);
            if (platelet != null)
                patient.setPlatelet(platelet);
            if (triglyceride != null)
                patient.setTriglyceride(triglyceride);
            if (hb != null)
                patient.setHb(hb);
            if (wbc != null)
                patient.setWbc(wbc);
            if (sugarLevel != null)
                patient.setSugarLevel(sugarLevel);
            if (rhFactor != null)
                patient.setRhFactor(rhFactor);
            if (cholesterolTotal != null)
                patient.setCholesterolTotal(cholesterolTotal);
            if (creatinine != null)
                patient.setCreatinine(creatinine);
            if (alt != null)
                patient.setAlt(alt);
            if (ast != null)
                patient.setAst(ast);
            if (bilirubin != null)
                patient.setBilirubin(bilirubin);

            patient.setUpdatedAt(new Date());
            patientService.save(patient);
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            DoctorUserDetails userDetails = (DoctorUserDetails) auth.getPrincipal();
            Doctor doctor = userDetails.getDoctor();
            notification notify = new notification("Blood And Bio Chemistry Updated by Dr. " +doctor.getName(), LocalDateTime.now(),patient);
            notificationService.sendToSpecific(patient.getUser().getEmail(), notify);
            // notificationService.sendToAll(notify);

            patientService.addNotification(patient,notify);

            response.put("success", true);
            response.put("message", "Biochemistry data updated successfully");
            response.put("updatedAt", patient.getUpdatedAt());

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }

        return response;
    }

    // Update Common Criterion
    @PostMapping("/update-criteria")
    @ResponseBody
    public Map<String, Object> updateCriteria(
            @RequestParam Long patientId,
            @RequestParam(required = false) String bloodPressure,
            @RequestParam(required = false) Double bmi,
            @RequestParam(required = false) Double bmr,
            @RequestParam(required = false) Double cholesterol,
            @RequestParam(required = false) Double bloodSugar) {

        Map<String, Object> response = new HashMap<>();

        try {
            Optional<Patient> patientOptional = patientService.findById(patientId);
            if (!patientOptional.isPresent()) {
                response.put("success", false);
                response.put("message", "Patient not found");
                return response;
            }

            Patient patient = patientOptional.get();

            if (bloodPressure != null)
                patient.setBloodPressure(bloodPressure);
            if (bmi != null)
                patient.setBmi(bmi);
            if (bmr != null)
                patient.setBmr(bmr);
            if (cholesterol != null)
                patient.setCholesterol(cholesterol);
            if (bloodSugar != null)
                patient.setBloodSugar(bloodSugar);

            patient.setUpdatedAt(new Date());
            patientService.save(patient);
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            DoctorUserDetails userDetails = (DoctorUserDetails) auth.getPrincipal();
            Doctor doctor = userDetails.getDoctor();
            notification notify = new notification("Common Criterion Updated by Dr. " + doctor.getName(), LocalDateTime.now(),patient);
            patientService.addNotification(patient,notify);
            response.put("success", true);
            response.put("message", "Criteria updated successfully");
            response.put("updatedAt", patient.getUpdatedAt());

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }

        return response;
    }

    // Update Mental Health
    @PostMapping("/update-mental")
    @ResponseBody
    public Map<String, Object> updateMentalHealth(
            @RequestParam Long patientId,
            @RequestParam(required = false) Double serotonin,
            @RequestParam(required = false) Double dopamine,
            @RequestParam(required = false) Double norepinephrine,
            @RequestParam(required = false) Double cortisol,
            @RequestParam(required = false) Double bdnf,
            @RequestParam(required = false) Integer stressLevel,
            @RequestParam(required = false) Integer anxietyScore) {

        Map<String, Object> response = new HashMap<>();

        try {
            Optional<Patient> patientOptional = patientService.findById(patientId);
            if (!patientOptional.isPresent()) {
                response.put("success", false);
                response.put("message", "Patient not found");
                return response;
            }

            Patient patient = patientOptional.get();

            if (serotonin != null)
                patient.setSerotonin(serotonin);
            if (dopamine != null)
                patient.setDopamine(dopamine);
            if (norepinephrine != null)
                patient.setNorepinephrine(norepinephrine);
            if (cortisol != null)
                patient.setCortisol(cortisol);
            if (bdnf != null)
                patient.setBdnf(bdnf);
            if (stressLevel != null)
                patient.setStressLevel(stressLevel);
            if (anxietyScore != null)
                patient.setAnxietyScore(anxietyScore);

            patient.setUpdatedAt(new Date());
            patientService.save(patient);
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            DoctorUserDetails userDetails = (DoctorUserDetails) auth.getPrincipal();
            Doctor doctor = userDetails.getDoctor();
            notification notify = new notification("Mental Health Info Updated by Dr. " + doctor.getName(), LocalDateTime.now(),patient);
            patientService.addNotification(patient,notify);
            response.put("success", true);
            response.put("message", "Mental health data updated successfully");
            response.put("updatedAt", patient.getUpdatedAt());

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }

        return response;
    }

    // Add/Remove list items - UPDATED WITH SURGERIES
    @PostMapping("/update-lists")
    @ResponseBody
    public Map<String, Object> updateLists(
            @RequestParam Long patientId,
            @RequestParam String listType,
            @RequestParam String newItem) {

        Map<String, Object> response = new HashMap<>();

        try {
            Optional<Patient> patientOptional = patientService.findById(patientId);
            if (!patientOptional.isPresent()) {
                response.put("success", false);
                response.put("message", "Patient not found");
                return response;
            }

            Patient patient = patientOptional.get();

            switch (listType) {
                case "allergies":
                    if (!patient.getAllergies().contains(newItem)) {
                        patient.getAllergies().add(newItem);
                    }
                    break;
                case "diseases":
                    if (!patient.getDiseases().contains(newItem)) {
                        patient.getDiseases().add(newItem);
                    }
                    break;
                case "medications":
                    if (!patient.getMedications().contains(newItem)) {
                        patient.getMedications().add(newItem);
                    }
                    break;
                case "surgeries": // Added surgeries
                    if (!patient.getPastSurgeries().contains(newItem)) {
                        patient.getPastSurgeries().add(newItem);
                    }
                    break;
            }

            patient.setUpdatedAt(new Date());
            patientService.save(patient);
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            DoctorUserDetails userDetails = (DoctorUserDetails) auth.getPrincipal();
            Doctor doctor = userDetails.getDoctor();
            notification notify = new notification("Surgery List Updated by Dr. " + doctor.getName(), LocalDateTime.now(),patient);
            patientService.addNotification(patient,notify);
            response.put("success", true);
            response.put("message", "List updated successfully");
            response.put("updatedAt", patient.getUpdatedAt());

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }

        return response;
    }

    // Add mental disease
    @PostMapping("/add-mental-disease")
    @ResponseBody
    public Map<String, Object> addMentalDisease(
            @RequestParam Long patientId,
            @RequestParam String disease) {

        Map<String, Object> response = new HashMap<>();

        try {
            Optional<Patient> patientOptional = patientService.findById(patientId);
            if (!patientOptional.isPresent()) {
                response.put("success", false);
                response.put("message", "Patient not found");
                return response;
            }

            Patient patient = patientOptional.get();

            if (!patient.getMentalDiseases().contains(disease)) {
                patient.getMentalDiseases().add(disease);
            }

            patient.setUpdatedAt(new Date());
            patientService.save(patient);
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            DoctorUserDetails userDetails = (DoctorUserDetails) auth.getPrincipal();
            Doctor doctor = userDetails.getDoctor();
            notification notify = new notification("Mental Disease Updated by Dr. " + doctor.getName(), LocalDateTime.now(),patient);
            patientService.addNotification(patient,notify);
            response.put("success", true);
            response.put("message", "Mental disease added successfully");

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }

        return response;
    }

    // Remove list item - UPDATED WITH SURGERIES
    @PostMapping("/remove-list-item")
    @ResponseBody
    public Map<String, Object> removeListItem(
            @RequestParam Long patientId,
            @RequestParam String listType,
            @RequestParam String item) {

        Map<String, Object> response = new HashMap<>();

        try {
            Optional<Patient> patientOptional = patientService.findById(patientId);
            if (!patientOptional.isPresent()) {
                response.put("success", false);
                response.put("message", "Patient not found");
                return response;
            }

            Patient patient = patientOptional.get();

            switch (listType) {
                case "allergies":
                    patient.getAllergies().remove(item);
                    break;
                case "diseases":
                    patient.getDiseases().remove(item);
                    break;
                case "medications":
                    patient.getMedications().remove(item);
                    break;
                case "surgeries": // Added surgeries
                    patient.getPastSurgeries().remove(item);
                    break;
            }

            patient.setUpdatedAt(new Date());
            patientService.save(patient);
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            DoctorUserDetails userDetails = (DoctorUserDetails) auth.getPrincipal();
            Doctor doctor = userDetails.getDoctor();
            notification notify = new notification("Disease List Updated by Dr. "+ doctor.getName(), LocalDateTime.now(),patient);
            patientService.addNotification(patient,notify);
            response.put("success", true);
            response.put("message", "Item removed successfully");

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }

        return response;
    }

    // Upload files (using Cloudinary or similar)
    @PostMapping("/upload-files")
    @ResponseBody
    public Map<String, Object> uploadFiles(
            @RequestParam Long patientId,
            @RequestParam String fileType,
            @RequestParam("file") MultipartFile[] files) {

        Map<String, Object> response = new HashMap<>();

        try {
            Optional<Patient> patientOptional = patientService.findById(patientId);
            if (patientOptional.isEmpty()) {
                response.put("success", false);
                response.put("message", "Patient not found");
                return response;
            }

            Patient patient = patientOptional.get();

            for (MultipartFile file : files) {
                if (file.isEmpty())
                    continue;

                Map uploadResult = cloudinary.uploader().upload(
                        file.getBytes(),
                        ObjectUtils.asMap(
                                "folder", "patient_reports",
                                "resource_type", "raw"));

                String fileUrl = (String) uploadResult.get("secure_url");

                if ("labReport".equals(fileType)) {
                    patient.getLabReportFiles().add(fileUrl);
                }
            }

            patient.setUpdatedAt(new Date());
            patientService.save(patient);

            response.put("success", true);
            response.put("message", "Files uploaded successfully");

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }
    // Remove mental disease
    @PostMapping("/remove-mental-disease")
    @ResponseBody
    public Map<String, Object> removeMentalDisease(
            @RequestParam Long patientId,
            @RequestParam String disease) {

        Map<String, Object> response = new HashMap<>();

        try {
            Optional<Patient> patientOptional = patientService.findById(patientId);
            if (!patientOptional.isPresent()) {
                response.put("success", false);
                response.put("message", "Patient not found");
                return response;
            }

            Patient patient = patientOptional.get();

            patient.getMentalDiseases().remove(disease);
            patient.setUpdatedAt(new Date());
            patientService.save(patient);
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            DoctorUserDetails userDetails = (DoctorUserDetails) auth.getPrincipal();
            Doctor doctor = userDetails.getDoctor();
            notification notify = new notification("MENTAL DISEASE REMOVAL  Updated by Dr. " + doctor.getName(), LocalDateTime.now(),patient);
            patientService.addNotification(patient,notify);

            response.put("success", true);
            response.put("message", "Mental disease removed successfully");

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }

        return response;
    }

    // Update timestamp only (public endpoint if needed)
    @PostMapping("/update-timestamp")
    @ResponseBody
    public Map<String, Object> updateTimestamp(@RequestParam Long patientId) {
        return updateTimestampHelper(patientId);
    }

   
}