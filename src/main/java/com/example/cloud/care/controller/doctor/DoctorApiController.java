package com.example.cloud.care.controller.doctor;

import com.example.cloud.care.model.Appointment;
import com.example.cloud.care.model.Doctor;
import com.example.cloud.care.model.DoctorAvailability;
import com.example.cloud.care.model.Patient;
import com.example.cloud.care.model.Prescription;
import com.example.cloud.care.model.User;
import com.example.cloud.care.model.Notification;
import com.example.cloud.care.dto.PrescriptionDto;
import com.example.cloud.care.service.AppointmentService;
import com.example.cloud.care.service.PrescriptionService;
import com.example.cloud.care.service.UserService;
import com.example.cloud.care.service.DoctorService;
import com.example.cloud.care.service.DoctorAvailabilityService;
import com.example.cloud.care.service.NotificationService;
import com.example.cloud.care.service.PatientService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * JSON API behind the Next.js doctor portal.
 *
 * <p>Every handler resolves "who am I" from the SecurityContext rather than
 * taking a doctor id from the request, so a doctor can't read or mutate another
 * doctor's calendar by editing a URL. Status changes additionally verify that
 * the target appointment is in the caller's own list.
 *
 * <p>Responses are explicit maps rather than serialised {@link Doctor}
 * entities, which carry {@code password} and {@code resetToken}.
 */
@Controller
@RequestMapping("/doctor/api")
public class DoctorApiController {

    private static final String DATE_FMT = "yyyy-MM-dd";
    private static final String TIME_FMT = "HH:mm";

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private DoctorAvailabilityService availabilityService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private PrescriptionService prescriptionService;

    // ------------------------------------------------------------------
    // Identity
    // ------------------------------------------------------------------

    /** The signed-in doctor, resolved from the authenticated email. */
    private Doctor currentDoctor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) return null;
        return doctorService.findByEmail(auth.getName());
    }

    /** Returns an error envelope when the caller isn't a known doctor, else null. */
    private Map<String, Object> denied() {
        if (currentDoctor() != null) return null;
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("success", false);
        m.put("message", "Doctor not found.");
        return m;
    }

    @GetMapping("/me")
    @ResponseBody
    public Map<String, Object> me() {
        Doctor doc = currentDoctor();
        if (doc == null) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("success", false);
            m.put("message", "Doctor not found.");
            return m;
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("success", true);
        out.putAll(profileDto(doc));
        return out;
    }

    // ------------------------------------------------------------------
    // Dashboard
    // ------------------------------------------------------------------

    @GetMapping("/dashboard")
    @ResponseBody
    public Map<String, Object> dashboard() {
        Map<String, Object> denied = denied();
        if (denied != null) return denied;

        Doctor doc = currentDoctor();
        List<Appointment> appointments = appointmentService.getAppointmentsByDoctor(doc.getId());

        long pending = appointments.stream()
                .filter(a -> a.getStatus() == Appointment.Status.PENDING)
                .count();
        long confirmed = appointments.stream()
                .filter(a -> a.getStatus() == Appointment.Status.CONFIRMED)
                .count();
        long cancelled = appointments.stream()
                .filter(a -> a.getStatus() == Appointment.Status.CANCELLED)
                .count();

        List<DoctorAvailability> slots = availabilityService.getAvailabilityForDoctor(doc);
        long openSlots = slots.stream().filter(s -> !s.isBooked()).count();

        // Today's onward agenda, soonest first — the doctor's "what's next" list.
        // Compared as Dates so a time component doesn't skew the cutoff.
        Date today = new Date();
        List<Appointment> upcoming = appointments.stream()
                .filter(a -> a.getAppointmentDate() != null
                        && !a.getAppointmentDate().before(today))
                .sorted((x, y) -> x.getAppointmentDate().compareTo(y.getAppointmentDate()))
                .limit(5)
                .toList();

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("success", true);
        out.put("name", doc.getName());
        out.put("status", doc.getStatus() != null ? doc.getStatus().name() : null);
        out.put("totalAppointments", appointments.size());
        out.put("pendingAppointments", pending);
        out.put("confirmedAppointments", confirmed);
        out.put("cancelledAppointments", cancelled);
        out.put("openSlots", openSlots);
        out.put("upcoming", upcoming.stream().map(this::appointmentDto).toList());
        return out;
    }

    // ------------------------------------------------------------------
    // Profile
    // ------------------------------------------------------------------

    @PostMapping("/profile")
    @ResponseBody
    public Map<String, Object> updateProfile(
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String division,
            @RequestParam(required = false) String zilla,
            @RequestParam(required = false) String hospitalName,
            @RequestParam(required = false) String hospitalAddress,
            @RequestParam(required = false) String consultationFee,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String workingDays,
            @RequestParam(required = false) String workingHours,
            @RequestParam(required = false) String languages,
            @RequestParam(required = false) String certifications,
            @RequestParam(required = false) String specialInterests) {

        Map<String, Object> denied = denied();
        if (denied != null) return denied;

        Doctor doc = currentDoctor();
        // Only contact/practice fields are writable here. Name, BMDC number and
        // status gate who may practise and are an admin's call, not a
        // self-service edit.
        if (phoneNumber != null) doc.setPhoneNumber(phoneNumber);
        if (address != null) doc.setAddress(address);
        if (division != null) doc.setDivision(division);
        if (zilla != null) doc.setZilla(zilla);
        if (hospitalName != null) doc.setHospitalName(hospitalName);
        if (hospitalAddress != null) doc.setHospitalAddress(hospitalAddress);
        if (consultationFee != null) doc.setConsultationFee(consultationFee);
        if (description != null) doc.setDescription(description);
        if (workingDays != null) doc.setWorkingDays(workingDays);
        if (workingHours != null) doc.setWorkingHours(workingHours);
        if (languages != null) doc.setLanguages(languages);
        if (certifications != null) doc.setCertifications(certifications);
        if (specialInterests != null) doc.setSpecialInterests(specialInterests);

        doctorService.saveDoctor(doc);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("success", true);
        out.put("message", "Profile updated successfully!");
        return out;
    }

    // ------------------------------------------------------------------
    // Availability
    // ------------------------------------------------------------------

    @GetMapping("/availability")
    @ResponseBody
    public Map<String, Object> availability() {
        Map<String, Object> denied = denied();
        if (denied != null) return denied;

        Doctor doc = currentDoctor();
        List<DoctorAvailability> slots = availabilityService.getAvailabilityForDoctor(doc);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("success", true);
        out.put("count", slots.size());
        out.put("data", slots.stream().map(this::slotDto).toList());
        return out;
    }

    @PostMapping("/availability")
    @ResponseBody
    public Map<String, Object> addSlot(
            @RequestParam @DateTimeFormat(pattern = DATE_FMT) Date date,
            @RequestParam String startTime,
            @RequestParam String endTime,
            @RequestParam(required = false, defaultValue = "false") boolean telemedicine) {

        Map<String, Object> denied = denied();
        if (denied != null) return denied;

        Doctor doctor = currentDoctor();
        LocalTime start;
        LocalTime end;
        try {
            start = LocalTime.parse(startTime);
            end = LocalTime.parse(endTime);
        } catch (Exception e) {
            return error("Times must be in HH:mm format.");
        }

        if (!start.isBefore(end)) {
            return error("The start time must be before the end time.");
        }

        // Same overlap rule as the Thymeleaf form: a slot that partially covers
        // an existing one would produce a double-bookable calendar.
        for (DoctorAvailability existing : availabilityService.getAvailabilityForDoctor(doctor)) {
            if (date.equals(existing.getDate())
                    && start.isBefore(existing.getEndTime())
                    && end.isAfter(existing.getStartTime())) {
                return error("That overlaps a slot you've already added.");
            }
        }

        DoctorAvailability slot = new DoctorAvailability();
        slot.setDate(date);
        slot.setStartTime(start);
        slot.setEndTime(end);
        slot.setTelemedicineAvailable(telemedicine);
        slot.setDoctor(doctor);
        availabilityService.save(slot);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("success", true);
        out.put("message", "Slot added.");
        out.put("slot", slotDto(slot));
        return out;
    }

    @DeleteMapping("/availability/{slotId}")
    @ResponseBody
    public Map<String, Object> removeSlot(@PathVariable Long slotId) {
        Map<String, Object> denied = denied();
        if (denied != null) return denied;

        Doctor doctor = currentDoctor();
        DoctorAvailability slot = availabilityService.getSlotById(slotId);

        // Confirm ownership before deleting, so an id from another doctor can't
        // be used to clear their calendar.
        if (slot == null || slot.getDoctor() == null
                || slot.getDoctor().getId() != doctor.getId()) {
            return error("That slot does not belong to you.");
        }
        if (slot.isBooked()) {
            return error("That slot is booked by a patient. Cancel the appointment first.");
        }

        availabilityService.delete(slotId);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("success", true);
        out.put("message", "Slot removed.");
        return out;
    }

    // ------------------------------------------------------------------
    // Appointments
    // ------------------------------------------------------------------

    @GetMapping("/appointments")
    @ResponseBody
    public Map<String, Object> appointments() {
        Map<String, Object> denied = denied();
        if (denied != null) return denied;

        Doctor doc = currentDoctor();
        List<Appointment> list = appointmentService.getAppointmentsByDoctor(doc.getId());

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("success", true);
        out.put("count", list.size());
        out.put("data", list.stream().map(this::appointmentDto).toList());
        return out;
    }

    @PostMapping("/appointments/{appointmentId}/confirm")
    @ResponseBody
    public Map<String, Object> confirm(@PathVariable Long appointmentId) {
        Map<String, Object> denied = denied();
        if (denied != null) return denied;
        return setAppointmentStatus(appointmentId, Appointment.Status.CONFIRMED);
    }

    @PostMapping("/appointments/{appointmentId}/cancel")
    @ResponseBody
    public Map<String, Object> cancel(@PathVariable Long appointmentId) {
        Map<String, Object> denied = denied();
        if (denied != null) return denied;
        return setAppointmentStatus(appointmentId, Appointment.Status.CANCELLED);
    }

    /**
     * Applies a status change, but only after confirming the appointment is in
     * the caller's own list — otherwise any doctor could confirm or cancel
     * another doctor's patient by guessing an id.
     */
    private Map<String, Object> setAppointmentStatus(Long appointmentId, Appointment.Status status) {
        Doctor doc = currentDoctor();

        // Appointment.id is a primitive int, so comparing with Long.equals
        // would always be false — compare as numbers instead.
        Appointment target = appointmentService.getAppointmentsByDoctor(doc.getId()).stream()
                .filter(a -> a.getId() == appointmentId.longValue())
                .findFirst()
                .orElse(null);

        if (target == null) {
            return error("That appointment does not belong to you.");
        }

        try {
            if (status == Appointment.Status.CONFIRMED) {
                appointmentService.confirmAppointment(target);
            } else {
                appointmentService.cancelAppointment(target);
            }
        } catch (Exception e) {
            return error(e.getMessage());
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("success", true);
        out.put("message", "Appointment " + status.name().toLowerCase() + ".");
        out.put("appointment", appointmentDto(target));
        return out;
    }

    /**
     * The distinct patients this doctor has appointments with.
     *
     * <p>Derived from the doctor's own appointment list rather than a separate
     * patient query, so the result can't include someone the doctor has never
     * actually seen. Sorted by name for a scannable list.
     */
    @GetMapping("/patients")
    @ResponseBody
    public Map<String, Object> patients() {
        Map<String, Object> denied = denied();
        if (denied != null) return denied;

        Doctor doc = currentDoctor();
        Map<Long, Map<String, Object>> byPatient = new LinkedHashMap<>();

        for (Appointment a : appointmentService.getAppointmentsByDoctor(doc.getId())) {
            if (a.getPatient() == null || a.getPatient().getId() == null) continue;

            Map<String, Object> entry = byPatient.computeIfAbsent(
                    a.getPatient().getId(), id -> {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("id", id);
                        m.put("name", a.getPatient().getUser() != null
                                ? a.getPatient().getUser().getName() : null);
                        m.put("email", a.getPatient().getUser() != null
                                ? a.getPatient().getUser().getEmail() : null);
                        m.put("bloodGroup", a.getPatient().getBloodGroup());
                        m.put("gender", a.getPatient().getGender() != null
                                ? String.valueOf(a.getPatient().getGender()) : null);
                        m.put("age", a.getPatient().getAge());
                        m.put("zilla", a.getPatient().getZilla());
                        m.put("appointments", 0L);
                        m.put("lastVisit", null);
                        return m;
                    });

            entry.put("appointments", ((Long) entry.get("appointments")) + 1L);
            if (a.getAppointmentDate() != null) {
                // Tracked as a Date and formatted at the end — comparing
                // pre-formatted strings would work here but wouldn't survive a
                // change to the date format.
                Date current = (Date) entry.get("lastVisitDate");
                if (current == null || a.getAppointmentDate().after(current)) {
                    entry.put("lastVisitDate", a.getAppointmentDate());
                }
            }
        }

        List<Map<String, Object>> list = new ArrayList<>(byPatient.values());
        SimpleDateFormat dateFmt = new SimpleDateFormat(DATE_FMT);
        for (Map<String, Object> entry : list) {
            Date last = (Date) entry.remove("lastVisitDate");
            entry.put("lastVisit", last == null ? null : dateFmt.format(last));
        }

        list.sort((x, y) -> {
            String nx = (String) x.get("name");
            String ny = (String) y.get("name");
            if (nx == null) return 1;
            if (ny == null) return -1;
            return nx.compareToIgnoreCase(ny);
        });

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("success", true);
        out.put("count", list.size());
        out.put("data", list);
        return out;
    }

    // ------------------------------------------------------------------
    // Patient lookup / editing
    // ------------------------------------------------------------------

    /**
     * Full chart for one patient, addressed by the patient id a doctor may have
     * from a prescription, a referral or the patient list.
     *
     * <p>This is intentionally a directory lookup rather than a "my patients
     * only" endpoint: a doctor may need to see a patient before the first
     * appointment is created. It is still role-gated by the doctor security
     * chain, and every response is shaped by hand so password hashes and lazy
     * JPA graphs never leak.
     */
    @GetMapping("/patients/{patientId}")
    @ResponseBody
    @Transactional(readOnly = true)
    public Map<String, Object> patientRecord(@PathVariable Long patientId) {
        Map<String, Object> denied = denied();
        if (denied != null) return denied;

        Doctor doctor = currentDoctor();
        Optional<Patient> found = patientService.findById(patientId);
        if (found.isEmpty()) {
            return error("No patient found with ID " + patientId + ".");
        }

        Patient patient = found.get();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("success", true);
        out.putAll(patientDto(patient, doctor));
        return out;
    }

    /**
     * Consolidated patient chart update used by the doctor's edit form.
     *
     * <p>Only fields present in the request are changed. Blank numeric fields
     * arrive as null from Spring's converter and therefore leave the existing
     * value alone, which is important: the form is long and a doctor may only
     * want to correct one reading.
     */
    @PostMapping("/patients/{patientId}")
    @ResponseBody
    @Transactional
    public Map<String, Object> updatePatientRecord(
            @PathVariable Long patientId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String bloodGroup,
            @RequestParam(required = false) Integer age,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) @DateTimeFormat(pattern = DATE_FMT) Date dateOfBirth,
            @RequestParam(required = false) String maritalStatus,
            @RequestParam(required = false) String nationalId,
            @RequestParam(required = false) String emergencyContact,
            @RequestParam(required = false) String division,
            @RequestParam(required = false) String zilla,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) Double weight,
            @RequestParam(required = false) Double height,
            @RequestParam(required = false) String bloodPressure,
            @RequestParam(required = false) Integer systolicBP,
            @RequestParam(required = false) Integer diastolicBP,
            @RequestParam(required = false) Integer heartRate,
            @RequestParam(required = false) Integer oxygenSaturation,
            @RequestParam(required = false) Double respiratoryRate,
            @RequestParam(required = false) Double bodyTemperature,
            @RequestParam(required = false) Double hb,
            @RequestParam(required = false) Double rbc,
            @RequestParam(required = false) Double wbc,
            @RequestParam(required = false) Double platelet,
            @RequestParam(required = false) Double sugarLevel,
            @RequestParam(required = false) Double bloodSugarLevel,
            @RequestParam(required = false) Double cholesterolTotal,
            @RequestParam(required = false) Double cholesterolLDL,
            @RequestParam(required = false) Double cholesterolHDL,
            @RequestParam(required = false) Double triglyceride,
            @RequestParam(required = false) Double creatinine,
            @RequestParam(required = false) Double creatinineLevel,
            @RequestParam(required = false) Double alt,
            @RequestParam(required = false) Double ast,
            @RequestParam(required = false) Double bilirubin,
            @RequestParam(required = false) String rhFactor,
            @RequestParam(required = false) Double bmi,
            @RequestParam(required = false) Double bmr,
            @RequestParam(required = false) Double cholesterol,
            @RequestParam(required = false) Double bloodSugar,
            @RequestParam(required = false) Double sleepHours,
            @RequestParam(required = false) Double waterIntake,
            @RequestParam(required = false) Boolean smoker,
            @RequestParam(required = false) Boolean alcoholConsumer,
            @RequestParam(required = false) Integer physicalActivity,
            @RequestParam(required = false) Integer stressLevel,
            @RequestParam(required = false) Integer anxietyScore,
            @RequestParam(required = false) String allergies,
            @RequestParam(required = false) String diseases,
            @RequestParam(required = false) String medications,
            @RequestParam(required = false) String symptoms,
            @RequestParam(required = false) String familyDiseaseHistory,
            @RequestParam(required = false) String vaccinationHistory,
            @RequestParam(required = false) String pastSurgeries,
            @RequestParam(required = false) String mentalDiseases) {

        Map<String, Object> denied = denied();
        if (denied != null) return denied;

        Doctor doctor = currentDoctor();
        Optional<Patient> found = patientService.findById(patientId);
        if (found.isEmpty()) {
            return error("No patient found with ID " + patientId + ".");
        }

        Patient patient = found.get();
        User patientUser = patient.getUser();

        if (name != null && !name.trim().isEmpty()) {
            patientUser.setName(name.trim());
            userService.saveUser(patientUser);
        }
        if (bloodGroup != null) patient.setBloodGroup(bloodGroup.trim());
        if (age != null) patient.setAge(age);
        if (gender != null && !gender.isBlank()) patient.setGender(gender.charAt(0));
        if (dateOfBirth != null) patient.setDateOfBirth(dateOfBirth);
        if (maritalStatus != null) patient.setMaritalStatus(maritalStatus.trim());
        if (nationalId != null) patient.setNationalId(nationalId.trim());
        if (emergencyContact != null) patient.setEmergencyContact(emergencyContact.trim());
        if (division != null) patient.setDivision(division.trim());
        if (zilla != null) patient.setZilla(zilla.trim());
        if (address != null) patient.setAddress(address.trim());
        if (weight != null) patient.setWeight(weight);
        if (height != null) patient.setHeight(height);

        if (bloodPressure != null) patient.setBloodPressure(bloodPressure.trim());
        if (systolicBP != null) patient.setSystolicBP(systolicBP);
        if (diastolicBP != null) patient.setDiastolicBP(diastolicBP);
        if (heartRate != null) patient.setHeartRate(heartRate);
        if (oxygenSaturation != null) patient.setOxygenSaturation(oxygenSaturation);
        if (respiratoryRate != null) patient.setRespiratoryRate(respiratoryRate);
        if (bodyTemperature != null) patient.setBodyTemperature(bodyTemperature);

        if (hb != null) patient.setHb(hb);
        if (rbc != null) patient.setRbc(rbc);
        if (wbc != null) patient.setWbc(wbc);
        if (platelet != null) patient.setPlatelet(platelet);
        if (sugarLevel != null) patient.setSugarLevel(sugarLevel);
        if (bloodSugarLevel != null) patient.setBloodSugarLevel(bloodSugarLevel);
        if (cholesterolTotal != null) patient.setCholesterolTotal(cholesterolTotal);
        if (cholesterolLDL != null) patient.setCholesterolLDL(cholesterolLDL);
        if (cholesterolHDL != null) patient.setCholesterolHDL(cholesterolHDL);
        if (triglyceride != null) patient.setTriglyceride(triglyceride);
        if (creatinine != null) patient.setCreatinine(creatinine);
        if (creatinineLevel != null) patient.setCreatinineLevel(creatinineLevel);
        if (alt != null) patient.setAlt(alt);
        if (ast != null) patient.setAst(ast);
        if (bilirubin != null) patient.setBilirubin(bilirubin);
        if (rhFactor != null) patient.setRhFactor(rhFactor.trim());

        if (bmi != null) patient.setBmi(bmi);
        if (bmr != null) patient.setBmr(bmr);
        if (cholesterol != null) patient.setCholesterol(cholesterol);
        if (bloodSugar != null) patient.setBloodSugar(bloodSugar);

        if (sleepHours != null) patient.setSleepHours(sleepHours);
        if (waterIntake != null) patient.setWaterIntake(waterIntake);
        if (smoker != null) patient.setSmoker(smoker);
        if (alcoholConsumer != null) patient.setAlcoholConsumer(alcoholConsumer);
        if (physicalActivity != null) patient.setPhysicalActivity(physicalActivity);
        if (stressLevel != null) patient.setStressLevel(stressLevel);
        if (anxietyScore != null) patient.setAnxietyScore(anxietyScore);

        if (allergies != null) patient.setAllergies(splitCsv(allergies));
        if (diseases != null) patient.setDiseases(splitCsv(diseases));
        if (medications != null) patient.setMedications(splitCsv(medications));
        if (symptoms != null) patient.setSymptoms(splitCsv(symptoms));
        if (familyDiseaseHistory != null) {
            patient.setFamilyDiseaseHistory(splitCsv(familyDiseaseHistory));
        }
        if (vaccinationHistory != null) {
            patient.setVaccinationHistory(splitCsv(vaccinationHistory));
        }
        if (pastSurgeries != null) patient.setPastSurgeries(splitCsv(pastSurgeries));
        if (mentalDiseases != null) patient.setMentalDiseases(splitCsv(mentalDiseases));

        patient.setUpdatedAt(new Date());
        patientService.save(patient);

        String doctorName = doctor.getName() == null ? "your doctor" : doctor.getName().trim();
        if (!doctorName.isEmpty() && !doctorName.startsWith("Dr.")) {
            doctorName = "Dr. " + doctorName;
        }
        Notification note = new Notification(
                "Your medical record was updated by " + doctorName + ".",
                LocalDateTime.now(), patient);
        patientService.addNotification(patient, note);
        if (patient.getUser() != null) {
            notificationService.sendToSpecific(patient.getUser().getEmail(), note);
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("success", true);
        out.put("message", "Patient record updated.");
        out.putAll(patientDto(patient, doctor));
        return out;
    }

    // ------------------------------------------------------------------
    // Prescriptions
    // ------------------------------------------------------------------

    /**
     * Prescriptions written by the signed-in doctor.
     *
     * <p>Passing {@code patientId} narrows the list to one patient, which is
     * what the prescription-maker page does after the doctor looks someone up.
     * Without it the page can still show the doctor's recent prescriptions.
     */
    @GetMapping("/prescriptions")
    @ResponseBody
    @Transactional(readOnly = true)
    public Map<String, Object> prescriptions(
            @RequestParam(required = false) Long patientId) {
        Map<String, Object> denied = denied();
        if (denied != null) return denied;

        Doctor doctor = currentDoctor();
        List<Prescription> list = prescriptionService.forDoctor(doctor.getId(), patientId);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("success", true);
        out.put("count", list.size());
        out.put("data", list.stream().map(PrescriptionDto::from).toList());
        return out;
    }

    /**
     * Write a prescription and notify the patient.
     *
     * <p>The patient ID is validated here rather than trusted from the form:
     * the lookup page lets a doctor type any numeric ID, and a typo should
     * produce a readable error rather than a foreign-key failure.
     */
    @PostMapping("/prescriptions")
    @ResponseBody
    @Transactional
    public Map<String, Object> createPrescription(
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) String diagnosis,
            @RequestParam(required = false) String medicines,
            @RequestParam(required = false) String instructions,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate followUpDate) {

        Map<String, Object> denied = denied();
        if (denied != null) return denied;

        if (patientId == null) {
            return error("Choose a patient first.");
        }
        if (medicines == null || medicines.isBlank()) {
            return error("Enter at least one medicine or treatment.");
        }
        if (followUpDate == null) {
            return error("Choose the follow-up date.");
        }
        if (followUpDate.isBefore(LocalDate.now())) {
            return error("The follow-up date cannot be in the past.");
        }

        Optional<Patient> found = patientService.findById(patientId);
        if (found.isEmpty()) {
            return error("No patient found with ID " + patientId + ".");
        }

        Doctor doctor = currentDoctor();
        try {
            Prescription saved = prescriptionService.create(
                    doctor, found.get(), diagnosis, medicines, instructions, followUpDate);

            Map<String, Object> out = new LinkedHashMap<>();
            out.put("success", true);
            out.put("message", "Prescription saved. The patient has been notified.");
            out.put("data", PrescriptionDto.from(saved));
            return out;
        } catch (RuntimeException e) {
            return error("Could not save the prescription: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // DTOs
    // ------------------------------------------------------------------

    private Map<String, Object> patientDto(Patient p, Doctor doctor) {
        Map<String, Object> m = new LinkedHashMap<>();
        User u = p.getUser();

        m.put("id", p.getId());
        m.put("name", u == null ? null : u.getName());
        m.put("email", u == null ? null : u.getEmail());
        m.put("photoUrl", u == null ? null : u.getPhotoUrl());

        m.put("bloodGroup", p.getBloodGroup());
        m.put("age", p.getAge());
        m.put("gender", p.getGender() == null ? null : String.valueOf(p.getGender()));
        m.put("dateOfBirth", formatDate(p.getDateOfBirth()));
        m.put("maritalStatus", p.getMaritalStatus());
        m.put("nationalId", p.getNationalId());
        m.put("emergencyContact", p.getEmergencyContact());
        m.put("division", p.getDivision());
        m.put("zilla", p.getZilla());
        m.put("address", p.getAddress());
        m.put("weight", p.getWeight());
        m.put("height", p.getHeight());

        m.put("bloodPressure", p.getBloodPressure());
        m.put("systolicBP", p.getSystolicBP());
        m.put("diastolicBP", p.getDiastolicBP());
        m.put("heartRate", p.getHeartRate());
        m.put("oxygenSaturation", p.getOxygenSaturation());
        m.put("respiratoryRate", p.getRespiratoryRate());
        m.put("bodyTemperature", p.getBodyTemperature());

        m.put("hb", p.getHb());
        m.put("rbc", p.getRbc());
        m.put("wbc", p.getWbc());
        m.put("platelet", p.getPlatelet());
        m.put("sugarLevel", p.getSugarLevel());
        m.put("bloodSugarLevel", p.getBloodSugarLevel());
        m.put("cholesterolTotal", p.getCholesterolTotal());
        m.put("cholesterolLDL", p.getCholesterolLDL());
        m.put("cholesterolHDL", p.getCholesterolHDL());
        m.put("triglyceride", p.getTriglyceride());
        m.put("creatinine", p.getCreatinine());
        m.put("creatinineLevel", p.getCreatinineLevel());
        m.put("alt", p.getAlt());
        m.put("ast", p.getAst());
        m.put("bilirubin", p.getBilirubin());
        m.put("rhFactor", p.getRhFactor());

        m.put("bmi", p.getBmi());
        m.put("bmr", p.getBmr());
        m.put("cholesterol", p.getCholesterol());
        m.put("bloodSugar", p.getBloodSugar());

        m.put("sleepHours", p.getSleepHours());
        m.put("waterIntake", p.getWaterIntake());
        m.put("smoker", p.isSmoker());
        m.put("alcoholConsumer", p.isAlcoholConsumer());
        m.put("physicalActivity", p.getPhysicalActivity());
        m.put("stressLevel", p.getStressLevel());
        m.put("anxietyScore", p.getAnxietyScore());

        m.put("allergies", copyList(p.getAllergies()));
        m.put("diseases", copyList(p.getDiseases()));
        m.put("medications", copyList(p.getMedications()));
        m.put("symptoms", copyList(p.getSymptoms()));
        m.put("familyDiseaseHistory", copyList(p.getFamilyDiseaseHistory()));
        m.put("vaccinationHistory", copyList(p.getVaccinationHistory()));
        m.put("pastSurgeries", copyList(p.getPastSurgeries()));
        m.put("mentalDiseases", copyList(p.getMentalDiseases()));

        m.put("updatedAt", formatTimestamp(p.getUpdatedAt()));

        List<Appointment> patientAppointments = appointmentService
                .getAppointmentsByDoctor(doctor.getId()).stream()
                .filter(a -> a.getPatient() != null
                        && p.getId().equals(a.getPatient().getId()))
                .sorted((x, y) -> {
                    if (x.getAppointmentDate() == null) return 1;
                    if (y.getAppointmentDate() == null) return -1;
                    return y.getAppointmentDate().compareTo(x.getAppointmentDate());
                })
                .toList();
        m.put("myPatient", !patientAppointments.isEmpty());
        m.put("appointmentCount", patientAppointments.size());
        m.put("appointments", patientAppointments.stream().map(this::appointmentDto).toList());
        m.put("lastVisit", patientAppointments.stream()
                .map(Appointment::getAppointmentDate)
                .filter(java.util.Objects::nonNull)
                .max(Date::compareTo)
                .map(DoctorApiController::formatDate)
                .orElse(null));

        return m;
    }

    private Map<String, Object> profileDto(Doctor d) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", d.getId());
        m.put("name", d.getName());
        m.put("email", d.getEmail());
        m.put("profileImage", d.getProfileImage());
        m.put("bloodGroup", d.getBloodGroup());
        m.put("gender", d.getGender());
        m.put("phoneNumber", d.getPhoneNumber());
        m.put("altPhone", d.getAltPhone());
        m.put("address", d.getAddress());
        m.put("division", d.getDivision());
        m.put("zilla", d.getZilla());
        m.put("bmdcRegNo", d.getBmdcRegNo());
        m.put("degrees", d.getDegrees());
        m.put("specialization", d.getSpecialization());
        m.put("education", d.getEducation());
        m.put("medicalCollege", d.getMedicalCollege());
        m.put("experienceYears", d.getExperienceYears());
        m.put("certifications", d.getCertifications());
        m.put("languages", d.getLanguages());
        m.put("hospitalName", d.getHospitalName());
        m.put("hospitalAddress", d.getHospitalAddress());
        m.put("consultationFee", d.getConsultationFee());
        m.put("description", d.getDescription());
        m.put("workingDays", d.getWorkingDays());
        m.put("workingHours", d.getWorkingHours());
        m.put("specialInterests", d.getSpecialInterests());
        m.put("rating", d.getRating());
        m.put("telemedicineAvailable", d.getTelemedicineAvailable());
        m.put("status", d.getStatus() != null ? d.getStatus().name() : null);
        return m;
    }

    private Map<String, Object> slotDto(DoctorAvailability s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", s.getId());
        m.put("date", s.getDate() == null ? null : new SimpleDateFormat(DATE_FMT).format(s.getDate()));
        m.put("startTime", s.getStartTime() == null ? null : s.getStartTime().format(java.time.format.DateTimeFormatter.ofPattern(TIME_FMT)));
        m.put("endTime", s.getEndTime() == null ? null : s.getEndTime().format(java.time.format.DateTimeFormatter.ofPattern(TIME_FMT)));
        m.put("booked", s.isBooked());
        m.put("telemedicineAvailable", s.getTelemedicineAvailable());
        return m;
    }

    private Map<String, Object> appointmentDto(Appointment a) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", a.getId());
        m.put("patientId", a.getPatient() != null ? a.getPatient().getId() : null);
        m.put("patientName", a.getPatient() != null && a.getPatient().getUser() != null
                ? a.getPatient().getUser().getName() : null);
        m.put("appointmentDate", a.getAppointmentDate() == null
                ? null : new SimpleDateFormat(DATE_FMT).format(a.getAppointmentDate()));
        m.put("timeSlot", a.getTimeSlot());
        m.put("type", a.getType() != null ? a.getType().name() : null);
        m.put("status", a.getStatus() != null ? a.getStatus().name() : null);
        m.put("telemedicineLink", a.getTelemedicineLink());
        return m;
    }

    private static List<String> copyList(List<String> values) {
        return values == null ? new ArrayList<>() : new ArrayList<>(values);
    }

    private static List<String> splitCsv(String raw) {
        List<String> out = new ArrayList<>();
        if (raw == null) return out;
        for (String part : raw.split(",")) {
            String value = part.trim();
            if (!value.isEmpty()) out.add(value);
        }
        return out;
    }

    private static String formatDate(Date date) {
        return date == null ? null : new SimpleDateFormat(DATE_FMT).format(date);
    }

    private static String formatTimestamp(Date date) {
        return date == null ? null : new SimpleDateFormat("yyyy-MM-dd HH:mm").format(date);
    }

    private static Map<String, Object> error(String message) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("success", false);
        m.put("message", message);
        return m;
    }
}