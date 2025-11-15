package com.example.cloud.care.controller;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.cloud.care.model.Donor;
import com.example.cloud.care.model.Request;
import com.example.cloud.care.service.DonorService;
import com.example.cloud.care.service.RequestService;

import java.util.List;

// @Controller
// public class DashboardController {

//     private final DonorService donorService;
//     private final RequestService requestService;

//     public DashboardController(DonorService donorService, RequestService requestService) {
//         this.donorService = donorService;
//         this.requestService = requestService;
//     }

//     @GetMapping("/")
//     public String dashboard(Model model) {
//         // Fetch all donors and requests
//         List<Donor> donors = donorService.filterDonors(null, null, null);  // Using filter method
//         List<Request> requests = requestService.filterRequests(null, null, null);

//         // Add attributes for Thymeleaf
//         model.addAttribute("donors", donors);
//         model.addAttribute("requests", requests);
//         model.addAttribute("totalDonors", donors.size());
//         model.addAttribute("totalRequests", requests.size());

//         return "dashboard"; // Renders dashboard.html
//     }
// }

@Controller
public class DashboardController {

    private final DonorService donorService;
    private final RequestService requestService;

    public DashboardController(DonorService donorService, RequestService requestService) {
        this.donorService = donorService;
        this.requestService = requestService;
    }

    @GetMapping("/donor")
    public String dashboard(Model model) {

        // Fetch all donors & all requests directly
        List<Donor> donors = donorService.getAllDonors();
        List<Request> requests = requestService.getAllRequests();

        // Add to Thymeleaf
        model.addAttribute("donors", donors);
        model.addAttribute("requests", requests);
        model.addAttribute("totalDonors", donors.size());
        model.addAttribute("totalRequests", requests.size());

        return "donor_dashboard";
    }
}
