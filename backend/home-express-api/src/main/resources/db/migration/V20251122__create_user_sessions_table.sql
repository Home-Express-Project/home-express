-- ============================================================================
-- Create user_sessions table for JWT refresh token management
-- ============================================================================
-- Migration: V20251122__create_user_sessions_table.sql
-- Description: Create user_sessions table for managing JWT refresh tokens
-- Date: 2025-01-22
-- Issue: Missing user_sessions table causing login failures

-- ============================================================================
-- USER_SESSIONS TABLE
-- ============================================================================
-- Purpose: Manage JWT refresh tokens with:
--   - Secure storage (SHA-256 hash, not plaintext)
--   - Token rotation support
--   - Revocation support (logout, security breach)
--   - Session tracking (IP, user agent, device)
--   - Automatic cleanup of expired sessions
-- ============================================================================

CREATE TABLE IF NOT EXISTS user_sessions (
    session_id CHAR(36) NOT NULL DEFAULT (UUID()) COMMENT 'UUID session identifier',
    user_id BIGINT NOT NULL COMMENT 'User who owns this session',
    refresh_token_hash VARCHAR(64) NOT NULL COMMENT 'SHA-256 hash of refresh token (64 hex chars)',
    
    -- Session lifecycle
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'When session was created',
    last_seen_at DATETIME DEFAULT NULL COMMENT 'Last time refresh token was used',
    expires_at DATETIME NOT NULL COMMENT 'When session expires (typically 7 days)',
    
    -- Revocation support
    revoked_at DATETIME DEFAULT NULL COMMENT 'When session was revoked (logout)',
    revoked_reason TEXT DEFAULT NULL COMMENT 'e.g., user_logout, security_breach, admin_action',
    
    -- Security tracking
    ip_address VARCHAR(45) DEFAULT NULL COMMENT 'IPv4 or IPv6 address',
    user_agent TEXT DEFAULT NULL COMMENT 'Browser/client user agent string',
    device_id VARCHAR(255) DEFAULT NULL COMMENT 'Client-provided device identifier',
    
    PRIMARY KEY (session_id),
    
    -- Indexes for performance
    KEY idx_user_sessions_active (user_id, expires_at) COMMENT 'Find active sessions for a user',
    KEY idx_user_sessions_refresh_token (refresh_token_hash) COMMENT 'Lookup by refresh token hash',
    KEY idx_user_sessions_cleanup (expires_at) COMMENT 'For cleanup jobs (delete expired sessions)',
    
    -- Constraints
    CONSTRAINT chk_user_sessions_expires_valid
        CHECK (expires_at > created_at),
    
    -- Foreign key
    CONSTRAINT fk_user_sessions_user
        FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON DELETE CASCADE
        ON UPDATE NO ACTION
        
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Manages refresh tokens for JWT authentication with rotation support';

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
  AND TABLE_NAME = 'user_sessions';

-- ============================================================================
-- Display table structure
-- ============================================================================
DESCRIBE user_sessions;

