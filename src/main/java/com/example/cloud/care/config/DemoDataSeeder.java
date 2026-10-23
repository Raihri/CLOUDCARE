package com.example.cloud.care.config;

import com.example.cloud.care.dao.doctor_dao;
import com.example.cloud.care.model.Appointment;
import com.example.cloud.care.model.Doctor;
import com.example.cloud.care.model.DoctorAvailability;
import com.example.cloud.care.model.Patient;
import com.example.cloud.care.model.Prescription;
import com.example.cloud.care.model.User;
import com.example.cloud.care.model.notification;
import com.example.cloud.care.repository.AppointmentRepository;
import com.example.cloud.care.repository.DoctorAvailabilityRepository;
import com.example.cloud.care.repository.PatientRepository;
import com.example.cloud.care.repository.PrescriptionRepository;
import com.example.cloud.care.repository.UserRepository;
import com.example.cloud.care.service.patient_service;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Seeds the patient and doctor accounts used by the one-click demo buttons in
 * the Next.js frontend, plus a small amount of appointment data so every screen
 * has something meaningful to show. The admin demo account is bootstrapped
 * separately by {@link AdminBootstrapConfig}.
 *
 * <p>The seeder is idempotent: it looks accounts up by email and only fills in
 * what is missing. Passwords are reset on each start so a recruiter can always
 * rely on the credentials shown on the sign-in page.
 */
@Configuration
public class DemoDataSeeder {

    public static final String DEMO_PATIENT_EMAIL = "demo.patient@cloudcare.test";
    public static final String DEMO_DOCTOR_EMAIL = "demo.doctor@cloudcare.test";
    public static final String DEMO_PASSWORD = "Demo@123";

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final doctor_dao doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorAvailabilityRepository availabilityRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final patient_service patientService;
    private final BCryptPasswordEncoder passwordEncoder;

    public DemoDataSeeder(UserRepository userRepository,
                          PatientRepository patientRepository,
                          doctor_dao doctorRepository,
                          AppointmentRepository appointmentRepository,
                          DoctorAvailabilityRepository availabilityRepository,
                          PrescriptionRepository prescriptionRepository,
                          patient_service patientService,
                          BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.availabilityRepository = availabilityRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.patientService = patientService;
        this.passwordEncoder = passwordEncoder;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seed() {
        Patient patient = ensureDemoPatient();
        Doctor doctor = ensureDemoDoctor();
        ensureDemoAppointments(patient, doctor);
        ensureDemoPrescription(patient, doctor);
        System.out.println("✅ Demo data ready — patient: " + DEMO_PATIENT_EMAIL
                + ", doctor: " + DEMO_DOCTOR_EMAIL + ", password: " + DEMO_PASSWORD);
    }

    private Patient ensureDemoPatient() {
        User user = userRepository.findByEmail(DEMO_PATIENT_EMAIL).orElseGet(() -> {
            User created = new User();
            created.setName("Ayesha Siddiqua");
            created.setEmail(DEMO_PATIENT_EMAIL);
            created.setPassword(passwordEncoder.encode(DEMO_PASSWORD));
            created.setEnabled(true);
            return userRepository.save(created);
        });

        // Demo credentials must always work, even if someone edited the account
        // while trying the app.
        if (!passwordEncoder.matches(DEMO_PASSWORD, user.getPassword())) {
            user.setPassword(passwordEncoder.encode(DEMO_PASSWORD));
        }
        user.setEnabled(true);
        user.setVerificationCode(null);
        final User demoUser = userRepository.save(user);

        Patient patient = patientRepository.findById(demoUser.getId()).orElseGet(() -> {
            Patient created = new Patient(demoUser);
            created.setBloodGroup("O+");
            created.setAge(29);
            created.setGender('F');
            created.setDivision("Dhaka");
            created.setZilla("Dhaka");
            created.setAddress("House 12, Road 5, Dhanmondi, Dhaka");
            created.setWeight(58.0);
            created.setHeight(162.0);
            created.setNationalId("1996 1234 567890");
            created.setEmergencyContact("+880 1712 345678");
            created.setMaritalStatus("Single");
            created.setDateOfBirth(Date.valueOf(LocalDate.of(1997, 4, 12)));
            created.setBloodPressure("120/80");
            created.setSystolicBP(120);
            created.setDiastolicBP(80);
            created.setHeartRate(76);
            created.setOxygenSaturation(98);
            created.setBodyTemperature(98.6);
            created.setHb(13.2);
            created.setRbc(4.8);
            created.setWbc(7200.0);
            created.setPlatelet(250000.0);
            created.setSugarLevel(5.4);
            created.setBloodSugarLevel(5.4);
            created.setCholesterolTotal(180.0);
            created.setCholesterolLDL(100.0);
            created.setCholesterolHDL(55.0);
            created.setTriglyceride(120.0);
            created.setCreatinine(0.9);
            created.setCreatinineLevel(0.9);
            created.setAlt(24.0);
            created.setAst(22.0);
            created.setBilirubin(0.8);
            created.setRhFactor("Positive");
            created.setBmi(22.1);
            created.setBmr(1450.0);
            created.setCholesterol(180.0);
            created.setBloodSugar(5.4);
            created.setSleepHours(7.0);
            created.setWaterIntake(2.5);
            created.setPhysicalActivity(4);
            created.setStressLevel(3);
            created.setAnxietyScore(2);
            created.setAllergies(new ArrayList<>(List.of("Penicillin")));
            created.setDiseases(new ArrayList<>(List.of("Hypothyroidism")));
            created.setMedications(new ArrayList<>(List.of("Levothyroxine 50mcg")));
            created.setSymptoms(new ArrayList<>(List.of("Fatigue")));
            created.setFamilyDiseaseHistory(new ArrayList<>(List.of("Diabetes (mother)")));
            created.setVaccinationHistory(new ArrayList<>(List.of("Tetanus booster (2024)")));
            created.setPastSurgeries(new ArrayList<>(List.of("Appendectomy (2018)")));
            return patientRepository.save(created);
        });

        // Make sure the profile is linked back to its login record.
        patient.setUser(demoUser);
        return patientRepository.save(patient);
    }

    private Doctor ensureDemoDoctor() {
        Doctor doctor = doctorRepository.findByEmail(DEMO_DOCTOR_EMAIL).orElseGet(() -> {
            Doctor created = new Doctor();
            created.setName("Dr. Nusrat Jahan");
            created.setEmail(DEMO_DOCTOR_EMAIL);
            created.setBmdcRegNo("DEMO-BMDC-2026");
            created.setGender("Female");
            created.setBloodGroup("B+");
            created.setPhoneNumber("+880 1812 345678");
            created.setAddress("Road 9, Banani, Dhaka");
            created.setDivision("Dhaka");
            created.setZilla("Dhaka");
            created.setDegrees("MBBS, FCPS (Medicine)");
            created.setSpecialization("Internal Medicine");
            created.setSpecialities("Diabetes, Thyroid, Hypertension");
            created.setEducation("Dhaka Medical College");
            created.setMedicalCollege("Dhaka Medical College");
            created.setExperienceYears(9);
            created.setHospitalName("CloudCare Demo Hospital");
            created.setHospitalAddress("Dhanmondi, Dhaka");
            created.setConsultationFee("800");
            created.setDescription("Internal medicine specialist with a focus on diabetes, thyroid and lifestyle-related conditions. Available for in-person and telemedicine consultations.");
            created.setWorkingDays("Sun – Thu");
            created.setWorkingHours("10:00 – 17:00");
            created.setLanguages("Bangla, English");
            created.setRating("4.9");
            created.setTelemedicineAvailable(true);
            created.setPassword(passwordEncoder.encode(DEMO_PASSWORD));
            created.setStatus(Doctor.Status.APPROVED);
            return doctorRepository.save(created);
        });

        if (!passwordEncoder.matches(DEMO_PASSWORD, doctor.getPassword())) {
            doctor.setPassword(passwordEncoder.encode(DEMO_PASSWORD));
        }
        doctor.setStatus(Doctor.Status.APPROVED);
        doctor.setTelemedicineAvailable(true);
        return doctorRepository.save(doctor);
    }

    private void ensureDemoAppointments(Patient patient, Doctor doctor) {
        List<Appointment> existing = appointmentRepository.findByDoctorId(doctor.getId());
        boolean alreadyHasDemoPatient = existing.stream()
                .anyMatch(a -> a.getPatient() != null && patient.getId().equals(a.getPatient().getId()));
        if (alreadyHasDemoPatient) {
            return;
        }

        // A confirmed telemedicine visit gives the patient an immediate
        // "Join meeting" button and proves the Jitsi link pipeline works.
        Appointment confirmed = createAppointment(
                patient, doctor,
                LocalDate.now().plusDays(2),
                LocalTime.of(10, 0), LocalTime.of(11, 0),
                Appointment.Status.CONFIRMED);
        String roomName = "appt-" + confirmed.getId() + "-"
                + UUID.randomUUID().toString().substring(0, 8);
        confirmed.setTelemedicineLink("https://meet.jit.si/" + roomName);
        appointmentRepository.save(confirmed);

        // A pending telemedicine visit gives the doctor a reason to use the
        // confirm action and watch a meeting link appear.
        createAppointment(
                patient, doctor,
                LocalDate.now().plusDays(3),
                LocalTime.of(11, 0), LocalTime.of(12, 0),
                Appointment.Status.PENDING);
    }

    /**
     * One prescription with a follow-up a week out, so the patient page and
     * the doctor's prescription history are not empty on a fresh database.
     * Guarded by the doctor/patient pair, so restarting does not duplicate it.
     */
    private void ensureDemoPrescription(Patient patient, Doctor doctor) {
        boolean exists = !prescriptionRepository
                .findByDoctorIdAndPatientIdOrderByCreatedAtDesc(doctor.getId(), patient.getId())
                .isEmpty();
        if (exists) {
            return;
        }

        LocalDate followUp = LocalDate.now().plusDays(7);
        Prescription prescription = new Prescription(
                patient,
                doctor,
                "Hypothyroidism — routine follow-up",
                "1. Levothyroxine 50 mcg — once daily before breakfast\n"
                        + "2. Continue the current dose until the next review",
                "Bring your latest TSH report. Keep a consistent morning routine and avoid taking iron or calcium within four hours.",
                followUp);
        prescriptionRepository.save(prescription);

        notification note = new notification(
                "A new prescription from " + doctor.getName()
                        + " is available. Follow-up visit: " + followUp + ".",
                LocalDateTime.now(), patient);
        patientService.addNotification(patient, note);
    }

    private Appointment createAppointment(Patient patient,
                                          Doctor doctor,
                                          LocalDate date,
                                          LocalTime start,
                                          LocalTime end,
                                          Appointment.Status status) {
        DoctorAvailability slot = new DoctorAvailability();
        slot.setDoctor(doctor);
        slot.setDate(Date.valueOf(date));
        slot.setStartTime(start);
        slot.setEndTime(end);
        slot.setTelemedicineAvailable(true);
        slot.setBooked(true);
        availabilityRepository.save(slot);

        Appointment appointment = new Appointment();
        appointment.setDoctor(doctor);
        appointment.setPatient(patient);
        appointment.setAppointmentDate(Date.valueOf(date));
        appointment.setTimeSlot(start + "-" + end);
        appointment.setType(Appointment.AppointmentType.TELEMEDICINE);
        appointment.setStatus(status);
        return appointmentRepository.save(appointment);
    }
}
