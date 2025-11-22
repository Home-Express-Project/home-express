-- ============================================================================
-- Create transport_payout_items table for payout item records
-- ============================================================================
-- Migration: V20251139__create_transport_payout_items_table.sql
-- Description: Create transport_payout_items table for storing individual payout items
-- Date: 2025-02-08
-- Issue: Missing transport_payout_items table for linking settlements to payouts

-- ============================================================================
-- TRANSPORT_PAYOUT_ITEMS TABLE
-- ============================================================================
-- Purpose: Individual line items in a payout batch
--   - Link specific booking settlements to their parent payout batch
--   - Each settlement can only be in one payout (enforced by unique constraint)
--   - Store amount snapshot for audit trail
--   - Business Logic: Each settlement can only be paid once
-- ============================================================================

CREATE TABLE IF NOT EXISTS transport_payout_items (
    payout_item_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    
    -- References
    payout_id BIGINT NOT NULL COMMENT 'Parent payout batch',
    settlement_id BIGINT NOT NULL COMMENT 'Settlement being paid in this batch',
    booking_id BIGINT NOT NULL COMMENT 'Booking reference for convenience',
    
    -- Amount Snapshot
    amount_vnd BIGINT NOT NULL COMMENT 'Amount from settlement (snapshot for audit)',
    
    -- Metadata
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation timestamp',
    
    PRIMARY KEY (payout_item_id),
    
    -- Unique constraint: each settlement can only be in one payout
    UNIQUE KEY uk_transport_payout_items_settlement (settlement_id),
    
    -- Indexes for performance
    KEY idx_transport_payout_items_payout (payout_id),
    KEY idx_transport_payout_items_booking (booking_id),
    KEY idx_transport_payout_items_created_at (created_at DESC),
    
    -- Constraints
    CONSTRAINT chk_transport_payout_items_amount_positive
        CHECK (amount_vnd > 0),
    
    -- Foreign keys
    CONSTRAINT fk_transport_payout_items_payout
        FOREIGN KEY (payout_id) REFERENCES transport_payouts(payout_id)
        ON DELETE CASCADE
        ON UPDATE NO ACTION,
    
    CONSTRAINT fk_transport_payout_items_settlement
        FOREIGN KEY (settlement_id) REFERENCES booking_settlements(settlement_id)
        ON DELETE NO ACTION
        ON UPDATE NO ACTION,
    
    CONSTRAINT fk_transport_payout_items_booking
        FOREIGN KEY (booking_id) REFERENCES bookings(booking_id)
        ON DELETE CASCADE
        ON UPDATE NO ACTION
        
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Links settlements to payout batches (one settlement per payout)';

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
  AND TABLE_NAME = 'transport_payout_items';

-- ============================================================================
-- Display table structure
-- ============================================================================
DESCRIBE transport_payout_items;

