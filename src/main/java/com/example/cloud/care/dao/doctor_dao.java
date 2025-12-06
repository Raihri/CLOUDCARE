package com.example.cloud.care.dao;

import com.example.cloud.care.model.Doctor;
import com.example.cloud.care.model.Doctor.Status;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface doctor_dao extends JpaRepository<Doctor, Long> {
    Optional<Doctor> findByEmail(String email);
    
    Optional<Doctor> findByResetToken(String token);

    boolean existsByEmail(String email);
    
    boolean existsByBmdcRegNo(String bmdcRegNo);


    List<Doctor> findByStatus(Status status);
    @Query("SELECT d FROM Doctor d LEFT JOIN FETCH d.availability WHERE d.id = :id")
    Doctor findByIdWithAvailability(@Param("id") Long id);
}