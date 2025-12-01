package com.example.cloud.care.controller;

import com.example.cloud.care.service.doctor_service;
import com.example.cloud.care.model.Doctor;
import com.example.cloud.care.service.DoctorUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.example.cloud.care.dao.doctor_dao;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.cloud.care.config.CloudinaryConfig;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.io.IOException;
import java.util.Map;

@Controller
@RequestMapping("/doctor")
public class doctor_controller {

    private final doctor_service doctorService;
    private final doctor_dao doctorRepository;
    private final com.cloudinary.Cloudinary cloudinary;
    @Autowired
    public doctor_controller(doctor_service doctorService, doctor_dao doctorRepository, Cloudinary cloudinary) {
        this.doctorService = doctorService;
        this.doctorRepository = doctorRepository;
        this.cloudinary = cloudinary;
    }

    // Show signup form
    @GetMapping("/signup")
    public String showSignupForm(Model model) {
        model.addAttribute("doctor", new Doctor());
        return "doctor_signup"; // Thymeleaf template
    }

    // Handle signup request
    @PostMapping("/signup/request")
    public String handleSignupRequest(@ModelAttribute Doctor doctorRequest,
                                      @RequestParam("profileImageFile") MultipartFile profileImage,
                                      @RequestParam("certificateFile") MultipartFile certificateFile,
                                      Model model) {
        try {
            doctorService.saveSignupRequest(doctorRequest, profileImage, certificateFile);
            model.addAttribute("success", "Signup request submitted! Wait for approval.");
            return "redirect:/doctor/login?signupSuccess=true";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "doctor_signup";
        } catch (IOException e) {
            model.addAttribute("error", "File upload failed: " + e.getMessage());
            return "doctor_signup";
        }
    }

    // Show login page
    @GetMapping("/login")
    public String showLoginForm() {
        return "doctor_login"; // Thymeleaf template
    }

    // Dashboard page - only accessible for authenticated & approved doctors
    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return "redirect:/doctor/login";
        }

        DoctorUserDetails userDetails = (DoctorUserDetails) auth.getPrincipal();
        Doctor loggedInDoctor = userDetails.getDoctor();
        model.addAttribute("doctor", loggedInDoctor);
        return "shit2"; // Thymeleaf template showing doctor info
    }
    


    // Edit doctor info form
    @GetMapping("/update")
    public String editDoctorForm(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        DoctorUserDetails userDetails = (DoctorUserDetails) auth.getPrincipal();
        Doctor doctor = userDetails.getDoctor();
        if (doctor == null) return "redirect:/login";
        model.addAttribute("doctor", doctor);
        return "doctor-data-entry"; // Thymeleaf template for edit form
    }

    


    @PostMapping("/update")
    public String updateDoctorInfo(
            @ModelAttribute Doctor updatedDoctor,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
            @RequestParam(value = "certificateFile", required = false) MultipartFile certificateFile
    ) throws IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        DoctorUserDetails userDetails = (DoctorUserDetails) auth.getPrincipal();
        Doctor doctor = userDetails.getDoctor();

        if (doctor == null) return "redirect:/doctor/login";

        // ===== UPDATE FIELDS =====
        doctor.setGender(updatedDoctor.getGender());
        doctor.setDob(updatedDoctor.getDob());
        doctor.setBloodGroup(updatedDoctor.getBloodGroup());
        doctor.setPhoneNumber(updatedDoctor.getPhoneNumber());
        doctor.setAltPhone(updatedDoctor.getAltPhone());
        doctor.setAddress(updatedDoctor.getAddress());
        doctor.setDivision(updatedDoctor.getDivision());
        doctor.setZilla(updatedDoctor.getZilla());
        doctor.setStreet(updatedDoctor.getStreet());
        doctor.setPostalCode(updatedDoctor.getPostalCode());
        doctor.setEmergencyContact(updatedDoctor.getEmergencyContact());
        doctor.setDegrees(updatedDoctor.getDegrees());
        doctor.setSpecialization(updatedDoctor.getSpecialization());
        doctor.setSpecialities(updatedDoctor.getSpecialities());
        doctor.setEducation(updatedDoctor.getEducation());
        doctor.setExperienceYears(updatedDoctor.getExperienceYears());
        doctor.setLanguages(updatedDoctor.getLanguages());
        doctor.setHospitalName(updatedDoctor.getHospitalName());
        doctor.setHospitalAddress(updatedDoctor.getHospitalAddress());
        doctor.setConsultationFee(updatedDoctor.getConsultationFee());
        doctor.setDescription(updatedDoctor.getDescription());
        doctor.setMedicalCollege(updatedDoctor.getMedicalCollege());
        doctor.setWebsite(updatedDoctor.getWebsite());
        doctor.setLinkedin(updatedDoctor.getLinkedin());
        doctor.setFacebook(updatedDoctor.getFacebook());
        doctor.setInstagram(updatedDoctor.getInstagram());
        doctor.setTwitter(updatedDoctor.getTwitter());

        // ===== CLOUDINARY FILE UPLOADS =====
        if (profileImage != null && !profileImage.isEmpty()) {
            Map uploadResult = cloudinary.uploader().upload(
                    profileImage.getBytes(),
                    ObjectUtils.asMap("folder", "doctor_profiles"));
            doctor.setProfileImage((String) uploadResult.get("secure_url"));
        }

        if (certificateFile != null && !certificateFile.isEmpty()) {
            Map certUpload = cloudinary.uploader().upload(
                    certificateFile.getBytes(),
                    ObjectUtils.asMap("folder", "doctor_certificates", "resource_type", "raw"));
            doctor.setCertifications((String) certUpload.get("secure_url"));
        }

        doctorRepository.save(doctor);

        // 🔥 REFRESH THE SESSION
        refreshDoctorSession(doctor);

        return "redirect:/doctor/dashboard?updated=true";
    }


    // ==========================
    // 🔄 SESSION REFRESH METHOD
    // ==========================
    private void refreshDoctorSession(Doctor updatedDoctor) {
        DoctorUserDetails newDetails = new DoctorUserDetails(updatedDoctor);

        UsernamePasswordAuthenticationToken newAuth =
                new UsernamePasswordAuthenticationToken(
                        newDetails,
                        newDetails.getPassword(),
                        newDetails.getAuthorities()
                );

        SecurityContextHolder.getContext().setAuthentication(newAuth);
    }

    // AJAX: check if email exists
    @GetMapping("/check-email")
    @ResponseBody
    public boolean checkEmail(@RequestParam String email) {
        return doctorService.emailExists(email);
    }
    @GetMapping("/check-bmdc")
@ResponseBody
public boolean checkBmdc(@RequestParam String bmdc) {
    return doctorService.bmdcExists(bmdc);
}
}// Logout is handled by Spring Security automatically via /doctor/logout


    // AJAX: check if email exists
    