package com.example.cloud.care.service;

import com.example.cloud.care.model.User;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PasswordResetService is no longer actively used.
 * Password reset flow is now handled entirely in AuthController and UserService.
 * 
 * Kept for backward compatibility; can be removed in future refactoring.
 */
@Service
public class PasswordResetService {
    private static final Logger logger = LoggerFactory.getLogger(PasswordResetService.class);

    public PasswordResetService() {
        logger.warn("PasswordResetService is deprecated; use AuthController.resetPassword() instead");
    }
}