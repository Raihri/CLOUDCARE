package com.example.cloud.care.service;

import com.example.cloud.care.model.Patient;
import com.example.cloud.care.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class loggedInUserFind {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private patient_service patientService;

    public Patient logger()
    {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        // Fetch user using Optional
        Patient user = patientService.findByEmail(email)
                .orElse(null);
        return user;
    }
}
