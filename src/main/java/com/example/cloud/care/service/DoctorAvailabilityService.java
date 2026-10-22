package com.example.cloud.care.service;

import com.example.cloud.care.model.Doctor;
import com.example.cloud.care.model.DoctorAvailability;
import com.example.cloud.care.repository.DoctorAvailabilityRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorAvailabilityService {

    private final DoctorAvailabilityRepository availabilityRepository;

    public List<DoctorAvailability> getAvailabilityForDoctor(Doctor doctor) {
        if (doctor == null) {
        return List.of();
    }

    List<DoctorAvailability> list = availabilityRepository.findByDoctor(doctor);

    return list != null ? list : List.of();
    }

    public DoctorAvailability getSlotById(Long id) {
        return availabilityRepository.findById(id).orElse(null);
    }
    @Transactional
    public void save(DoctorAvailability slot) {
         availabilityRepository.save(slot);
    }

    /**
     * Delete a slot by id.
     *
     * <p>Callers are expected to have already checked the slot belongs to the
     * doctor making the request — this method deletes whatever it is handed.
     */
    @Transactional
    public void delete(Long slotId) {
        availabilityRepository.deleteById(slotId);
    }
}