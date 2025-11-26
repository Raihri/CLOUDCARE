package com.example.cloud.care.dao;

import com.example.cloud.care.var.doctor;
import com.example.cloud.care.var.doctor.Status;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface doctor_dao extends JpaRepository<doctor, Long> {
    Optional<doctor> findByEmail(String email);

    boolean existsByEmail(String email);
    
    boolean existsByBmdcRegNo(String bmdcRegNo);


    List<doctor> findByStatus(Status status);
}
