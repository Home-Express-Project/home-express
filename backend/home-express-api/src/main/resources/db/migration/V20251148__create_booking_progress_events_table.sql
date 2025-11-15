-- ============================================================================
-- Create booking_progress_events table for booking lifecycle tracking
-- ============================================================================
-- Migration: V20251148__create_booking_progress_events_table.sql
-- Description: Create booking_progress_events table for tracking booking progress events
-- Date: 2025-02-17
-- Issue: Missing booking_progress_events table for granular progress tracking

-- ============================================================================
-- BOOKING_PROGRESS_EVENTS TABLE
-- ============================================================================
-- Purpose: Track detailed booking lifecycle events and status changes
--   - Granular progress sub-steps (EN_ROUTE, LOADING, IN_TRANSIT, UNLOADING, COMPLETED, CANCELLED)
--   - GPS location tracking for each event
--   - Notes for additional context
--   - Audit trail for real-time updates
--   - Support for live tracking features
-- ============================================================================

CREATE TABLE IF NOT EXISTS booking_progress_events (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    
    -- References
    booking_id BIGINT NOT NULL COMMENT 'Booking this event belongs to',
    
    -- Progress Step
    step ENUM('EN_ROUTE', 'LOADING', 'IN_TRANSIT', 'UNLOADING', 'COMPLETED', 'CANCELLED') NOT NULL COMMENT 'Progress step',
    
    -- Event Details
    note TEXT DEFAULT NULL COMMENT 'Additional notes about this progress step',
    
    -- GPS Location
    gps_lat DECIMAL(10,8) DEFAULT NULL COMMENT 'GPS latitude',
    gps_lng DECIMAL(11,8) DEFAULT NULL COMMENT 'GPS longitude',
    
    -- Audit
    created_by BIGINT DEFAULT NULL COMMENT 'User who created this event (usually transport)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'When event was created',
    
    PRIMARY KEY (id),
    
    -- Indexes for performance
    KEY idx_progress_booking (booking_id, created_at DESC),
    KEY idx_progress_step (step, created_at DESC),
    KEY idx_progress_created_by (created_by, created_at DESC),
    KEY idx_progress_created (created_at DESC),
    
    -- Foreign keys
    CONSTRAINT fk_progress_booking
        FOREIGN KEY (booking_id) REFERENCES bookings(booking_id)
        ON DELETE CASCADE
        ON UPDATE NO ACTION,
    
    CONSTRAINT fk_progress_created_by
        FOREIGN KEY (created_by) REFERENCES users(user_id)
        ON DELETE SET NULL
        ON UPDATE NO ACTION
        
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Tracks granular progress sub-steps for active jobs';

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
  AND TABLE_NAME = 'booking_progress_events';

-- ============================================================================
-- Display table structure
-- ============================================================================
DESCRIBE booking_progress_events;

