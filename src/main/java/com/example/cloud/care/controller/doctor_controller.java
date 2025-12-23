package com.example.cloud.care.controller;

import com.example.cloud.care.service.doctor_service;
import com.example.cloud.care.model.Doctor;
import com.example.cloud.care.service.DoctorUserDetails;
import com.example.cloud.care.service.EmailServiceD;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.example.cloud.care.dao.doctor_dao;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.cloud.care.config.CloudinaryConfig;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.io.IOException;
import java.sql.Date;
import java.util.Calendar;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/doctor")
public class doctor_controller {

    private final doctor_service doctorService;
    private final doctor_dao doctorRepository;
    private final com.cloudinary.Cloudinary cloudinary;
    @Autowired

    private  BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private EmailServiceD emailServiceD;
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
        return "doctor_dashboard"; // Thymeleaf template showing doctor info
    }
    


    // Edit doctor info form
    @GetMapping("/update")
public String editCurrentDoctor(Model model) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    DoctorUserDetails userDetails = (DoctorUserDetails) auth.getPrincipal();
    Doctor doctor = userDetails.getDoctor();
    if (doctor == null) return "redirect:/login";
    model.addAttribute("doctor", doctor);
    return "doctor_data_entry"; // Thymeleaf template
}

    


    @PostMapping("/update")
public String updateDoctorInfo(
        @ModelAttribute Doctor updatedDoctor,
        BindingResult bindingResult,
        @RequestParam(value = "profileImageFile", required = false) MultipartFile profileImageFile,
        @RequestParam(value = "certificateFile", required = false) MultipartFile certificateFile
) throws IOException {

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    DoctorUserDetails userDetails = (DoctorUserDetails) auth.getPrincipal();
    Doctor doctor = userDetails.getDoctor();

    if (doctor == null) return "redirect:/doctor/login";

    if (bindingResult.hasErrors()) {
        System.out.println("Binding errors: " + bindingResult.getAllErrors());
        return "doctor_data_entry";
    }

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

    // ===== HANDLE FILES SEPARATELY =====
    if (profileImageFile != null && !profileImageFile.isEmpty()) {
        Map uploadResult = cloudinary.uploader().upload(
                profileImageFile.getBytes(),
                ObjectUtils.asMap("folder", "doctor_profiles")
        );
        // Only set the URL string in your entity
        doctor.setProfileImage((String) uploadResult.get("secure_url") + "?v=" + System.currentTimeMillis());
    }

    if (certificateFile != null && !certificateFile.isEmpty()) {
        Map certUpload = cloudinary.uploader().upload(
                certificateFile.getBytes(),
                ObjectUtils.asMap("folder", "doctor_certificates", "resource_type", "raw")
        );
        doctor.setCertifications((String) certUpload.get("secure_url") + "?v=" + System.currentTimeMillis());
    }

    // ===== SAVE =====
    doctorRepository.save(doctor);

    // 🔥 REFRESH THE SESSION
    refreshDoctorSession(doctor);

    return "redirect:/doctor/dashboard?updated=true";
}


    // ==========================
    // 🔄 SESSION REFRESH METHOD
    // ==========================
    private void refreshDoctorSession(Doctor updatedDoctor) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    DoctorUserDetails newDetails = new DoctorUserDetails(updatedDoctor);

    UsernamePasswordAuthenticationToken newAuth =
            new UsernamePasswordAuthenticationToken(
                    newDetails,
                    auth.getCredentials(),
                    newDetails.getAuthorities()
            );

    newAuth.setDetails(auth.getDetails());
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
@GetMapping("/forgot-password")
public String showForgotPasswordPage() {
    return "doctor_forgot_password"; // Thymeleaf template with email input
}

@PostMapping("/forgot-password")
public String handleForgotPassword(@RequestParam String email, Model model) {
    Doctor doctor = doctorService.findByEmail(email);
    if (doctor == null) {
        model.addAttribute("error", "Email not registered");
        return "doctor_forgot_password";
    }

    // Generate reset token
    String token = UUID.randomUUID().toString();
    doctor.setResetToken(token);

    // Set token expiry (1 hour)
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.HOUR, 1);
    doctor.setResetTokenExpiry(cal.getTime());

    doctorRepository.save(doctor);

    // Send reset email
    String resetLink = "http://localhost:8080/doctor/reset-password?token=" + token;
    String subject = "CloudCare Password Reset";
    String body = """
            Hello Dr. %s,

            Click the link below to reset your CloudCare password. This link will expire in 1 hour:

            %s

            If you didn't request this, please ignore this email.
            """.formatted(doctor.getName(), resetLink);

    emailServiceD.sendEmail(doctor.getEmail(), subject, body);

    model.addAttribute("message", "Password reset link sent to your email.");
return "doctor_forgot_password";
}

@GetMapping("/reset-password")
public String showResetPasswordPage(@RequestParam String token, Model model) {
    Doctor doctor = doctorService.findByResetToken(token);
   if (doctor == null || doctor.getResetTokenExpiry().before(new java.util.Date())) { 
       model.addAttribute("error", "invalid or expired password reset link");
    return "doctor_forgot_password"; // show error message
    }
    model.addAttribute("token", token);
    return "doctor_reset_password"; // template with new password input
}

@PostMapping("/reset-password")
public String handleResetPassword(
        @RequestParam String token,
        @RequestParam String newPassword,
        Model model
) {
    Doctor doctor = doctorService.findByResetToken(token);
   if (doctor == null || doctor.getResetTokenExpiry().before(new java.util.Date())) { 
        model.addAttribute("error", "Invalid or expired token.");
        return "doctor_reset_password";
    }

    // Update password
    doctor.setPassword(passwordEncoder.encode(newPassword));

    // Clear token
    doctor.setResetToken(null);
    doctor.setResetTokenExpiry(null);

    doctorRepository.save(doctor);

    model.addAttribute("message", "Password updated successfully.");
    return "redirect:/doctor/login?resetSuccess=true";
}
}
    