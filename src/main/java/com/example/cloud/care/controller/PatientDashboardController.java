package com.example.cloud.care.controller;
import com.example.cloud.care.model.Doctor;
import com.example.cloud.care.model.Donor;
import com.example.cloud.care.model.Patient;
import com.example.cloud.care.model.User;
import com.example.cloud.care.repository.PatientRepository;
import com.example.cloud.care.service.ChatService;
import com.example.cloud.care.service.UserService;
import com.example.cloud.care.service.doctor_service;
import com.example.cloud.care.service.loggedInUserFind;
import com.example.cloud.care.service.DonorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @Autowired
    private ChatService chatService;
     @Autowired
    private DonorService donorService;

    @Autowired
    private com.example.cloud.care.service.RequestService requestService;

    @GetMapping("/aichat")
    public String home() {
        return "aichat";
    }

    @PostMapping("/chat")
    public String chat(@RequestParam("message") String message, Model model) {
        String response = chatService.getChatResponse(message);
        model.addAttribute("userMessage", message);
        model.addAttribute("botResponse", response);
        return "aichat";
    }

    @GetMapping("/raima")
    public String raima()
    {
        return "raima";
    }


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
    public String getDoctorById(@PathVariable("id") long id, Model model) {
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
       @GetMapping("/donor")
    public String donorDashboard(Model model) {
        Patient patient = logger.logger();
        model.addAttribute("patient", patient);
        return "donor_dashboard";
    }

    @GetMapping("/donor-form")
    public String donorForm(Model model, @org.springframework.web.bind.annotation.RequestParam(value = "id", required = false) Long id) {
        Patient patient = logger.logger();
        model.addAttribute("patient", patient);

        if (id != null) {
            Donor donor = donorService.getDonor(id);
            if (donor != null) model.addAttribute("donor", donor);
        }

        return "donor_form";
    }

    @GetMapping("/request-form")
    public String requestForm(Model model, @org.springframework.web.bind.annotation.RequestParam(value = "id", required = false) Long id) {
        Patient patient = logger.logger();
        model.addAttribute("patient", patient);

        if (id != null) {
            // load request for editing and pass to template
            com.example.cloud.care.model.Request req = requestService.getRequest(id);
            if (req != null) model.addAttribute("request", req);
        }

        return "request_form";
    }

    @GetMapping("/donor-response")
    public String donorResponse(Model model) {
        Patient patient = logger.logger();
        model.addAttribute("patient", patient);
        return "donor_response_list";
    }
}


