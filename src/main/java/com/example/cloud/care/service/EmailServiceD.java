package com.example.cloud.care.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.example.cloud.care.model.Appointment;
import com.example.cloud.care.model.Doctor;

import org.springframework.scheduling.annotation.Async;;


@Service
public class EmailServiceD {

    @Autowired
    private JavaMailSender mailSender;
    @Async
    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch(Exception e){
            e.printStackTrace(); // log error
        }
    }
    public void sendAppointmentConfirmation(Appointment appt) {
        Doctor doctor = appt.getDoctor();
        String patientEmail = appt.getPatient().getEmail();
        String patientName = appt.getPatient().getName();

        String subject = "Appointment Confirmed - CloudCare";
        String body;

        if (appt.getType() == Appointment.AppointmentType.TELEMEDICINE) {
            body = String.format(
                    "Hello %s,\n\nYour telemedicine appointment with Dr. %s has been confirmed.\n" +
                    "Date: %s\nTime: %s\n\nJoin via this link: %s\n\nRegards,\nCloudCare Team",
                    patientName,
                    doctor.getName(),
                    appt.getAppointmentDate(),
                    appt.getTimeSlot(),
                    appt.getTelemedicineLink()
            );
        } else { // Physical appointment
            body = String.format(
                    "Hello %s,\n\nYour appointment with Dr. %s has been confirmed.\n" +
                    "Date: %s\nTime: %s\nHospital: %s\n\nRegards,\nCloudCare Team",
                    patientName,
                    doctor.getName(),
                    appt.getAppointmentDate(),
                    appt.getTimeSlot(),
                    doctor.getHospitalName()
            );
        }

        sendEmail(patientEmail, subject, body);
    }

    // Send cancellation email
    public void sendAppointmentCancellation(Appointment appt) {
        Doctor doctor = appt.getDoctor();
        String patientEmail = appt.getPatient().getEmail();
        String patientName = appt.getPatient().getName();

        String subject = "Appointment Cancelled - CloudCare";
        String body = String.format(
                "Hello %s,\n\nYour appointment with Dr. %s on %s at %s has been cancelled.\n\nRegards,\nCloudCare Team",
                patientName,
                doctor.getName(),
                appt.getAppointmentDate(),
                appt.getTimeSlot()
        );

        sendEmail(patientEmail, subject, body);
    }

}

    

