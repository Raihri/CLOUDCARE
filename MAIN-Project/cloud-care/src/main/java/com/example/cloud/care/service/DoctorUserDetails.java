package com.example.cloud.care.service;

import com.example.cloud.care.var.doctor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class DoctorUserDetails implements UserDetails {

    private final doctor doctor;

    public DoctorUserDetails(doctor doctor) {
        this.doctor = doctor;
    }

    public doctor getDoctor() {
        return doctor;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList(); // no roles for now
    }

    @Override
    public String getPassword() {
        return doctor.getPassword();
    }

    @Override
    public String getUsername() {
        return doctor.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // compare with the enum using the fully-qualified type name to avoid
        // confusion with the instance variable name 'doctor'
        return doctor.getStatus() == com.example.cloud.care.var.doctor.Status.APPROVED;
    }
}