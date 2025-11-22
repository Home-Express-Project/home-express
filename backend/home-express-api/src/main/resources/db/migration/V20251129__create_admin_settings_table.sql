-- ============================================================================
-- Create admin_settings table for manager/admin preferences
-- ============================================================================
-- Migration: V20251129__create_admin_settings_table.sql
-- Description: Create admin_settings table for storing manager/admin preferences and configuration
-- Date: 2025-01-29
-- Issue: Missing admin_settings table for platform admin configuration

-- ============================================================================
-- ADMIN_SETTINGS TABLE
-- ============================================================================
-- Purpose: Platform admin preferences and operational configuration for:
--   - Personal information (full name, phone)
--   - Notification preferences (email, system alerts, user registrations, etc.)
--   - Security settings (2FA, session timeout, login notifications)
--   - UI preferences (theme, date format, timezone)
--   - System settings (maintenance mode, auto backup)
--   - Email configuration (SMTP settings)
-- Uses shared primary key pattern with managers table (manager_id)
-- ============================================================================

CREATE TABLE IF NOT EXISTS admin_settings (
    manager_id BIGINT NOT NULL COMMENT 'Shared primary key with managers table',
    
    -- Personal information
    full_name VARCHAR(255) DEFAULT NULL COMMENT 'Manager full name',
    phone VARCHAR(20) DEFAULT NULL COMMENT 'Manager phone number',
    
    -- Notification preferences
    email_notifications BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Enable email notifications',
    system_alerts BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Receive system alerts',
    user_registrations BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Notify about new user registrations',
    transport_verifications BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Notify about transport verification requests',
    booking_alerts BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Notify about new bookings',
    review_moderation BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Notify about reviews requiring moderation',
    
    -- Security settings
    two_factor_enabled BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Enable two-factor authentication',
    session_timeout_minutes INT NOT NULL DEFAULT 30 COMMENT 'Session timeout in minutes',
    login_notifications BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Notify about login events',
    
    -- UI preferences
    theme ENUM('light', 'dark', 'system') NOT NULL DEFAULT 'light' COMMENT 'UI theme preference',
    date_format VARCHAR(20) NOT NULL DEFAULT 'DD/MM/YYYY' COMMENT 'Date format preference',
    timezone VARCHAR(100) NOT NULL DEFAULT 'Asia/Ho_Chi_Minh' COMMENT 'Timezone preference',
    
    -- System settings
    maintenance_mode BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Enable maintenance mode',
    auto_backup BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Enable automatic backups',
    backup_frequency ENUM('hourly', 'daily', 'weekly', 'monthly') NOT NULL DEFAULT 'daily' COMMENT 'Backup frequency',
    
    -- Email configuration
    email_provider ENUM('smtp', 'sendgrid', 'mailgun') NOT NULL DEFAULT 'smtp' COMMENT 'Email service provider',
    smtp_host VARCHAR(255) DEFAULT NULL COMMENT 'SMTP server hostname',
    smtp_port VARCHAR(10) DEFAULT NULL COMMENT 'SMTP server port',
    smtp_username VARCHAR(255) DEFAULT NULL COMMENT 'SMTP authentication username',
    smtp_password TEXT DEFAULT NULL COMMENT 'SMTP authentication password (encrypted)',
    
    -- Timestamp
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
    
    PRIMARY KEY (manager_id),
    
    -- Constraints
    CONSTRAINT chk_admin_settings_session_timeout
        CHECK (session_timeout_minutes >= 5 AND session_timeout_minutes <= 240),
    
    CONSTRAINT chk_admin_settings_phone
        CHECK (phone IS NULL OR phone REGEXP '^0[1-9][0-9]{8}$'),
    
    -- Foreign key
    CONSTRAINT fk_admin_settings_manager
        FOREIGN KEY (manager_id) REFERENCES managers(manager_id)
        ON DELETE CASCADE
        ON UPDATE NO ACTION
        
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Platform admin preferences and operational configuration';

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
  AND TABLE_NAME = 'admin_settings';

-- ============================================================================
-- Display table structure
-- ============================================================================
DESCRIBE admin_settings;

