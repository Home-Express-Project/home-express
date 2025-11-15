package com.homeexpress.home_express_api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for security-related settings
 * Centralizes magic numbers and makes them configurable
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "security")
public class SecurityConfigProperties {
    
    /**
     * Login attempt rate limiting configuration
     */
    private LoginAttemptConfig loginAttempt = new LoginAttemptConfig();
    
    /**
     * OTP configuration
     */
    private OtpConfig otp = new OtpConfig();
    
    /**
     * Session cleanup configuration
     */
    private SessionCleanupConfig sessionCleanup = new SessionCleanupConfig();
    
    @Data
    public static class LoginAttemptConfig {
        /**
         * Maximum failed login attempts before blocking
         */
        private int maxFailedAttempts = 5;
        
        /**
         * Time window in minutes for counting failed attempts
         */
        private int rateLimitWindowMinutes = 15;
        
        /**
         * Duration in minutes to lock account after max failed attempts
         */
        private int lockoutDurationMinutes = 30;
        
        /**
         * Number of days to keep old login attempt records
         */
        private int cleanupDays = 30;
        
        /**
         * Cron expression for cleanup job (default: 2am daily)
         */
        private String cleanupCron = "0 0 2 * * ?";
    }
    
    @Data
    public static class OtpConfig {
        /**
         * OTP code length (number of digits)
         */
        private int codeLength = 6;
        
        /**
         * OTP expiration time in minutes
         */
        private int expirationMinutes = 5;
        
        /**
         * Maximum OTP requests per user per hour
         */
        private int maxRequestsPerHour = 3;
    }
    
    @Data
    public static class SessionCleanupConfig {
        /**
         * Cron expression for session cleanup job (default: 3am daily)
         */
        private String cleanupCron = "0 0 3 * * ?";
        
        /**
         * Number of days to keep expired sessions before deletion
         */
        private int retentionDays = 30;
    }
}

