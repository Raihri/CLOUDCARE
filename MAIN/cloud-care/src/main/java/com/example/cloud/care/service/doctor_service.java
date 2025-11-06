package com.example.cloud.care.service;

import com.example.cloud.care.dao.doctor_dao;
import com.example.cloud.care.var.doctor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class doctor_service {
    @Autowired
    doctor_dao doctor_dao;

    public List<doctor> getDoctors() {
        try {
            return doctor_dao.findAll();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public doctor getDoctorByID(int id) {
        try {
            return doctor_dao.findById(id).orElse(null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public doctor saveDoctor(doctor p) {
        try {
            return doctor_dao.save(p);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void deleteDoctor(int id) {
        try {
            doctor_dao.deleteById(id);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to delete doctor with ID: " + id);
        }
    }
}
