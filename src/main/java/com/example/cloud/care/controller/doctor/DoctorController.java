package com.example.cloud.care.controller.doctor;

import com.example.cloud.care.service.DoctorService;
import com.example.cloud.care.service.PatientService;
import com.example.cloud.care.model.Doctor;
import com.example.cloud.care.service.DoctorUserDetails;
import com.example.cloud.care.service.EmailService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.example.cloud.care.repository.DoctorRepository;
import com.example.cloud.care.service.StorageService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.io.IOException;

@Controller
@RequestMapping("/doctor")
public class DoctorController {

    private final DoctorService doctorService;
    private final PatientService patientService;
    private final DoctorRepository doctorRepository;
    private final StorageService storageService;
    @Autowired

    private  BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private EmailService emailService;
 

    @Autowired
    public DoctorController(DoctorService doctorService, DoctorRepository doctorRepository, StorageService storageService,PatientService patientService) {
        this.doctorService = doctorService;
        this.doctorRepository = doctorRepository;
        this.storageService = storageService;
        this.patientService = patientService;
    }

    @GetMapping("/patient_id_fetch")
    public String id_fetch(Model model)
    {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    DoctorUserDetails userDetails = (DoctorUserDetails) auth.getPrincipal();
    Doctor doctor = userDetails.getDoctor();

    model.addAttribute("doctor", doctor); // 🔥 REQUIRED
        return "id_fetch";
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
        String profileUrl = storageService.upload(profileImageFile, "doctor_profiles");
        // Only set the URL string in your entity
        doctor.setProfileImage(profileUrl + "?v=" + System.currentTimeMillis());
    }

    if (certificateFile != null && !certificateFile.isEmpty()) {
        String certificateUrl = storageService.upload(certificateFile, "doctor_certificates");
        doctor.setCertifications(certificateUrl + "?v=" + System.currentTimeMillis());
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
}
    