-- ============================================================================
-- Ensure users table has complete schema matching User entity
-- ============================================================================
-- Migration: V20251119__ensure_users_table_complete_schema.sql
-- Description: Add all missing columns to users table to match User entity
-- Date: 2025-01-18
-- Issue: Multiple missing columns in users table

SET @schema := DATABASE();

-- Add email column if missing
SET @sql := (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = @schema
              AND TABLE_NAME = 'users'
              AND COLUMN_NAME = 'email'
        ),
        'SELECT "Column email already exists" AS status',
        'ALTER TABLE users ADD COLUMN email VARCHAR(255) NOT NULL COMMENT ''User email address'''
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add password_hash column if missing
SET @sql := (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = @schema
              AND TABLE_NAME = 'users'
              AND COLUMN_NAME = 'password_hash'
        ),
        'SELECT "Column password_hash already exists" AS status',
        'ALTER TABLE users ADD COLUMN password_hash TEXT NOT NULL COMMENT ''Hashed password'''
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add role column if missing
SET @sql := (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = @schema
              AND TABLE_NAME = 'users'
              AND COLUMN_NAME = 'role'
        ),
        'SELECT "Column role already exists" AS status',
        'ALTER TABLE users ADD COLUMN role ENUM(''CUSTOMER'', ''TRANSPORT'', ''MANAGER'') NOT NULL COMMENT ''User role'''
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add is_active column if missing
SET @sql := (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = @schema
              AND TABLE_NAME = 'users'
              AND COLUMN_NAME = 'is_active'
        ),
        'SELECT "Column is_active already exists" AS status',
        'ALTER TABLE users ADD COLUMN is_active BOOLEAN DEFAULT TRUE COMMENT ''Account active status'''
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add is_verified column if missing
SET @sql := (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = @schema
              AND TABLE_NAME = 'users'
              AND COLUMN_NAME = 'is_verified'
        ),
        'SELECT "Column is_verified already exists" AS status',
        'ALTER TABLE users ADD COLUMN is_verified BOOLEAN DEFAULT FALSE COMMENT ''Email verification status'''
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add email_verified_at column if missing
SET @sql := (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = @schema
              AND TABLE_NAME = 'users'
              AND COLUMN_NAME = 'email_verified_at'
        ),
        'SELECT "Column email_verified_at already exists" AS status',
        'ALTER TABLE users ADD COLUMN email_verified_at DATETIME DEFAULT NULL COMMENT ''Email verification timestamp'''
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add last_password_change column if missing
SET @sql := (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = @schema
              AND TABLE_NAME = 'users'
              AND COLUMN_NAME = 'last_password_change'
        ),
        'SELECT "Column last_password_change already exists" AS status',
        'ALTER TABLE users ADD COLUMN last_password_change DATETIME DEFAULT NULL COMMENT ''Last password change timestamp'''
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add locked_until column if missing
SET @sql := (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = @schema
              AND TABLE_NAME = 'users'
              AND COLUMN_NAME = 'locked_until'
        ),
        'SELECT "Column locked_until already exists" AS status',
        'ALTER TABLE users ADD COLUMN locked_until DATETIME DEFAULT NULL COMMENT ''Account lock expiration'''
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add verification_token column if missing (deprecated but still in entity)
SET @sql := (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = @schema
              AND TABLE_NAME = 'users'
              AND COLUMN_NAME = 'verification_token'
        ),
        'SELECT "Column verification_token already exists" AS status',
        'ALTER TABLE users ADD COLUMN verification_token VARCHAR(255) DEFAULT NULL COMMENT ''DEPRECATED: Use user_tokens table'''
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add reset_password_token column if missing (deprecated but still in entity)
SET @sql := (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = @schema
              AND TABLE_NAME = 'users'
              AND COLUMN_NAME = 'reset_password_token'
        ),
        'SELECT "Column reset_password_token already exists" AS status',
        'ALTER TABLE users ADD COLUMN reset_password_token VARCHAR(255) DEFAULT NULL COMMENT ''DEPRECATED: Use user_tokens table'''
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add reset_password_expires column if missing (deprecated but still in entity)
SET @sql := (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = @schema
              AND TABLE_NAME = 'users'
              AND COLUMN_NAME = 'reset_password_expires'
        ),
        'SELECT "Column reset_password_expires already exists" AS status',
        'ALTER TABLE users ADD COLUMN reset_password_expires DATETIME DEFAULT NULL COMMENT ''DEPRECATED: Use user_tokens table'''
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add last_login column if missing
SET @sql := (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = @schema
              AND TABLE_NAME = 'users'
              AND COLUMN_NAME = 'last_login'
        ),
        'SELECT "Column last_login already exists" AS status',
        'ALTER TABLE users ADD COLUMN last_login DATETIME DEFAULT NULL COMMENT ''Last login timestamp'''
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Verify all columns exist
SELECT 
    COLUMN_NAME,
    COLUMN_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'users'
ORDER BY ORDINAL_POSITION;

