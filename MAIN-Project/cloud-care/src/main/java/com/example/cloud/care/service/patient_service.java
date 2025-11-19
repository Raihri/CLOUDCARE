package com.example.cloud.care.service;
import com.example.cloud.care.dao.patient_dao;
import com.example.cloud.care.model.Patient;
import com.example.cloud.care.model.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
public class patient_service
{
    @Autowired
    patient_dao patient_dao;
    public List<Patient> getPatients() {
        try {
            return patient_dao.findAll();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of(); 
        }
    }

    public Patient savePatient(Patient p) {
        try {
            return patient_dao.save(p);
        } catch (Exception e) {
            e.printStackTrace();
            return null; 
        }
    }

    public void deletePatient(int id) {
        try {
            patient_dao.deleteById(id);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to delete patient with ID: " + id);
        }
    }

    
public Optional<Patient> findById(Long id) {
        return patient_dao.findById(id);
    }
//     public Patient getPatientData(int patientId) {
//         return patient_dao.findById(patientId).orElse(null);
// }


}
