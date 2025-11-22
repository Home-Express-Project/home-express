-- ============================================================================
-- Create user_tokens table for secure token management
-- ============================================================================
-- Migration: V20251123__create_user_tokens_table.sql
-- Description: Create user_tokens table for email verification, password reset, etc.
-- Date: 2025-01-23
-- Issue: Missing user_tokens table for authentication workflows

-- ============================================================================
-- USER_TOKENS TABLE
-- ============================================================================
-- Purpose: Secure token storage for:
--   - Email verification (VERIFY_EMAIL)
--   - Password reset (RESET_PASSWORD)
--   - User invitations (INVITE)
--   - MFA recovery codes (MFA_RECOVERY)
-- Replaces deprecated verification_token and reset_password_token in users table
-- ============================================================================

CREATE TABLE IF NOT EXISTS user_tokens (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT 'User who owns this token',
    token_type ENUM('VERIFY_EMAIL', 'RESET_PASSWORD', 'INVITE', 'MFA_RECOVERY') NOT NULL COMMENT 'Type of token',
    token_hash VARCHAR(64) NOT NULL COMMENT 'SHA-256 hash of token - NEVER store plaintext',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'When token was created',
    expires_at DATETIME NOT NULL COMMENT 'When token expires',
    consumed_at DATETIME DEFAULT NULL COMMENT 'When token was used (one-time use)',
    metadata JSON DEFAULT NULL COMMENT 'Additional data (IP, user agent, etc.)',
    
    PRIMARY KEY (id),
    
    -- Unique constraint: one token hash per user per type
    UNIQUE KEY uk_user_tokens (user_id, token_type, token_hash),
    
    -- Indexes for performance
    KEY idx_user_tokens_lookup (user_id, token_type, expires_at) COMMENT 'Find valid tokens for a user',
    KEY idx_user_tokens_type_hash_expires (token_type, token_hash, expires_at) COMMENT 'Verify token by hash',
    KEY idx_user_tokens_cleanup (expires_at) COMMENT 'For cleanup jobs (delete expired tokens)',
    
    -- Constraints
    CONSTRAINT chk_user_tokens_expires_valid
        CHECK (expires_at > created_at),
    
    -- Foreign key
    CONSTRAINT fk_user_tokens_user
        FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON DELETE CASCADE
        ON UPDATE NO ACTION
        
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Secure token storage for email verification, password reset, etc.';

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
  AND TABLE_NAME = 'user_tokens';

-- ============================================================================
-- Display table structure
-- ============================================================================
DESCRIBE user_tokens;

