-- ============================================================================
-- Create transport_settings table for transport company preferences
-- ============================================================================
-- Migration: V20251127__create_transport_settings_table.sql
-- Description: Create transport_settings table for storing transport company preferences and configuration
-- Date: 2025-01-27
-- Issue: Missing transport_settings table for transport company configuration

-- ============================================================================
-- TRANSPORT_SETTINGS TABLE
-- ============================================================================
-- Purpose: Transport-specific settings for:
--   - Job matching preferences (search radius, minimum job value)
--   - Automation settings (auto-accept jobs, response time)
--   - Notification preferences (email, new jobs, quotations, payments, reviews)
-- Uses shared primary key pattern with transports table (transport_id)
-- ============================================================================

CREATE TABLE IF NOT EXISTS transport_settings (
    transport_id BIGINT NOT NULL COMMENT 'Shared primary key with transports table',
    
    -- Job matching preferences
    search_radius_km DECIMAL(5,2) NOT NULL DEFAULT 10.00 COMMENT 'Search radius for job matching in kilometers',
    min_job_value_vnd DECIMAL(12,0) NOT NULL DEFAULT 0 COMMENT 'Minimum job value in VND to accept',
    
    -- Automation settings
    auto_accept_jobs BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Automatically accept matching jobs',
    response_time_hours DECIMAL(4,1) DEFAULT 2.0 COMMENT 'Expected response time for quotations in hours',
    
    -- Notification preferences
    email_notifications BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Enable email notifications',
    new_job_alerts BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Notify about new job opportunities',
    quotation_updates BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Notify about quotation status changes',
    payment_notifications BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Notify about payment events',
    review_notifications BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Notify about new reviews',
    
    -- Timestamp
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
    
    PRIMARY KEY (transport_id),
    
    -- Constraints
    CONSTRAINT chk_transport_settings_search_radius
        CHECK (search_radius_km > 0 AND search_radius_km <= 999.99),
    
    CONSTRAINT chk_transport_settings_min_job_value
        CHECK (min_job_value_vnd >= 0),
    
    CONSTRAINT chk_transport_settings_response_time
        CHECK (response_time_hours IS NULL OR (response_time_hours >= 0.5 AND response_time_hours <= 999.9)),
    
    -- Foreign key
    CONSTRAINT fk_transport_settings_transport
        FOREIGN KEY (transport_id) REFERENCES transports(transport_id)
        ON DELETE CASCADE
        ON UPDATE NO ACTION
        
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Transport-specific settings for job matching, notifications, and preferences';

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
  AND TABLE_NAME = 'transport_settings';

-- ============================================================================
-- Display table structure
-- ============================================================================
DESCRIBE transport_settings;

