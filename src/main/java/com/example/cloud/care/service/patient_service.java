package com.example.cloud.care.service;

import com.example.cloud.care.model.Patient;
import com.example.cloud.care.model.notification;
import com.example.cloud.care.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class patient_service {
    @Autowired
    PatientRepository patientRepository;

    public List<Patient> getPatients() {
        try {
            return patientRepository.findAll();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public Patient savePatient(Patient p) {
        try {
            return patientRepository.save(p);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void deletePatient(long id) {
        try {
            patientRepository.deleteById(id);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to delete patient with ID: " + id);
        }
    }

    public Optional<Patient> findById(Long patientId) {
        return Optional.ofNullable(patientRepository.findById(patientId).orElse(null));
    }

    public Optional<Patient> findByEmail(String email) {
        return patientRepository.findByUserEmail(email);
    }

    // FIX THIS METHOD - Remove the Optional parameter
    public Patient save(Patient patient) {
        try {
            return patientRepository.save(patient);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // OR if you want to keep Optional support, add this method:
    public Patient saveOptional(Optional<Patient> patientOptional) {
        if (patientOptional.isPresent()) {
            return patientRepository.save(patientOptional.get());
        }
        return null;
    }

    public void addNotification(Patient patient, notification notification)
    {
        patient.addNotification(notification);
        patientRepository.save(patient);
    }
}