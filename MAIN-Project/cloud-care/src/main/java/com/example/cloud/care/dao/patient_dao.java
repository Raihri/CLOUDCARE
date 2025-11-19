package com.example.cloud.care.dao;

import com.example.cloud.care.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.lang.classfile.ClassFile.Option;
import java.util.List;
import java.util.Optional;

@Repository
public interface patient_dao extends JpaRepository<Patient,Integer> {
    Optional<Patient> findById(Long id);

}
