package com.example.cloud.care.service;

import com.example.cloud.care.model.Donor;
import com.example.cloud.care.model.Patient;
import com.example.cloud.care.repository.DonorRepository;
import com.example.cloud.care.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Service
public class DonorService {

    @Autowired
    private com.example.cloud.care.service.EmailServiceDonor emailServiceDonor; // Async donor email service

    private final DonorRepository donorRepository;

    @Autowired
    private UserRepository userRepository;

    public DonorService(DonorRepository donorRepository) {
        this.donorRepository = donorRepository;
    }

    public List<Donor> findMatchingDonors(String bloodGroup) {
        return donorRepository.findByBloodGroup(bloodGroup).stream()
                .distinct()
                .toList();
    }

    public List<Donor> getAllDonors() {
        return donorRepository.findAll().stream()
                .distinct()
                .toList();
    }

    /**
     * Comprehensive eligibility checking based on medical criteria
     * Returns eligibility status and reason
     */
    public Map<String, Object> checkEligibility(Donor donor) {
        String status = "Eligible";
        StringBuilder reason = new StringBuilder();

        // 1. Age check (18-65 years)
        Integer age = donor.getAge();
        if (age == null || age < 18 || age > 65) {
            status = "Not Eligible";
            reason.append("Age must be between 18-65 years. ");
        }

        // 2. Blood group verification
        if (donor.getBloodGroup() == null || donor.getBloodGroup().isEmpty()) {
            status = "Not Eligible";
            reason.append("Blood group is required. ");
        }

        // 3. Disease check - Critical level diseases disqualify
        if (donor.getDiseases() != null && !donor.getDiseases().isEmpty()) {
            boolean hasCriticalDisease = donor.getDiseases().stream()
                    .anyMatch(d -> d.getLevel() == 3);
            boolean hasMediumDisease = donor.getDiseases().stream()
                    .anyMatch(d -> d.getLevel() == 2);
            if (hasCriticalDisease) {
                status = "Not Eligible";
                reason.append("Critical diseases (Level 3) disqualify donor. ");
            } else if (hasMediumDisease) {
                status = "Not Eligible";
                reason.append("Medium severity diseases (Level 2) disqualify donor. ");
            }
            // Level 1 (Low) diseases: allow donation with caution
        }

        // 4. Last donation check (minimum 3 months gap)
        if (donor.getLastDonated() != null) {
            long monthsSinceDonation = ChronoUnit.MONTHS.between(donor.getLastDonated(), LocalDate.now());
            if (monthsSinceDonation < 3) {
                status = "Not Eligible";
                reason.append(String.format("Must wait %d more months before donating again. ", 3 - monthsSinceDonation));
            }
        }

        // 5. Gender check (no restrictions but note for records)
        if (donor.getGender() == null || donor.getGender().isEmpty()) {
            status = "Not Eligible";
            reason.append("Gender is required. ");
        }

        return Map.of(
                "status", status,
                "reason", reason.toString().trim(),
                "eligible", status.equals("Eligible")
        );
    }

    public Donor saveDonor(Donor donor) {
        // Auto-fetch fields from User/Patient if available
        String email = donor.getEmail();
        if (email != null && !email.isBlank()) {
            userRepository.findByEmail(email).ifPresent(user -> {
                donor.setName(user.getName());
                if (user.getPatient() != null) {
                    Patient patient = user.getPatient();
                    // Only set blood group from patient if donor doesn't already have one
                    if ((donor.getBloodGroup() == null || donor.getBloodGroup().isEmpty()) 
                        && patient.getBloodGroup() != null && !patient.getBloodGroup().isEmpty()) {
                        donor.setBloodGroup(patient.getBloodGroup());
                    }
                    if (patient.getAge() != null && patient.getAge() > 0 && donor.getAge() <= 0) {
                        donor.setAge(patient.getAge());
                    }
                    if (patient.getGender() != null && (donor.getGender() == null || donor.getGender().isEmpty())) {
                        donor.setGender(patient.getGender().toString());
                    }
                    if (patient.getLastDonated() != null && donor.getLastDonated() == null) {
                        donor.setLastDonated(patient.getLastDonated().toInstant()
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate());
                    }
                }
            });
        }

        // Eligibility check
        Map<String, Object> eligibilityResult = checkEligibility(donor);
        String eligibilityStatus = (String) eligibilityResult.get("status");
        donor.setStatus(eligibilityStatus);

        // Save donor to DB
        Donor saved = donorRepository.save(donor);

        return saved;
    }

    public void deleteDonor(Long id) {
        if (id != null) {
            donorRepository.deleteById(id);
        }
    }

    @Transactional(readOnly = true)
    public Donor getDonor(Long id) {
        if (id == null) return null;
        java.util.Optional<Donor> opt = donorRepository.findById(id);
        if (opt.isEmpty()) return null;

        Donor donor = opt.get();
        try {
            if (donor.getDiseases() != null) donor.getDiseases().size(); // initialize lazy collection
        } catch (Exception e) {
            // ignore initialization errors
        }
        return donor;
    }

    public List<Donor> findMatchingDonors(String bloodGroup, String location, LocalDate requiredDate) {
        // 1. Filter by blood group
        List<Donor> donors = donorRepository.findByBloodGroup(bloodGroup);

        // 2. Filter by location (optional)
        donors = donors.stream()
                .filter(d -> d.getLocation() != null && d.getLocation().equalsIgnoreCase(location))
                .toList();

        // 3. Filter by eligibility
        donors = donors.stream()
                .filter(d -> {
                    Map<String, Object> result = checkEligibility(d);
                    return (boolean) result.get("eligible");
                })
                .toList();

        return donors;
    }
}
