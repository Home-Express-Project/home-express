-- ============================================================================
-- Create outbox_messages table for transactional outbox pattern
-- ============================================================================
-- Migration: V20251126__create_outbox_messages_table.sql
-- Description: Create outbox_messages table for reliable event publishing
-- Date: 2025-01-26
-- Issue: Missing outbox_messages table for event-driven architecture

-- ============================================================================
-- OUTBOX_MESSAGES TABLE
-- ============================================================================
-- Purpose: Transactional outbox pattern for reliable event publishing
--   - Ensures events are published reliably (at-least-once delivery)
--   - Supports retry logic for failed events
--   - Enables event sourcing and CQRS patterns
--   - Decouples event production from event consumption
-- ============================================================================

CREATE TABLE IF NOT EXISTS outbox_messages (
    id BIGINT NOT NULL AUTO_INCREMENT,
    aggregate_type VARCHAR(100) NOT NULL COMMENT 'Entity type (e.g., BOOKING, PAYMENT, REVIEW)',
    aggregate_id VARCHAR(100) NOT NULL COMMENT 'Entity ID',
    event_type VARCHAR(100) NOT NULL COMMENT 'Event name (e.g., BookingCreated, PaymentCompleted)',
    payload JSON NOT NULL COMMENT 'Event data as JSON',
    status ENUM('PENDING', 'PROCESSING', 'SENT', 'FAILED') NOT NULL DEFAULT 'PENDING' COMMENT 'Processing status',
    retry_count INT NOT NULL DEFAULT 0 COMMENT 'Number of retry attempts',
    max_retries INT NOT NULL DEFAULT 5 COMMENT 'Maximum retry attempts before giving up',
    last_error TEXT DEFAULT NULL COMMENT 'Last error message if failed',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'When event was created',
    processed_at DATETIME DEFAULT NULL COMMENT 'When event was successfully processed',
    next_retry_at DATETIME DEFAULT NULL COMMENT 'When to retry next (for exponential backoff)',
    
    PRIMARY KEY (id),
    
    -- Indexes for processing
    KEY idx_outbox_status_created (status, created_at) COMMENT 'Find pending events to process',
    KEY idx_outbox_next_retry (next_retry_at) COMMENT 'Find events ready for retry',
    KEY idx_outbox_aggregate (aggregate_type, aggregate_id) COMMENT 'Find events by aggregate',
    KEY idx_outbox_event_type (event_type, created_at DESC) COMMENT 'Find events by type'
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Transactional outbox for reliable event publishing';

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
  AND TABLE_NAME = 'outbox_messages';

-- ============================================================================
-- Display table structure
-- ============================================================================
DESCRIBE outbox_messages;

