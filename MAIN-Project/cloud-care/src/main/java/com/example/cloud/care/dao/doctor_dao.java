package com.example.cloud.care.dao;

import com.example.cloud.care.model.Doctor;
import com.example.cloud.care.model.Doctor.Status;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface doctor_dao extends JpaRepository<Doctor, Long> {
    Optional<Doctor> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Doctor> findByStatus(Status status);
}
