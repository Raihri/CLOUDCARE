package com.example.doctors.repository;

import com.example.doctors.model.Doctor;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-memory repository for doctors.
 * Keyed by Long id. When updating a doctor's ID, use saveWithIdChange(oldId, newDoctor)
 * which will remove the old entry and put the new one, overwriting any existing doctor at newId.
 */
@Repository
public class DoctorRepository {
    private final Map<Long, Doctor> store = new ConcurrentHashMap<>();

    public List<Doctor> findAll() {
        ArrayList<Doctor> list = new ArrayList<>(store.values());
        list.sort(Comparator.comparing(Doctor::getId));
        return list;
    }

    public Optional<Doctor> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public void save(Doctor d) {
        if (d.getId() == null) {
            // generate simple id if null
            long next = store.keySet().stream().mapToLong(Long::longValue).max().orElse(0L) + 1;
            d.setId(next);
        }
        // put will overwrite existing if same id exists (per your requirement replace not duplicate)
        store.put(d.getId(), d);
    }

    public void saveWithIdChange(Long oldId, Doctor newDoctor) {
        // remove old id if present
        if (oldId != null) {
            store.remove(oldId);
        }
        if (newDoctor.getId() == null) {
            long next = store.keySet().stream().mapToLong(Long::longValue).max().orElse(0L) + 1;
            newDoctor.setId(next);
        }
        // This will overwrite any existing doctor at newDoctor.getId()
        store.put(newDoctor.getId(), newDoctor);
    }

    public void delete(Long id) {
        store.remove(id);
    }

    // helper to pre-load sample data
    public void initSample() {
        Doctor d1 = new Doctor();
        d1.setId(1L);
        d1.setFullName("Dr. Ayesha Rahman");
        d1.setEmail("ayesha@example.com");
        d1.setPhone("+8801712345678");
        d1.setSpecialization("Cardiology");
        d1.setEducation("MBBS, MD (Cardiology)");
        d1.setHospitalName("Dhaka General Hospital");
        // do NOT set pictureUrl here so templates show fallback /images/image.png
        d1.setLanguages("Bengali, English");
        save(d1);

        Doctor d2 = new Doctor();
        d2.setId(2L);
        d2.setFullName("Dr. Kamal Hossain");
        d2.setEmail("kamal@example.com");
        d2.setPhone("+8801911122233");
        d2.setSpecialization("Pediatrics");
        d2.setEducation("MBBS, FCPS");
        d2.setHospitalName("Chittagong Children's Clinic");
        // do NOT set pictureUrl here as well
        d2.setLanguages("Bengali, English");
        save(d2);
    }
}
