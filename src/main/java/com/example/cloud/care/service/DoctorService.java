package com.example.cloud.care.service;

import com.example.cloud.care.repository.DoctorRepository;
import com.example.cloud.care.model.Doctor;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import com.example.cloud.care.repository.DoctorAvailabilityRepository;


@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final StorageService storageService;
    private final EmailService emailService;
    private final DoctorAvailabilityRepository doctorAvailabilityRepository;
    

    public DoctorService(DoctorRepository doctorRepository, BCryptPasswordEncoder passwordEncoder, StorageService storageService, EmailService emailService, DoctorAvailabilityRepository doctorAvailabilityRepository) {
        this.doctorRepository = doctorRepository;
        this.passwordEncoder = passwordEncoder;
        this.storageService = storageService;
        this.emailService = emailService;
        this.doctorAvailabilityRepository = doctorAvailabilityRepository;
    }

    public List<Doctor> getDoctors() {
        // System.out.println("Doctor List----------------------");

        List<Doctor> list = doctorRepository.findAll(); // only 1 DB call

        // System.out.println(list);
        // System.out.println("Doctor End----------------------");

        return list;
    }

    public List<Doctor> getDoctorsRejected() {
        // System.out.println("Doctor List----------------------");

        List<Doctor> list = doctorRepository.findAll().stream()
                .filter(doc -> doc.getStatus() == Doctor.Status.REJECTED)
                .toList(); // only 1 DB call

        System.out.println(list);
        // System.out.println("Doctor End----------------------");

        return list;
    }

    public List<Doctor> getDoctorsPending() {
        // System.out.println("Doctor List----------------------");

        List<Doctor> list = doctorRepository.findAll().stream()
                .filter(doc -> doc.getStatus() == Doctor.Status.PENDING)
                .toList(); // only 1 DB call

        // System.out.println(list);
        // System.out.println("Doctor End----------------------");

        return list;
    }



    public Doctor getDoctorByID(long id) {
        return doctorRepository.findById(id).orElse(null);
    }

    public Doctor saveDoctor(Doctor doc) {
        return doctorRepository.save(doc);
    }

    public void deleteDoctor(long id) {
        doctorRepository.deleteById(id);
    }

    // Doctor signup request
    public Doctor saveSignupRequest(Doctor doctorRequest, MultipartFile profileImage, MultipartFile certificateFile) throws IOException {
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
        doctorRequest.setProfileImage(storageService.upload(profileImage, "doctor_profiles"));

        // Upload certificate file
        doctorRequest.setCertifications(storageService.upload(certificateFile, "doctor_certificates"));

        // Hash password
        doctorRequest.setPassword(passwordEncoder.encode(doctorRequest.getPassword()));
        doctorRequest.setConfirmPassword(null); // remove confirmPassword
       if (emailExists(doctorRequest.getEmail())) {
        throw new IllegalArgumentException("Email already in use");
    }

    // Check if BMDC number exists (if not null)
    if (doctorRequest.getBmdcRegNo() != null && bmdcExists(doctorRequest.getBmdcRegNo())) {
        throw new IllegalArgumentException("BMDC Registration No. already in use");
    }
        // Set status pending
        doctorRequest.setStatus(Doctor.Status.PENDING);

        return doctorRepository.save(doctorRequest);
    }

    // Doctor login
    public Optional<Doctor> login(String email, String rawPassword) {
        Optional<Doctor> doctorOpt = doctorRepository.findByEmail(email);
        if (doctorOpt.isPresent()) {
            Doctor doc = doctorOpt.get();
            if (doc.getStatus() != Doctor.Status.APPROVED) return Optional.empty();
            if (passwordEncoder.matches(rawPassword, doc.getPassword())) return Optional.of(doc);
        }
        return Optional.empty();
    }

    // Check if email already exists
    public boolean emailExists(String email) {
        return doctorRepository.findByEmail(email).isPresent();
    }
    // Get all pending doctors
public List<Doctor> getPendingDoctors() {
    return doctorRepository.findByStatus(Doctor.Status.PENDING);
}

// Approve a doctor and send email
public void approveDoctor(Long doctorId) {
    Optional<Doctor> optionalDoctor = doctorRepository.findById(doctorId);
    if (optionalDoctor.isPresent()) {
        Doctor doc = optionalDoctor.get();
        doc.setStatus(Doctor.Status.APPROVED);
        doctorRepository.save(doc);

        // Send email notification
        String subject = "Your Doctor Account Has Been Approved!";
        String body = "Hello " + doc.getName() + ",\n\nYour account has been approved. You can now log in.\n\nThanks!";
        emailService.sendEmail(doc.getEmail(), subject, body);
    }
}
     public void rejectDoctor(Long doctorId, String reason) {
    Optional<Doctor> optionalDoctor = doctorRepository.findById(doctorId);
    if (optionalDoctor.isPresent()) {
        Doctor doc = optionalDoctor.get();
        doc.setStatus(Doctor.Status.REJECTED);
        doctorRepository.save(doc);

        // Send rejection email
        String subject = "Your Doctor Account Has Been Rejected";
        String body = "Hello " + doc.getName() + ",\n\nYour signup request was rejected.\nReason: " + reason;
        emailService.sendEmail(doc.getEmail(), subject, body);
    }
}
    public boolean bmdcExists(String bmdcRegNo) {
        return doctorRepository.existsByBmdcRegNo(bmdcRegNo);
    }

    public Doctor findByEmail(String email) {
        
       return doctorRepository.findByEmail(email).orElse(null);
    }
    @Transactional(readOnly = true)
    public Doctor getDoctorWithAvailability(Long doctorId) {
        return doctorRepository.findByIdWithAvailability(doctorId);
    }
    @Transactional
public void removeAvailabilitySlot(Long slotId, Long doctorId) {
    Doctor doctor = getDoctorWithAvailability(doctorId);
    if (doctor == null) return;

    doctor.getAvailability().removeIf(slot -> slot.getId().equals(slotId));
    // If using JPA, also remove from database
    doctorAvailabilityRepository.deleteById(slotId);

    // Save doctor (optional if cascading works)
    doctorRepository.save(doctor);
}
}