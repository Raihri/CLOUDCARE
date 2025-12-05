package com.example.cloud.care.repository;
import com.example.cloud.care.model.Donor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface DonorRepository extends JpaRepository<Donor, Long> {
    Optional<Donor> findByEmail(String email);
    java.util.List<Donor> findByBloodGroup(String bloodGroup);
}
    
