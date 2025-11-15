-- ============================================================================
-- Create users table - foundational table for authentication and authorization
-- ============================================================================
-- Migration: V20251101__create_users_table.sql
-- Description: Create the users table that is referenced by many other tables
-- Date: 2025-01-01
-- Note: This must run before V20251102 migrations that reference users table

CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE COMMENT 'User email address',
    password_hash TEXT NOT NULL COMMENT 'Hashed password',
    role ENUM('CUSTOMER', 'TRANSPORT', 'MANAGER') NOT NULL COMMENT 'User role',
    is_active BOOLEAN DEFAULT TRUE COMMENT 'Account active status',
    is_verified BOOLEAN DEFAULT FALSE COMMENT 'Email verification status',
    email_verified_at DATETIME DEFAULT NULL COMMENT 'Email verification timestamp',
    last_password_change DATETIME DEFAULT NULL COMMENT 'Last password change timestamp',
    locked_until DATETIME DEFAULT NULL COMMENT 'Account lock expiration',
    verification_token VARCHAR(255) DEFAULT NULL COMMENT 'DEPRECATED: Use user_tokens table',
    reset_password_token VARCHAR(255) DEFAULT NULL COMMENT 'DEPRECATED: Use user_tokens table',
    reset_password_expires DATETIME DEFAULT NULL COMMENT 'DEPRECATED: Use user_tokens table',
    last_login DATETIME DEFAULT NULL COMMENT 'Last login timestamp',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Timestamp when user was created',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Timestamp when user was last updated',
    INDEX idx_users_email (email),
    INDEX idx_users_role (role),
    INDEX idx_users_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Core users table for authentication and authorization';

