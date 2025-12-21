package com.example.cloud.care.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.cloud.care.model.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PhotoUploadService {

    @Autowired
    private Cloudinary cloudinary;

    @Autowired
    private patient_service patientService;

    @Autowired
    private loggedInUserFind logger;

    @Async
    public void uploadAndSave(Long patientId, byte[] imageBytes) {
        try {
            Map upload = cloudinary.uploader().upload(
                    imageBytes,
                    ObjectUtils.asMap("folder", "patients")
            );

            Patient p = logger.logger();
            if (p != null) {
                p.getUser().setPhotoUrl(upload.get("secure_url").toString());
                patientService.save(p);
            }

            System.out.println("Async upload completed!");

        } catch (Exception e) {
            System.out.println("Async upload failed: " + e.getMessage());
        }
    }
}