package com.example.cloud.care.dao;

import com.example.cloud.care.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AdminDao extends JpaRepository<Admin, Long> {
    Optional<Admin> findByUsername(String username);
}
