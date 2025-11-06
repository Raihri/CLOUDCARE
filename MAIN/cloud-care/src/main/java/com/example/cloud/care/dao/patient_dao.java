package com.example.cloud.care.dao;

import com.example.cloud.care.var.patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface patient_dao extends JpaRepository<patient,Integer> {


}
