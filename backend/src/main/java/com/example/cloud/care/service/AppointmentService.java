package com.example.cloud.care.service;

import com.example.cloud.care.model.*;
import com.example.cloud.care.repository.*;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;
import java.util.UUID;

import com.example.cloud.care.repository.DoctorRepository;
import java.util.Date;
import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepo;
    private final DoctorAvailabilityRepository availabilityRepo;
    private final DoctorRepository doctorRepo;

    public AppointmentService(AppointmentRepository appointmentRepo,
                              DoctorAvailabilityRepository availabilityRepo,
                              DoctorRepository doctorRepo) {
        this.appointmentRepo = appointmentRepo;
        this.availabilityRepo = availabilityRepo;
        this.doctorRepo = doctorRepo;
    }

    public List<DoctorAvailability> getDoctorAvailability(long doctorId) {
        Doctor doctor = doctorRepo.findById(doctorId).orElseThrow();
        return availabilityRepo.findByDoctor(doctor);
    }

    @Transactional
    public Appointment bookAppointment(Patient patient, long doctorId, long slotId) {
        Doctor doctor = doctorRepo.findById(doctorId).orElseThrow();
        DoctorAvailability slot = availabilityRepo.findById(slotId).orElseThrow();

        // Refuse a slot that is already taken. Without this check two patients
        // can book the same slot and the doctor's calendar double-books.
        if (slot.isBooked()) {
            throw new IllegalStateException("That time slot has already been booked.");
        }

        // The slot belongs to a different doctor, so booking it would file the
        // appointment against the wrong calendar.
        if (slot.getDoctor() != null && slot.getDoctor().getId() != doctorId) {
            throw new IllegalArgumentException("That slot does not belong to this doctor.");
        }

        Appointment appointment = new Appointment();
        appointment.setDoctor(doctor);
        appointment.setPatient(patient);
        appointment.setAppointmentDate(slot.getDate());
        appointment.setTimeSlot(slot.getStartTime() + "-" + slot.getEndTime());
        appointment.setType(slot.getTelemedicineAvailable() ?
                Appointment.AppointmentType.TELEMEDICINE :
                Appointment.AppointmentType.PHYSICAL);
        appointment.setStatus(Appointment.Status.PENDING);

        // Mark the slot taken. This was missing entirely, so `booked` never
        // became true and every slot stayed bookable forever.
        slot.setBooked(true);
        availabilityRepo.save(slot);

        return appointmentRepo.save(appointment);
    }

    public List<Appointment> getPatientAppointments(long patientId) {
        return appointmentRepo.findByPatientId(patientId);
    }
    public Appointment getAppointmentById(int appointmentId) {
        return appointmentRepo.findById(appointmentId).orElse(null);
    }
    
    public void confirmAppointment(Appointment appointment) {
        appointment.setStatus(Appointment.Status.CONFIRMED);
        if (appointment.getType() == Appointment.AppointmentType.TELEMEDICINE) {
        // Generate a unique, hard-to-guess Jitsi room
        String roomName = "appt-" + appointment.getId() + "-" + UUID.randomUUID().toString().substring(0, 8);
        String jitsiLink = "https://meet.jit.si/" + roomName;
        appointment.setTelemedicineLink(jitsiLink);
    }

    appointmentRepo.save(appointment);
    }
    @Transactional
    public void cancelAppointment(Appointment appointment) {
        appointment.setStatus(Appointment.Status.CANCELLED);
        appointmentRepo.save(appointment);

        // Release the slot so it can be booked again. Without this a cancelled
        // appointment leaves its slot permanently unusable.
        DoctorAvailability slot = findSlotFor(appointment);
        if (slot != null) {
            slot.setBooked(false);
            availabilityRepo.save(slot);
        }
    }

    /**
     * Locate the availability row an appointment consumed. Appointments store a
     * rendered "HH:mm:ss-HH:mm:ss" string rather than the slot id, so the slot
     * is matched on doctor + date + time.
     */
    private DoctorAvailability findSlotFor(Appointment appointment) {
        Doctor doctor = appointment.getDoctor();
        if (doctor == null || appointment.getTimeSlot() == null) {
            return null;
        }
        String[] parts = appointment.getTimeSlot().split("-");
        if (parts.length != 2) {
            return null;
        }
        return availabilityRepo.findByDoctor(doctor).stream()
                .filter(s -> appointment.getAppointmentDate() != null
                        && s.getDate() != null
                        && appointment.getAppointmentDate().compareTo(s.getDate()) == 0)
                .filter(s -> parts[0].equals(s.getStartTime().toString())
                        && parts[1].equals(s.getEndTime().toString()))
                .findFirst()
                .orElse(null);
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