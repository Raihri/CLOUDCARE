package com.example.cloud.care.dao;

import com. example.cloud.care.var.doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface doctor_dao extends JpaRepository<doctor, Integer> {
}