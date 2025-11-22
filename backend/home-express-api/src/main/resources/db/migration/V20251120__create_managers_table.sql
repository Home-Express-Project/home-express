-- ============================================================================
-- Create managers table for platform administrators
-- ============================================================================
-- Migration: V20251120__create_managers_table.sql
-- Description: Create managers table for storing platform admin/manager information
-- Date: 2025-01-20
-- Issue: Missing managers table required by admin_settings and other tables

-- ============================================================================
-- MANAGERS TABLE
-- ============================================================================
-- Purpose: Platform administrators and managers with:
--   - Shared primary key with users table (manager_id = user_id)
--   - Personal information (full name, phone)
--   - Employee identification (employee_id, department)
--   - JSON permissions array for granular access control
--   - Timestamps for audit trail
-- Uses @MapsId pattern with users table for 1-to-1 relationship
-- ============================================================================

CREATE TABLE IF NOT EXISTS managers (
    manager_id BIGINT NOT NULL COMMENT 'Shared primary key with users table',
    
    -- Personal information
    full_name VARCHAR(255) NOT NULL COMMENT 'Manager full name',
    phone VARCHAR(20) NOT NULL COMMENT 'Manager phone number (VN format: 0XXXXXXXXX)',
    
    -- Employee identification
    employee_id VARCHAR(50) DEFAULT NULL COMMENT 'Unique employee identifier',
    department VARCHAR(100) DEFAULT NULL COMMENT 'Department or division',
    
    -- Permissions
    permissions JSON DEFAULT NULL COMMENT 'Array of permission codes (e.g., ["USER_MANAGE", "TRANSPORT_APPROVE"])',
    
    -- Timestamps
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation timestamp',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
    
    PRIMARY KEY (manager_id),
    
    -- Unique constraints
    UNIQUE KEY uk_managers_employee (employee_id),
    
    -- Indexes
    KEY idx_managers_phone (phone),
    KEY idx_managers_department (department),
    
    -- Constraints
    CONSTRAINT chk_managers_phone
        CHECK (phone REGEXP '^0[1-9][0-9]{8}$'),
    
    -- Foreign key to users table
    CONSTRAINT fk_managers_users
        FOREIGN KEY (manager_id) REFERENCES users(user_id)
        ON DELETE CASCADE
        ON UPDATE NO ACTION
        
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Platform administrators and managers with role-based permissions';

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
  AND TABLE_NAME = 'managers';

-- ============================================================================
-- Display table structure
-- ============================================================================
DESCRIBE managers;

