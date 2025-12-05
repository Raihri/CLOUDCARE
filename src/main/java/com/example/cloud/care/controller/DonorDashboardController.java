package com.example.cloud.care.controller;

import com.example.cloud.care.model.Donor;
import com.example.cloud.care.model.Request;
import com.example.cloud.care.service.DonorService;
import com.example.cloud.care.service.RequestService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class DonorDashboardController {

    private final DonorService donorService;
    private final RequestService requestService;

    public DonorDashboardController(DonorService donorService, RequestService requestService) {
        this.donorService = donorService;
        this.requestService = requestService;
    }

    @GetMapping("/")
    public String dashboard(Model model) {

        // Fetch all donors & all requests directly
        List<Donor> donors = donorService.getAllDonors();
        List<Request> requests = requestService.getAllRequests();

        // Add to Thymeleaf
        model.addAttribute("donors", donors);
        model.addAttribute("requests", requests);
        model.addAttribute("totalDonors", donors.size());
        model.addAttribute("totalRequests", requests.size());

        return "dashboard";
    }
}
