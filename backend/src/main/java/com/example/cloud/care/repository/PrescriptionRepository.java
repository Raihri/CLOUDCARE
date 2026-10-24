package com.example.cloud.care.repository;

import com.example.cloud.care.model.Prescription;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    @EntityGraph(attributePaths = {"doctor", "patient", "patient.user"})
    List<Prescription> findByPatientIdOrderByCreatedAtDesc(Long patientId);

    @EntityGraph(attributePaths = {"doctor", "patient", "patient.user"})
    List<Prescription> findByDoctorIdOrderByCreatedAtDesc(Long doctorId);

    @EntityGraph(attributePaths = {"doctor", "patient", "patient.user"})
    List<Prescription> findByDoctorIdAndPatientIdOrderByCreatedAtDesc(Long doctorId, Long patientId);

    @EntityGraph(attributePaths = {"doctor", "patient", "patient.user"})
    List<Prescription> findByReminderSentFalseAndFollowUpDateBetween(
            LocalDate from, LocalDate to);

    @EntityGraph(attributePaths = {"doctor", "patient", "patient.user"})
    List<Prescription> findByDueReminderSentFalseAndFollowUpDateLessThanEqual(LocalDate date);

    long countByPatientId(Long patientId);
}
