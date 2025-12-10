package com.example.cloud.care.repository;

import com.example.cloud.care.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {

    List<Appointment> findByDoctorId(long doctorId);
    List<Appointment> findByPatientId(int patientId);
    List<Appointment> findByDoctorIdAndAppointmentDate(long doctorId, java.util.Date date);
}