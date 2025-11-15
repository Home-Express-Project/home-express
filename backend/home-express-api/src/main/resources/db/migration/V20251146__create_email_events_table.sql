-- ============================================================================
-- Create email_events table for email lifecycle events
-- ============================================================================
-- Migration: V20251146__create_email_events_table.sql
-- Description: Create email_events table for tracking email lifecycle events
-- Date: 2025-02-15
-- Issue: Missing email_events table for email event tracking

-- ============================================================================
-- EMAIL_EVENTS TABLE
-- ============================================================================
-- Purpose: Append-only timeline of email lifecycle events
--   - Track all events from email service providers (sent, delivered, opened, clicked, bounced, complained, failed)
--   - Store webhook payloads for debugging
--   - Support multiple events per email (e.g., multiple opens, multiple clicks)
--   - Immutable audit trail
-- ============================================================================

CREATE TABLE IF NOT EXISTS email_events (
    event_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    
    -- References
    log_id BIGINT NOT NULL COMMENT 'Email log this event belongs to',
    
    -- Event Details
    event_type ENUM('QUEUED', 'SENT', 'DELIVERED', 'OPENED', 'CLICKED', 'BOUNCED', 'COMPLAINED', 'FAILED') NOT NULL COMMENT 'Type of email event',
    event_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'When event occurred',
    
    -- Webhook Data
    payload JSON DEFAULT NULL COMMENT 'Webhook payload or additional data',
    
    -- Metadata
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation timestamp',
    
    PRIMARY KEY (event_id),
    
    -- Indexes for performance
    KEY idx_email_events_log (log_id, event_time DESC),
    KEY idx_email_events_type_time (event_type, event_time DESC),
    KEY idx_email_events_created (created_at DESC),
    
    -- Foreign keys
    CONSTRAINT fk_email_events_log
        FOREIGN KEY (log_id) REFERENCES email_logs(log_id)
        ON DELETE CASCADE
        ON UPDATE NO ACTION
        
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Append-only timeline of email lifecycle events';

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
  AND TABLE_NAME = 'email_events';

-- ============================================================================
-- Display table structure
-- ============================================================================
DESCRIBE email_events;

