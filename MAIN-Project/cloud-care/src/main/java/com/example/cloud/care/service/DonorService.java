package com.example.cloud.care.service;

import com.example.cloud.care.model.Donor;
import com.example.cloud.care.repository.DonorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DonorService {

    @Autowired
    private DonorEmailService emailService; // Async email service

    private final DonorRepository donorRepository;

    public DonorService(DonorRepository donorRepository) {
        this.donorRepository = donorRepository;
    }

    public List<Donor> getAllDonors() {
        return donorRepository.findAll();
    }

    public Donor saveDonor(Donor donor) {

        // Save donor to DB normally
        Donor saved = donorRepository.save(donor);

        // Send simple thank-you email asynchronously
        if (saved.getEmail() != null && !saved.getEmail().isBlank()) {

            String html =
                    "<html><body>" +
                    "<h2>Thank You for Registering as a Donor!</h2>" +
                    "<p>Hello " + saved.getName() + ",</p>" +
                    "<p>Your donor registration has been successfully saved.</p>" +
                    "<p>We appreciate your willingness to save lives ❤️</p>" +
                    "</body></html>";

            // Send email asynchronously (method must be @Async)
            emailService.sendHtmlEmailAsync(
                    saved.getEmail(),
                    "Blood Donor Registration Successful",
                    html
            );
        }

        return saved;
    }

    public void deleteDonor(Long id) {
        donorRepository.deleteById(id);
    }

    public Donor getDonor(Long id) {
        return donorRepository.findById(id).orElse(null);
    }

    
   
}
