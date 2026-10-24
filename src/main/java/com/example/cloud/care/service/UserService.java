package com.example.cloud.care.service;

import com.example.cloud.care.model.User;
import com.example.cloud.care.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        if (userRepository == null) {
            throw new IllegalArgumentException("UserRepository cannot be null");
        }
        if (passwordEncoder == null) {
            throw new IllegalArgumentException("BCryptPasswordEncoder cannot be null");
        }
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        logger.info("UserService initialized successfully with all dependencies");
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
     
    public Optional<User> authenticate(String email, String rawPassword) {

        // 1. Find user by email
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            return Optional.empty(); // no such email
        }

        User user = userOptional.get();

        // 2. Compare raw password with hashed password
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            return Optional.empty(); // password mismatch
        }

        // 3. User authenticated successfully
        return Optional.of(user);
    }

    public void registerUser(User user) throws Exception {
        logger.info("Starting registration for user: {}", user.getEmail());

        if (user.getName() == null || user.getEmail() == null || user.getPassword() == null) {
            System.out.println("Registration failed: Missing required fields.");
            throw new Exception("All fields are required.");
        }

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            System.out.println("Email already in use: " + user.getEmail());
            throw new Exception("Email already in use.");
        }

        // Encrypt password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Demo-ready registration: accounts are usable immediately. The old
        // email OTP step has been removed, so there is no verification code to
        // store or deliver.
        user.setVerificationCode(null);
        user.setEnabled(true);

        userRepository.save(user);
        logger.info("User registered successfully with email: {}", user.getEmail());
    }

    // Save user (encapsulated method instead of exposing repository directly)
    @Transactional
    public User saveUser(User user) {
        return userRepository.save(user);
    }
}
