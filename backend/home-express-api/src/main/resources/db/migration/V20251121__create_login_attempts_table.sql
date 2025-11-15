-- ============================================================================
-- Create login_attempts table for rate limiting and security monitoring
-- ============================================================================
-- Migration: V20251121__create_login_attempts_table.sql
-- Description: Create login_attempts table for tracking login attempts
-- Date: 2025-01-21
-- Issue: Missing login_attempts table causing login failures

-- ============================================================================
-- LOGIN_ATTEMPTS TABLE
-- ============================================================================
-- Purpose: Track all login attempts (success and failed) for:
--   - Rate limiting (prevent brute force attacks)
--   - Security monitoring and audit trail
--   - Account lockout after N failed attempts
--   - IP-based rate limiting
-- ============================================================================

CREATE TABLE IF NOT EXISTS login_attempts (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT DEFAULT NULL COMMENT 'NULL if user does not exist (wrong email)',
    email VARCHAR(255) NOT NULL COMMENT 'Always log email, even if user does not exist',
    ip_address VARCHAR(45) DEFAULT NULL COMMENT 'IPv4 or IPv6 address',
    user_agent TEXT DEFAULT NULL COMMENT 'Browser/client user agent string',
    success BOOLEAN NOT NULL COMMENT 'TRUE = successful login, FALSE = failed login',
    failure_reason TEXT DEFAULT NULL COMMENT 'e.g., invalid_password, account_locked, account_disabled',
    attempted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Timestamp of login attempt',
    
    PRIMARY KEY (id),
    
    -- Indexes for rate limiting queries
    KEY idx_login_attempts_email_time (email, attempted_at DESC) COMMENT 'For email-based rate limiting',
    KEY idx_login_attempts_ip_time (ip_address, attempted_at DESC) COMMENT 'For IP-based rate limiting',
    KEY idx_login_attempts_user_time (user_id, attempted_at DESC) COMMENT 'For user-based queries',
    KEY idx_login_attempts_cleanup (attempted_at) COMMENT 'For cleanup jobs (delete old records)',
    
    -- Foreign key constraint
    CONSTRAINT fk_login_attempts_user
        FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON DELETE CASCADE
        ON UPDATE NO ACTION
        
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Audit log of all login attempts for rate limiting and security analysis';

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
  AND TABLE_NAME = 'login_attempts';

-- ============================================================================
-- Display table structure
-- ============================================================================
DESCRIBE login_attempts;

