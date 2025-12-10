package com.example.cloud.care.service;

import com.example.cloud.care.model.*;
import com.example.cloud.care.repository.*;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.example.cloud.care.dao.doctor_dao;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepo;
    private final DoctorAvailabilityRepository availabilityRepo;
    private final doctor_dao doctorRepo;

    public AppointmentService(AppointmentRepository appointmentRepo,
                              DoctorAvailabilityRepository availabilityRepo,
                              doctor_dao doctorRepo) {
        this.appointmentRepo = appointmentRepo;
        this.availabilityRepo = availabilityRepo;
        this.doctorRepo = doctorRepo;
    }

    public List<DoctorAvailability> getDoctorAvailability(long doctorId) {
        Doctor doctor = doctorRepo.findById(doctorId).orElseThrow();
        return availabilityRepo.findByDoctor(doctor);
    }

    public Appointment bookAppointment(Patient patient, long doctorId, long slotId) {
        Doctor doctor = doctorRepo.findById(doctorId).orElseThrow();
        DoctorAvailability slot = availabilityRepo.findById(slotId).orElseThrow();

        Appointment appointment = new Appointment();
        appointment.setDoctor(doctor);
        appointment.setPatient(patient);
        appointment.setAppointmentDate(slot.getDate());
        appointment.setTimeSlot(slot.getStartTime() + "-" + slot.getEndTime());
        appointment.setType(slot.getTelemedicineAvailable() ?
                Appointment.AppointmentType.TELEMEDICINE :
                Appointment.AppointmentType.PHYSICAL);
        appointment.setStatus(Appointment.Status.PENDING);

       

        return appointmentRepo.save(appointment);
    }

    public List<Appointment> getPatientAppointments(int patientId) {
        return appointmentRepo.findByPatientId(patientId);
    }
    public Appointment getAppointmentById(int appointmentId) {
        return appointmentRepo.findById(appointmentId).orElse(null);
    }
    
    public void confirmAppointment(Appointment appointment) {
        appointment.setStatus(Appointment.Status.CONFIRMED);
        appointmentRepo.save(appointment);
    }
    public void cancelAppointment(Appointment appointment) {
        appointment.setStatus(Appointment.Status.CANCELLED);
        appointmentRepo.save(appointment);
    }
    public List<Appointment> getAppointmentsByDoctor(long doctorId) {
        return appointmentRepo.findByDoctorId(doctorId);
    }
    public List<Appointment> getAppointmentsByDoctorAndDate(int doctorId, Date date) {
        return appointmentRepo.findByDoctorIdAndAppointmentDate(doctorId, date);
    }
    @Transactional
    public void save(Appointment appointment) {
        appointmentRepo.save(appointment);
    }
}