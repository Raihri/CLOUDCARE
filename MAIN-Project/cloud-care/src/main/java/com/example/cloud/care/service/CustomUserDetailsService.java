package com.example.cloud.care.service;

import com.example.cloud.care.model.User;
import com.example.cloud.care.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Only allow login if user is enabled (verified)
        if (!user.isEnabled()) {
            System.out.println("User " + email + " attempted to log in but is not verified.");
            throw new UsernameNotFoundException("Email not verified");
        }

        // Grant the application role expected by the security configuration and
        // controllers.
        // Use the standard 'ROLE_' prefix so checks like hasRole("PATIENT") work
        // correctly.
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_PATIENT")));
    }
}