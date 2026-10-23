package com.example.cloud.care.controller;

import com.example.cloud.care.model.Patient;
import com.example.cloud.care.service.StorageService;
import com.example.cloud.care.service.patient_service;
import com.example.cloud.care.service.loggedInUserFind;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * JSON API behind the Next.js health-records page.
 *
 * <p>Every write here deliberately mirrors an existing method in
 * {@link patientDataController} — same field names, same null-means-"leave
 * alone" semantics, same BMI calculation — but answers with JSON instead of a
 * Thymeleaf redirect. The two paths are kept in step deliberately rather than
 * the old ones being rewritten, so the legacy templates still work.
 *
 * <p>Reads are hand-rolled DTO-style maps rather than serialised
 * {@link Patient} entities: the entity carries {@code user} and would drag
 * credentials into the response.
 */
@Controller
@RequestMapping("/patient/api/health")
public class PatientHealthApiController {

    private static final String DATE_FMT = "yyyy-MM-dd";

    @Autowired
    private patient_service patientService;

    @Autowired
    private loggedInUserFind logger;

    @Autowired
    private StorageService storageService;

    /** Every health field the patient data-entry form can show or edit. */
    @GetMapping("")
    @ResponseBody
    public Map<String, Object> read() {
        Patient p = logger.logger();
        if (p == null) return error("Patient not found.");

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("success", true);

        out.put("name", p.getUser() != null ? p.getUser().getName() : null);
        out.put("email", p.getUser() != null ? p.getUser().getEmail() : null);
        out.put("photoUrl", p.getUser() != null ? p.getUser().getPhotoUrl() : null);

        out.put("bloodGroup", p.getBloodGroup());
        out.put("age", p.getAge());
        out.put("gender", p.getGender() != null ? String.valueOf(p.getGender()) : null);
        out.put("division", p.getDivision());
        out.put("zilla", p.getZilla());
        out.put("address", p.getAddress());
        out.put("weight", p.getWeight());
        out.put("height", p.getHeight());
        out.put("bmi", p.getBmi());
        out.put("nationalId", p.getNationalId());
        out.put("emergencyContact", p.getEmergencyContact());
        out.put("fatherName", p.getFatherName());
        out.put("motherName", p.getMotherName());
        out.put("maritalStatus", p.getMaritalStatus());
        out.put("dateOfBirth", fmt(p.getDateOfBirth()));
        out.put("lastDonated", fmt(p.getLastDonated()));

        out.put("systolicBP", p.getSystolicBP());
        out.put("diastolicBP", p.getDiastolicBP());
        out.put("heartRate", p.getHeartRate());
        out.put("oxygenSaturation", p.getOxygenSaturation());
        out.put("respiratoryRate", p.getRespiratoryRate());
        out.put("bodyTemperature", p.getBodyTemperature());
        out.put("bloodSugarLevel", p.getBloodSugarLevel());

        out.put("sleepHours", p.getSleepHours());
        out.put("waterIntake", p.getWaterIntake());
        out.put("physicalActivity", p.getPhysicalActivity());
        out.put("smoker", p.isSmoker());
        out.put("alcoholConsumer", p.isAlcoholConsumer());

        // These are Boolean wrappers on the entity, so Lombok generates getX()
        // rather than isX() — unlike smoker/alcoholConsumer, which are primitives.
        out.put("pregnant", p.getPregnant());
        out.put("breastFeeding", p.getBreastFeeding());
        out.put("menstruationRegular", p.getMenstruationRegular());
        out.put("pregnancyWeek", p.getPregnancyWeek());
        out.put("lastMenstrualDate", fmt(p.getLastMenstrualDate()));

        out.put("parentalCareState", p.getParentalCareState());
        out.put("homeEnvironment", p.getHomeEnvironment());
        out.put("haveBeenBullied", p.getHaveBeenBullied());
        out.put("feelingLeftOut", p.getFeelingLeftOut());

        return out;
    }

    @PostMapping("/personal")
    @ResponseBody
    public Map<String, Object> updatePersonal(
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
            @RequestParam(required = false) String lastDonated) {

        Patient p = logger.logger();
        if (p == null) return error("Patient not found.");

        try {
            if (bloodGroup != null) p.setBloodGroup(bloodGroup);
            if (age != null) p.setAge(age);
            if (gender != null) p.setGender(gender);
            if (division != null) p.setDivision(division);
            if (zilla != null) p.setZilla(zilla);
            if (address != null) p.setAddress(address);
            if (weight != null) p.setWeight(weight);
            if (height != null) p.setHeight(height);
            if (nationalId != null) p.setNationalId(nationalId);
            if (emergencyContact != null) p.setEmergencyContact(emergencyContact);
            if (fatherName != null) p.setFatherName(fatherName);
            if (motherName != null) p.setMotherName(motherName);
            if (maritalStatus != null) p.setMaritalStatus(maritalStatus);

            // Same BMI rule as the legacy controller: needs both, height in cm.
            if (height != null && weight != null && height > 0) {
                double m = height / 100.0;
                p.setBmi(weight / (m * m));
            } else {
                p.setBmi(null);
            }

            if (dateOfBirth != null && !dateOfBirth.isEmpty()) {
                p.setDateOfBirth(parse(dateOfBirth));
            }
            if (lastDonated != null && !lastDonated.isEmpty()) {
                p.setLastDonated(parse(lastDonated));
            }

            p.setUpdatedAt(new Date());
            patientService.save(p);
            return ok("Personal information updated successfully!");
        } catch (Exception e) {
            return error("Error updating personal information: " + e.getMessage());
        }
    }

    @PostMapping("/vitals")
    @ResponseBody
    public Map<String, Object> updateVitals(
            @RequestParam(required = false) Integer systolicBP,
            @RequestParam(required = false) Integer diastolicBP,
            @RequestParam(required = false) Integer heartRate,
            @RequestParam(required = false) Integer oxygenSaturation,
            @RequestParam(required = false) Double respiratoryRate,
            @RequestParam(required = false) Double bodyTemperature,
            @RequestParam(required = false) Double bloodSugarLevel) {

        Patient p = logger.logger();
        if (p == null) return error("Patient not found.");

        try {
            if (systolicBP != null) p.setSystolicBP(systolicBP);
            if (diastolicBP != null) p.setDiastolicBP(diastolicBP);
            if (heartRate != null) p.setHeartRate(heartRate);
            if (oxygenSaturation != null) p.setOxygenSaturation(oxygenSaturation);
            if (respiratoryRate != null) p.setRespiratoryRate(respiratoryRate);
            if (bodyTemperature != null) p.setBodyTemperature(bodyTemperature);
            if (bloodSugarLevel != null) p.setBloodSugarLevel(bloodSugarLevel);

            p.setUpdatedAt(new Date());
            patientService.save(p);
            return ok("Vital signs updated successfully!");
        } catch (Exception e) {
            return error("Error updating vital signs: " + e.getMessage());
        }
    }

    @PostMapping("/lifestyle")
    @ResponseBody
    public Map<String, Object> updateLifestyle(
            @RequestParam(required = false) Double sleepHours,
            @RequestParam(required = false) Double waterIntake,
            @RequestParam(required = false) Integer physicalActivity,
            @RequestParam(defaultValue = "false") boolean smoker,
            @RequestParam(defaultValue = "false") boolean alcoholConsumer) {

        Patient p = logger.logger();
        if (p == null) return error("Patient not found.");

        try {
            if (sleepHours != null) p.setSleepHours(sleepHours);
            if (waterIntake != null) p.setWaterIntake(waterIntake);
            if (physicalActivity != null) p.setPhysicalActivity(physicalActivity);
            p.setSmoker(smoker);
            p.setAlcoholConsumer(alcoholConsumer);

            p.setUpdatedAt(new Date());
            patientService.save(p);
            return ok("Lifestyle information updated successfully!");
        } catch (Exception e) {
            return error("Error updating lifestyle information: " + e.getMessage());
        }
    }

    @PostMapping("/menstrual")
    @ResponseBody
    public Map<String, Object> updateMenstrual(
            @RequestParam(defaultValue = "false") boolean pregnant,
            @RequestParam(defaultValue = "false") boolean breastFeeding,
            @RequestParam(defaultValue = "false") boolean menstruationRegular,
            @RequestParam(required = false) Integer pregnancyWeek,
            @RequestParam(required = false) String lastMenstrualDate) {

        Patient p = logger.logger();
        if (p == null) return error("Patient not found.");
        // Same gender guard as the legacy controller.
        if (p.getGender() != 'F') return error("This section only applies to female patients.");

        try {
            p.setPregnant(pregnant);
            p.setBreastFeeding(breastFeeding);
            p.setMenstruationRegular(menstruationRegular);
            if (pregnancyWeek != null) p.setPregnancyWeek(pregnancyWeek);
            if (lastMenstrualDate != null && !lastMenstrualDate.isEmpty()) {
                p.setLastMenstrualDate(parse(lastMenstrualDate));
            }

            p.setUpdatedAt(new Date());
            patientService.save(p);
            return ok("Menstrual cycle information updated successfully!");
        } catch (Exception e) {
            return error("Error updating menstrual cycle information: " + e.getMessage());
        }
    }

    @PostMapping("/environment")
    @ResponseBody
    public Map<String, Object> updateEnvironment(
            @RequestParam(required = false) String parentalCareState,
            @RequestParam(required = false) String homeEnvironment,
            @RequestParam(defaultValue = "false") boolean haveBeenBullied,
            @RequestParam(defaultValue = "false") boolean feelingLeftOut) {

        Patient p = logger.logger();
        if (p == null) return error("Patient not found.");

        try {
            if (parentalCareState != null) p.setParentalCareState(parentalCareState);
            if (homeEnvironment != null) p.setHomeEnvironment(homeEnvironment);
            p.setHaveBeenBullied(haveBeenBullied);
            p.setFeelingLeftOut(feelingLeftOut);

            p.setUpdatedAt(new Date());
            patientService.save(p);
            return ok("Environment & family information updated successfully!");
        } catch (Exception e) {
            return error("Error updating environment & family information: " + e.getMessage());
        }
    }

    @PostMapping("/photo")
    @ResponseBody
    public Map<String, Object> updatePhoto(@RequestParam("file") MultipartFile file) {

        Patient p = logger.logger();
        if (p == null) return error("Patient not found.");
        if (p.getUser() == null) return error("User account not found.");

        if (file == null || file.isEmpty()) {
            return error("Please choose a photo to upload.");
        }
        // The frontend constrains this too, but a client-side guard is not a
        // boundary — this endpoint is reachable directly.
        if (file.getSize() > 5L * 1024 * 1024) {
            return error("Photo must be smaller than 5 MB.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return error("Only image files are accepted.");
        }

        try {
            String url = storageService.upload(file, "patients");
            p.getUser().setPhotoUrl(url);
            p.setUpdatedAt(new Date());
            patientService.save(p);

            Map<String, Object> m = ok("Profile photo updated!");
            m.put("photoUrl", url);
            return m;
        } catch (IOException e) {
            return error("Could not upload the photo: " + e.getMessage());
        } catch (Exception e) {
            return error("Error uploading photo: " + e.getMessage());
        }
    }

    private static Date parse(String yyyyMMdd) throws Exception {
        return new SimpleDateFormat(DATE_FMT).parse(yyyyMMdd);
    }

    private static String fmt(Date d) {
        return d == null ? null : new SimpleDateFormat(DATE_FMT).format(d);
    }

    private static Map<String, Object> ok(String message) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("success", true);
        m.put("message", message);
        return m;
    }

    private static Map<String, Object> error(String message) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("success", false);
        m.put("message", message);
        return m;
    }
}