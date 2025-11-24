package com.example.cloud.care.repository;

import com.example.cloud.care.model.Patient;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PatientRepository extends JpaRepository<Patient, Long> {
   @Query("SELECT p FROM Patient p WHERE p.user.email = :email")
   Optional<Patient> findByUserEmail(@Param("email") String email);
}