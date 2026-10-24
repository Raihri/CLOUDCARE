package com.example.cloud.care.controller.patient;


import com.example.cloud.care.model.Doctor;
import com.example.cloud.care.model.Donor;
import com.example.cloud.care.model.Patient;
import com.example.cloud.care.repository.PatientRepository;
import com.example.cloud.care.service.UserService;
import com.example.cloud.care.service.DoctorService;
import com.example.cloud.care.service.CurrentPatientService;
import com.example.cloud.care.service.DonorService;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/patient")
public class PatientDashboardController {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private CurrentPatientService logger;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private DonorService donorService;

    @Autowired
    private com.example.cloud.care.service.RequestService requestService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Patient patient = logger.currentPatient();
        model.addAttribute("patient", patient);
        return "patient_dashboard";
    }

    @GetMapping("/doctor_list")
    public String docList(Model model) {
        Patient patient = logger.currentPatient();
        model.addAttribute("doctors", doctorService.getDoctors());
        model.addAttribute("patient", patient);
        return "doctor_list";
    }

    @GetMapping("/doctor/{id}")
    public String getDoctorById(@PathVariable("id") long id, Model model) {
        Doctor doc = doctorService.getDoctorByID(id);
        if (doc == null) {
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
        Patient patient = logger.currentPatient();
        model.addAttribute("patient", patient);
        return "donor_dashboard";
    }

    @GetMapping("/donor-form")
    public String donorForm(Model model, @RequestParam(value = "id", required = false) Long id) {
        Patient patient = logger.currentPatient();
        model.addAttribute("patient", patient);

        if (id != null) {
            Donor donor = donorService.getDonor(id);
            if (donor != null) model.addAttribute("donor", donor);
        }

        return "donor_form";
    }

    @GetMapping("/request-form")
    public String requestForm(Model model, @RequestParam(value = "id", required = false) Long id) {
        Patient patient = logger.currentPatient();
        model.addAttribute("patient", patient);

        if (id != null) {
            com.example.cloud.care.model.Request req = requestService.getRequest(id);
            if (req != null) model.addAttribute("request", req);
        }

        return "request_form";
    }

    @GetMapping("/donor-response")
    public String donorResponse(Model model) {
        Patient patient = logger.currentPatient();
        model.addAttribute("patient", patient);
        return "donor_response_list";
    }
}