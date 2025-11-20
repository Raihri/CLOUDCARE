package com.example.cloud.care.dao;

import com.example.cloud.care.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface patient_dao extends JpaRepository<Patient, Long> {
    Optional<Patient> findById(Long id);

}
