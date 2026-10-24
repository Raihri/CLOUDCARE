package com.example.cloud.care.service;
import com.example.cloud.care.model.User;
import com.example.cloud.care.repository.UserRepository;

import java.util.Collections;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;     
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        return userRepository.findByEmail(email)
                // Email OTP verification has been removed, so a row existing is
                // enough to authenticate. `enabled` is kept for compatibility
                // with older data but no longer blocks sign-in.
                .map(user -> new org.springframework.security.core.userdetails.User(
                        user.getEmail(),
                        user.getPassword(),
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_PATIENT"))
                ))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}