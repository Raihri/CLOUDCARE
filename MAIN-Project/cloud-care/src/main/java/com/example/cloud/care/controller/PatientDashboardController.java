package com.example.cloud.care.controller;
import com.example.cloud.care.model.Doctor;
import com.example.cloud.care.model.Patient;
import com.example.cloud.care.model.User;
import com.example.cloud.care.repository.PatientRepository;
import com.example.cloud.care.service.UserService;
import com.example.cloud.care.service.doctor_service;
import com.example.cloud.care.service.loggedInUserFind;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
@Controller
@RequestMapping("/patient")
public class PatientDashboardController {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private UserService userService;  // <-- inject UserService

    @Autowired
    private loggedInUserFind logger;

    @Autowired
    private doctor_service doctorService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Get logged-in user's email
        Patient patient = logger.logger();

        model.addAttribute("patient", patient);


        System.out.println("---------------HELLO--------------------");

        System.out.println("Patient Data: ");
        System.out.println(patient.getAnxietyScore());
        System.out.println("Patient Name is:   --------------------");
        System.out.println(patient.getUser().getName());
        System.out.println("Photo URL ---------------");
        System.out.println(patient.getUser().getPhotoUrl());

        ;
        return "patient_dashboard";
    }

    @GetMapping("/doctor_list")
    public String docList(Model model)
    {
        Patient patient = logger.logger();
        model.addAttribute("doctors",doctorService.getDoctors());
        model.addAttribute("patient",patient);

        return "doctor_list";
    }

    @GetMapping("/doctor/{id}")
    public String getDoctorById(@PathVariable("id") int id, Model model) {
        Doctor doc = doctorService.getDoctorByID(id);
        if (doc == null) {
            // Handle case when doctor is not found
            return "redirect:/list";
        }
        System.out.println("Doctor found with ID: " + id);
        System.out.println("Doctor name: " + doc.getName());
        System.out.println("Doctor profile image: " + doc.getProfileImage());
        model.addAttribute("doctor", doc);
        return "doctor_profile_view";
    }
}
