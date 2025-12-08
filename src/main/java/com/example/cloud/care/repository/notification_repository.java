package com.example.cloud.care.repository;

import com.example.cloud.care.model.notification;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface notification_repository extends JpaRepository<notification, Long> {
    List<notification> findByPatientId(Long patientId);

    List<notification> findByPatientIdAndIsReadFalse(Long patientId);
}