package com.example.cloud.care.controller.patient;
import com.example.cloud.care.model.Appointment;
import com.example.cloud.care.model.Doctor;
import com.example.cloud.care.service.AppointmentService;
import com.example.cloud.care.service.DoctorService;
import com.example.cloud.care.service.PatientService;



import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.cloud.care.model.DoctorAvailability;
import com.example.cloud.care.model.Patient;
import com.example.cloud.care.service.DoctorAvailabilityService;

import java.security.Principal;
@Controller
@RequestMapping("/patient")
public class PatientBookingController {
    private final DoctorAvailabilityService availabilityService ;
    private final DoctorService doctorService;
    private final AppointmentService appointmentService;
    private final PatientService patientService;

    public PatientBookingController(DoctorAvailabilityService availabilityService, DoctorService doctorService, AppointmentService appointmentService, PatientService patientService) {
        this.availabilityService = availabilityService;
        this.doctorService = doctorService;
        this.appointmentService = appointmentService;
        this.patientService = patientService;
    }

    @GetMapping("/{doctorId}/availability")
    public String viewAvailability(@PathVariable long doctorId, Model model) {
        Doctor doctor = doctorService.getDoctorByID(doctorId);
        model.addAttribute("doctor", doctor);

        model.addAttribute("availabilityList",
                availabilityService.getAvailabilityForDoctor(doctor));

        return "doctor_availability"; // availability view template
    }

   @PostMapping("/{doctorId}/book/{slotId}")
public String bookSlot(@PathVariable long doctorId,
                       @PathVariable long slotId,
                       Principal principal,
                       RedirectAttributes redirectAttributes) {

    if (principal == null) {
        return "redirect:/patient/";
    }

    // Get logged-in patient
    String email = principal.getName();
    Patient patient = patientService.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Patient not found"));

    // Fetch doctor and slot
    Doctor doctor = doctorService.getDoctorByID(doctorId);
    DoctorAvailability slot = availabilityService.getSlotById(slotId);

    if (slot == null) {
        redirectAttributes.addFlashAttribute("errorMessage", "❌ Slot not found!");
        return "redirect:/patient/" + doctorId + "/availability";
    }

    if (slot.isBooked()) {
        redirectAttributes.addFlashAttribute("errorMessage", "❌ This slot is already booked!");
        return "redirect:/patient/" + doctorId + "/availability";
    }

    // Mark slot as booked and save appointment in a single transaction
    try {
        // Use your service method that handles saving
        Appointment appt = appointmentService.bookAppointment(patient, doctorId, slotId);

        // Mark slot as booked
        slot.setBooked(true);
        availabilityService.save(slot);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "✅ Your booking request has been saved successfully!"
        );
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute(
                "errorMessage",
                "❌ Something went wrong while booking: " + e.getMessage()
        );
    }

    return "redirect:/patient/" + doctorId + "/availability";
}

}