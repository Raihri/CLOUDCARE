package com.example.cloud.care.service;

import com.example.cloud.care.model.Doctor;
import com.example.cloud.care.model.DoctorAvailability;
import com.example.cloud.care.repository.DoctorAvailabilityRepository;
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

    public void save(DoctorAvailability slot) {
        // TODO Auto-generated method stub
         availabilityRepository.save(slot); 
    }
}