package com.example.cloud.care.service;

import com.example.cloud.care.dao.doctor_dao;
import com.example.cloud.care.var.doctor;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.example.cloud.care.service.EmailServiceD;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class doctor_service {

    private final doctor_dao doctor_dao;
    private final BCryptPasswordEncoder passwordEncoder;
    private final Cloudinary cloudinary;
    private final EmailServiceD emailServiceD;

    public doctor_service(doctor_dao doctor_dao, BCryptPasswordEncoder passwordEncoder, Cloudinary cloudinary, EmailServiceD emailServiceD) {
        this.doctor_dao = doctor_dao;
        this.passwordEncoder = passwordEncoder;
        this.cloudinary = cloudinary;
        this.emailServiceD = emailServiceD;
    }

    public List<doctor> getDoctors() {
        return doctor_dao.findAll();
    }

    public doctor getDoctorByID(long id) {
        return doctor_dao.findById(id).orElse(null);
    }

    public doctor saveDoctor(doctor doc) {
        return doctor_dao.save(doc);
    }

    public void deleteDoctor(long id) {
        doctor_dao.deleteById(id);
    }

    // Doctor signup request
    public doctor saveSignupRequest(doctor doctorRequest, MultipartFile profileImage, MultipartFile certificateFile) throws IOException {
        // Validate mandatory fields
        if (doctorRequest.getPassword() == null || doctorRequest.getConfirmPassword() == null
                || !doctorRequest.getPassword().equals(doctorRequest.getConfirmPassword())) {
            throw new IllegalArgumentException("Password and Confirm Password must match and not be empty");
        }

        if (profileImage == null || profileImage.isEmpty()) {
            throw new IllegalArgumentException("Profile image is required");
        }

        if (certificateFile == null || certificateFile.isEmpty()) {
            throw new IllegalArgumentException("BMDC Registration / Certificate is required");
        }

        // Upload profile image
        Map profileUploadResult = cloudinary.uploader().upload(profileImage.getBytes(),
                ObjectUtils.asMap("folder", "doctor_profiles"));
        doctorRequest.setProfileImage((String) profileUploadResult.get("secure_url"));

        // Upload certificate file
        Map certUploadResult = cloudinary.uploader().upload(certificateFile.getBytes(),
                ObjectUtils.asMap("folder", "doctor_certificates", "resource_type", "auto"));
        doctorRequest.setCertifications((String) certUploadResult.get("secure_url"));

        // Hash password
        doctorRequest.setPassword(passwordEncoder.encode(doctorRequest.getPassword()));
        doctorRequest.setConfirmPassword(null); // remove confirmPassword

        // Set status pending
        doctorRequest.setStatus(doctor.Status.PENDING);

        return doctor_dao.save(doctorRequest);
    }

    // Doctor login
    public Optional<doctor> login(String email, String rawPassword) {
        Optional<doctor> doctorOpt = doctor_dao.findByEmail(email);
        if (doctorOpt.isPresent()) {
            doctor doc = doctorOpt.get();
            if (doc.getStatus() != doctor.Status.APPROVED) return Optional.empty();
            if (passwordEncoder.matches(rawPassword, doc.getPassword())) return Optional.of(doc);
        }
        return Optional.empty();
    }

    // Check if email already exists
    public boolean emailExists(String email) {
        return doctor_dao.findByEmail(email).isPresent();
    }
    // Get all pending doctors
public List<doctor> getPendingDoctors() {
    return doctor_dao.findByStatus(doctor.Status.PENDING);
}

// Approve a doctor and send email
public void approveDoctor(Long doctorId) {
    Optional<doctor> optionalDoctor = doctor_dao.findById(doctorId);
    if (optionalDoctor.isPresent()) {
        doctor doc = optionalDoctor.get();
        doc.setStatus(doctor.Status.APPROVED);
        doctor_dao.save(doc);

        // Send email notification
        String subject = "Your Doctor Account Has Been Approved!";
        String body = "Hello " + doc.getName() + ",\n\nYour account has been approved. You can now log in.\n\nThanks!";
        emailServiceD.sendEmail(doc.getEmail(), subject, body);
    }
}
     public void rejectDoctor(Long doctorId, String reason) {
    Optional<doctor> optionalDoctor = doctor_dao.findById(doctorId);
    if (optionalDoctor.isPresent()) {
        doctor doc = optionalDoctor.get();
        doc.setStatus(doctor.Status.REJECTED);
        doctor_dao.save(doc);

        // Send rejection email
        String subject = "Your Doctor Account Has Been Rejected";
        String body = "Hello " + doc.getName() + ",\n\nYour signup request was rejected.\nReason: " + reason;
        emailServiceD.sendEmail(doc.getEmail(), subject, body);
    }
}
}