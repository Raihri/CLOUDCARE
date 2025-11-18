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

    public final UserRepository userRepository;
    private final EmailService emailService;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, EmailService emailService,
            BCryptPasswordEncoder passwordEncoder) {
        if (userRepository == null) {
            throw new IllegalArgumentException("UserRepository cannot be null");
        }
        if (emailService == null) {
            throw new IllegalArgumentException("EmailService cannot be null");
        }
        if (passwordEncoder == null) {
            throw new IllegalArgumentException("BCryptPasswordEncoder cannot be null");
        }
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        logger.info("UserService initialized successfully with all dependencies");
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
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

        // Send code by email using the exact code we just saved so delivery matches
        // stored code
        // This now runs asynchronously in background thread
        emailService.sendVerificationEmail(user.getEmail(), verificationCode);
        logger.info("User registered successfully with email: {}", user.getEmail());
        logger.info("Verification email queued for sending to: {}", user.getEmail());
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

    // Dev helper: return raw user data for debugging verification (email, enabled,
    // verificationCode)
    public Optional<User> findByEmailOptional(String email) {
        if (email == null)
            return Optional.empty();
        return userRepository.findByEmail(email.trim());
    }

    // Generate a verification code for password reset and save it to the user
    @Transactional
    public void sendPasswordResetCode(String email) throws Exception {
        Optional<User> optUser = userRepository.findByEmail(email.trim());
        if (optUser.isEmpty()) {
            throw new Exception("Email not found");
        }

        User user = optUser.get();
        // Generate new code
        String verificationCode = String.format("%06d", new Random().nextInt(1000000));
        user.setVerificationCode(verificationCode);
        User saved = userRepository.save(user);
        logger.info("Verification code {} saved for user {} (ID: {})", verificationCode, email, saved.getId());

        // Send email with the exact code we just saved so delivery matches stored code
        // This now runs asynchronously in background thread
        emailService.sendVerificationEmail(email, verificationCode);
        logger.info("Password reset code {} queued for sending to {} successfully", verificationCode, email);
    }

    // Resend verification code for an existing unverified user
    @Transactional
    public void resendVerification(String email) throws Exception {
        Optional<User> optUser = userRepository.findByEmail(email.trim());
        if (optUser.isEmpty()) {
            throw new Exception("Email not found");
        }

        User user = optUser.get();
        if (user.isEnabled()) {
            throw new Exception("User already verified");
        }

        String verificationCode = String.format("%06d", new Random().nextInt(1000000));
        user.setVerificationCode(verificationCode);
        User saved = userRepository.save(user);
        logger.info("Resent verification code {} for user {} (ID: {})", verificationCode, email, saved.getId());
        emailService.sendVerificationEmail(email, verificationCode);
    }

    // Save user (encapsulated method instead of exposing repository directly)
    @Transactional
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    /**
     * Reset a user's password. Encodes the password, clears verification code,
     * enables the user and saves the record.
     * This encapsulates the logic previously in the controller.
     */
    @Transactional
    public void resetPassword(String email, String rawPassword) throws Exception {
        if (email == null || email.trim().isEmpty()) {
            throw new Exception("Email is required");
        }
        Optional<User> optUser = userRepository.findByEmail(email.trim());
        if (optUser.isEmpty()) {
            throw new Exception("User not found.");
        }

        User user = optUser.get();
        // Encode password using the injected encoder
        user.setPassword(passwordEncoder.encode(rawPassword));
        // Clear verification code and enable user
        user.setVerificationCode(null);
        user.setEnabled(true);

        userRepository.save(user);
        logger.info("Password reset performed for user: {}", email);
    }

}
