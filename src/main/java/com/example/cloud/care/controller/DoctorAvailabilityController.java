package com.example.cloud.care.controller;

import com.example.cloud.care.model.Doctor;
import com.example.cloud.care.model.DoctorAvailability;
import com.example.cloud.care.service.DoctorUserDetails;
import com.example.cloud.care.service.doctor_service;
import com.example.cloud.care.dao.doctor_dao;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;

@Controller
public class DoctorAvailabilityController {

    @Autowired
    private doctor_dao doctorRepository;
    @Autowired
    private doctor_service doctorService;

    // Show doctor availability page
    @GetMapping("/doctor/saveAvailability")
    @Transactional // Keep session open for lazy collections
    public String showAvailability(Model model,
                                   @RequestParam(required = false) String error,
                                   @RequestParam(required = false) String slotAdded) {

        Doctor doctor = getLoggedInDoctor();
        if (doctor == null) {
            return "redirect:/doctor/login";
        }

        // Force fetch availability to avoid LazyInitializationException
        doctor = doctorRepository.findByIdWithAvailability(doctor.getId());

        model.addAttribute("doctor", doctor);
        model.addAttribute("error", error);
        model.addAttribute("slotAdded", slotAdded);
        LocalDate today = LocalDate.now();
    model.addAttribute("today", today);
        return "doctor_slot";
    }

    @PostMapping("/doctor/addAvailabilitySlot")
public String addAvailabilitySlot(
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date date,
        @RequestParam String startTime,
        @RequestParam String endTime,
        @RequestParam(required = false) boolean telemedicine
) {

    Doctor doctor = getLoggedInDoctor();
    if (doctor == null) return "redirect:/doctor/login";

    LocalTime start = LocalTime.parse(startTime);
    LocalTime end = LocalTime.parse(endTime);

    if (!start.isBefore(end)) {
        return "redirect:/doctor/saveAvailability?error=invalidTime";
    }

    // Check overlaps
    for (DoctorAvailability existing : doctor.getAvailability()) {
        if (existing.getDate().equals(date) &&
            start.isBefore(existing.getEndTime()) &&
            end.isAfter(existing.getStartTime())) {
            return "redirect:/doctor/saveAvailability?error=overlap";
        }
    }

    // Create slot
    DoctorAvailability slot = new DoctorAvailability();
    slot.setDate(date);
    slot.setStartTime(start);
    slot.setEndTime(end);
    slot.setTelemedicineAvailable(telemedicine);
    slot.setDoctor(doctor);

    doctor.getAvailability().add(slot);
    doctorService.saveDoctor(doctor);

    return "redirect:/doctor/saveAvailability?slotAdded=true";
}

    // Helper to get doctor with availability via service
    private Doctor getLoggedInDoctor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof DoctorUserDetails)) {
            return null;
        }
        DoctorUserDetails userDetails = (DoctorUserDetails) auth.getPrincipal();
        Long doctorId = userDetails.getDoctor().getId();

        return doctorService.getDoctorWithAvailability(doctorId);
    }
    @PostMapping("/doctor/removeAvailabilitySlot")
    public String removeAvailabilitySlot(@RequestParam Long slotId) {
    Doctor doctor = getLoggedInDoctor();
    if (doctor == null) return "redirect:/doctor/login";

    // Remove the slot
    doctorService.removeAvailabilitySlot(slotId, doctor.getId());

    return "redirect:/doctor/saveAvailability";
}
}
