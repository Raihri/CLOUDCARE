package com.example.cloud.care.service;

import com.example.cloud.care.model.User;
import com.example.cloud.care.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;
import java.util.Random;
import java.util.Scanner;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, EmailService emailService, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    public void registerUser(User user) throws Exception {
        logger.info("Starting registration for user: {}", user.getEmail());

        if (user.getEmail() == null || user.getUsername() == null || user.getPassword() == null) {
            System.out.println("Registration failed: Missing required fields.");
            throw new Exception("All fields are required.");
        }

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            System.out.println("Email already in use: " + user.getEmail());
            throw new Exception("Email already in use.");
        }

        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            System.out.println("Username already in use: " + user.getUsername());
            throw new Exception("Username already in use.");
        }

        // Encrypt password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Generate a random 6-digit numeric code
        String verificationCode = String.format("%06d", new Random().nextInt(1000000));
        user.setVerificationCode(verificationCode);
        user.setEnabled(false);

        // Save user first
        userRepository.save(user);

        // Send code by email
        String codeSent = emailService.sendVerificationEmail(user.getEmail());
        if (codeSent == null) {
            System.out.println("Failed to send verification email to: " + user.getEmail());
            throw new Exception("Failed to send verification email.");
        }

        // Replace the user’s stored code with the one actually sent
        user.setVerificationCode(codeSent);
        userRepository.save(user);

        logger.info("User registered successfully with email: {}", user.getEmail());
        System.out.println("stoppppppp");

        // Scanner in = new Scanner(System.in);
        // System.out.println("Enter OTP-> ");
        // String otp = in.nextLine();


        // System.out.println(verify(user.getEmail(), otp));

    }

    @Transactional
    public boolean verify(String email, String code) {
        System.out.println("Hello from VERIFY method");
        if (email == null || code == null) {
            logger.warn("Verification attempt with null email or code. email={}, codePresent={}", email, code != null);
            return false;
        }

        Optional<User> optionalUser = userRepository.findByEmail(email.trim());
        if (optionalUser.isEmpty()) {
            logger.warn("No user found for email during verification: {}", email);
            return false;
        }

        User user = optionalUser.get();
        String expected = user.getVerificationCode();
        logger.info("Verification attempt for {} - provided: '{}' expected: '{}'", email, code, expected);

        if (expected == null) {
            logger.warn("User {} has no verification code stored (maybe already verified)", email);
            return false;
        }

        // Trim and compare to avoid whitespace mismatch
        if (code.trim().equals(expected.trim())) {
            user.setEnabled(true);
            user.setVerificationCode(null);
            userRepository.save(user);
            logger.info("User {} has been enabled (verified)", email);
            return true;
        } else {
            logger.warn("Verification code mismatch for {}: provided='{}' expected='{}'", email, code, expected);
            return false;
        }
    }

    // Dev helper: return raw user data for debugging verification (email, enabled, verificationCode)
    public Optional<User> findByEmailOptional(String email) {
        if (email == null) return Optional.empty();
        return userRepository.findByEmail(email.trim());
    }
}