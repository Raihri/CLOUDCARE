package com.example.cloud.care.service;

import com.example.cloud.care.model.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class PhotoUploadService {

    @Autowired
    private StorageService storageService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private CurrentPatientService logger;

    @Async
    public void uploadAndSave(Long patientId, byte[] imageBytes) {
        try {
            String url = storageService.upload(imageBytes, "image/jpeg", "patients");

            Patient p = logger.currentPatient();
            if (p != null) {
                p.getUser().setPhotoUrl(url);
                patientService.save(p);
            }

            System.out.println("Async upload completed!");

        } catch (Exception e) {
            System.out.println("Async upload failed: " + e.getMessage());
        }
    }
}
