-- ============================================================================
-- Add created_at and updated_at columns to users table
-- ============================================================================
-- Migration: V20251118__add_timestamps_to_users_table.sql
-- Description: Add missing timestamp columns to users table to match User entity
-- Date: 2025-01-18
-- Issue: SQL Error 1054 - Unknown column 'u1_0.created_at' in 'field list'

-- Check if created_at column exists, if not add it
SET @schema := DATABASE();

SET @sql := (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = @schema
              AND TABLE_NAME = 'users'
              AND COLUMN_NAME = 'created_at'
        ),
        'SELECT "Column created_at already exists in users table" AS status',
        'ALTER TABLE users ADD COLUMN created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT ''Timestamp when user was created'''
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Check if updated_at column exists, if not add it
SET @sql := (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = @schema
              AND TABLE_NAME = 'users'
              AND COLUMN_NAME = 'updated_at'
        ),
        'SELECT "Column updated_at already exists in users table" AS status',
        'ALTER TABLE users ADD COLUMN updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''Timestamp when user was last updated'''
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Verify the columns were added
SELECT 
    COLUMN_NAME,
    COLUMN_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT,
    EXTRA
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'users'
  AND COLUMN_NAME IN ('created_at', 'updated_at')
ORDER BY ORDINAL_POSITION;

