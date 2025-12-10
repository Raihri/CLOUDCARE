package com.example.cloud.care.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class PatientController2 {

    // Returns logged-in patient info
    @GetMapping("/patient/current")
    public Map<String, Object> getCurrentPatient(@AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();
        response.put("username", userDetails.getUsername());
        response.put("email", userDetails.getUsername()); // if email is used as username
        return response;
    }
}