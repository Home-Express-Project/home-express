-- ============================================================================
-- Create transport_list table for booking notification tracking
-- ============================================================================
-- Migration: V20251130__create_transport_list_table.sql
-- Description: Create transport_list table for managing transport notification lists for bookings
-- Date: 2025-01-30
-- Issue: Missing transport_list table for tracking which transports are notified about booking opportunities

-- ============================================================================
-- TRANSPORT_LIST TABLE
-- ============================================================================
-- Purpose: Track which transport companies were notified about each booking opportunity
--   - Notification tracking (when notified, method used)
--   - Response tracking (viewed, responded timestamps)
--   - Prevents duplicate notifications to same transport for same booking
-- ============================================================================

CREATE TABLE IF NOT EXISTS transport_list (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    booking_id BIGINT NOT NULL COMMENT 'Booking that transport was notified about',
    transport_id BIGINT NOT NULL COMMENT 'Transport company that was notified',
    
    -- Notification tracking
    notified_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'When the transport was notified',
    notification_method ENUM('EMAIL', 'SMS', 'PUSH') DEFAULT 'EMAIL' COMMENT 'Method used to notify transport',
    
    -- Response tracking
    has_viewed BOOLEAN DEFAULT FALSE COMMENT 'Whether transport has viewed the booking',
    viewed_at DATETIME DEFAULT NULL COMMENT 'When transport viewed the booking',
    has_responded BOOLEAN DEFAULT FALSE COMMENT 'Whether transport has responded (submitted quotation)',
    responded_at DATETIME DEFAULT NULL COMMENT 'When transport responded',
    
    -- Timestamp
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation timestamp',
    
    PRIMARY KEY (id),
    
    -- Unique constraint: one notification per transport per booking
    UNIQUE KEY uk_transport_list_booking_transport (booking_id, transport_id),
    
    -- Indexes for performance
    KEY idx_transport_list_booking (booking_id),
    KEY idx_transport_list_transport (transport_id),
    KEY idx_transport_list_notified (notified_at),
    KEY idx_transport_list_has_viewed (has_viewed),
    KEY idx_transport_list_has_responded (has_responded),
    
    -- Foreign keys
    CONSTRAINT fk_transport_list_booking
        FOREIGN KEY (booking_id) REFERENCES bookings(booking_id)
        ON DELETE CASCADE
        ON UPDATE NO ACTION,
    
    CONSTRAINT fk_transport_list_transport
        FOREIGN KEY (transport_id) REFERENCES transports(transport_id)
        ON DELETE CASCADE
        ON UPDATE NO ACTION
        
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Tracks which transports were notified about each booking opportunity';

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
  AND TABLE_NAME = 'transport_list';

-- ============================================================================
-- Display table structure
-- ============================================================================
DESCRIBE transport_list;

