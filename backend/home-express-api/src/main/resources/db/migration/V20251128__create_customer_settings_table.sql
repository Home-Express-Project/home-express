-- ============================================================================
-- Create customer_settings table for customer preferences
-- ============================================================================
-- Migration: V20251128__create_customer_settings_table.sql
-- Description: Create customer_settings table for storing customer preferences and configuration
-- Date: 2025-01-28
-- Issue: Missing customer_settings table for customer configuration

-- ============================================================================
-- CUSTOMER_SETTINGS TABLE
-- ============================================================================
-- Purpose: Customer-specific settings for:
--   - Language and localization preferences
--   - Notification preferences (email, bookings, quotations, promotions)
--   - Privacy settings (profile visibility, contact information display)
-- Uses shared primary key pattern with customers table (customer_id)
-- ============================================================================

CREATE TABLE IF NOT EXISTS customer_settings (
    customer_id BIGINT NOT NULL COMMENT 'Shared primary key with customers table',
    
    -- Localization
    language VARCHAR(10) NOT NULL DEFAULT 'vi' COMMENT 'Preferred language (vi, en)',
    
    -- Notification preferences
    email_notifications BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Enable email notifications',
    booking_updates BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Notify about booking status changes',
    quotation_alerts BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Notify about new quotations',
    promotions BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Receive promotional emails',
    newsletter BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Subscribe to newsletter',
    
    -- Privacy settings
    profile_visibility ENUM('public', 'private') NOT NULL DEFAULT 'public' COMMENT 'Profile visibility setting',
    show_phone BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Show phone number on profile',
    show_email BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Show email address on profile',
    
    -- Timestamp
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
    
    PRIMARY KEY (customer_id),
    
    -- Constraints
    CONSTRAINT chk_customer_settings_language
        CHECK (language IN ('vi', 'en')),
    
    -- Foreign key
    CONSTRAINT fk_customer_settings_customer
        FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
        ON DELETE CASCADE
        ON UPDATE NO ACTION
        
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Customer notification and privacy preferences';

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
  AND TABLE_NAME = 'customer_settings';

-- ============================================================================
-- Display table structure
-- ============================================================================
DESCRIBE customer_settings;

