-- ============================================================================
-- Create otp_codes table for OTP verification
-- ============================================================================
-- Migration: V20251125__create_otp_codes_table.sql
-- Description: Create otp_codes table for OTP-based verification
-- Date: 2025-01-25
-- Issue: Missing otp_codes table for 2FA and verification workflows

-- ============================================================================
-- OTP_CODES TABLE
-- ============================================================================
-- Purpose: Store OTP codes for:
--   - Email verification
--   - Two-factor authentication (2FA)
--   - Phone number verification
--   - Additional security verification
-- Note: Legacy table - consider migrating to user_tokens for consistency
-- ============================================================================

CREATE TABLE IF NOT EXISTS otp_codes (
    otp_id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL COMMENT 'Email address for OTP delivery',
    code VARCHAR(6) NOT NULL COMMENT '6-digit OTP code',
    expires_at DATETIME NOT NULL COMMENT 'When OTP expires (typically 5-10 minutes)',
    is_used BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Whether OTP has been used',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'When OTP was created',
    
    PRIMARY KEY (otp_id),
    
    -- Indexes for performance
    KEY idx_otp_email (email) COMMENT 'Find OTPs by email',
    KEY idx_otp_expires (expires_at) COMMENT 'For cleanup jobs (delete expired OTPs)',
    
    -- Index for verification lookup
    KEY idx_otp_email_code_used (email, code, is_used) COMMENT 'Verify OTP code'
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='OTP codes for email verification (legacy - consider migrating to user_tokens)';

-- ============================================================================
-- Verify table was created
-- ============================================================================
SELECT 
    TABLE_NAME,
    TABLE_ROWS,
    CREATE_TIME,
    TABLE_COMMENT
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'otp_codes';

-- ============================================================================
-- Display table structure
-- ============================================================================
DESCRIBE otp_codes;

