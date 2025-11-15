-- ============================================================================
-- Create booking_settlements table for settlement records
-- ============================================================================
-- Migration: V20251137__create_booking_settlements_table.sql
-- Description: Create booking_settlements table for storing settlement records
-- Date: 2025-02-06
-- Issue: Missing booking_settlements table for financial settlement tracking

-- ============================================================================
-- BOOKING_SETTLEMENTS TABLE
-- ============================================================================
-- Purpose: Track financial breakdown of each completed booking
--   - Money collected from customer
--   - Gateway fees deducted
--   - Platform commission calculated
--   - Net amount payable to transport
--   - Settlement status (PENDING, READY, ON_HOLD, PAID, CANCELLED)
--   - Business Logic: net_to_transport = total_collected - gateway_fee - platform_fee + adjustment
-- ============================================================================

CREATE TABLE IF NOT EXISTS booking_settlements (
    settlement_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    
    -- References
    booking_id BIGINT NOT NULL COMMENT 'Booking this settlement is for',
    transport_id BIGINT NOT NULL COMMENT 'Transport company to be paid',
    
    -- Money Breakdown (All amounts in VND - BIGINT for integer values)
    agreed_price_vnd BIGINT NOT NULL COMMENT 'Contract agreed price (from contracts.agreed_price_vnd)',
    deposit_paid_vnd BIGINT NOT NULL DEFAULT 0 COMMENT 'Amount collected from 30% deposit payments',
    remaining_paid_vnd BIGINT NOT NULL DEFAULT 0 COMMENT 'Amount collected from 70% remaining payments',
    tip_vnd BIGINT NOT NULL DEFAULT 0 COMMENT 'Tips paid by customer (optional)',
    total_collected_vnd BIGINT NOT NULL DEFAULT 0 COMMENT 'Total collected from customer (deposit + remaining + tip)',
    gateway_fee_vnd BIGINT NOT NULL DEFAULT 0 COMMENT 'Payment gateway fees (, , ZaloPay)',
    commission_rate_bps INT NOT NULL DEFAULT 0 COMMENT 'Applied commission rate in basis points (for audit)',
    platform_fee_vnd BIGINT NOT NULL DEFAULT 0 COMMENT 'Platform commission (calculated from commission_rules)',
    adjustment_vnd BIGINT NOT NULL DEFAULT 0 COMMENT 'Manual adjustments (can be negative for deductions)',
    net_to_transport_vnd BIGINT GENERATED ALWAYS AS (
        (total_collected_vnd - gateway_fee_vnd - platform_fee_vnd + adjustment_vnd)
    ) STORED COMMENT 'Net amount payable to transport (calculated)',
    
    -- Collection Mode
    collection_mode ENUM('ALL_ONLINE', 'PARTIAL_ONLINE', 'CASH_ON_DELIVERY', 'MIXED', 'ALL_CASH') NOT NULL DEFAULT 'ALL_ONLINE'
        COMMENT 'Payment collection method',
    
    -- Settlement Status
    status ENUM('PENDING', 'READY', 'IN_PAYOUT', 'ON_HOLD', 'PAID', 'CANCELLED') NOT NULL DEFAULT 'PENDING'
        COMMENT 'PENDING: not completed, READY: ready for payout, IN_PAYOUT: batched for payout, ON_HOLD: held, PAID: paid, CANCELLED: cancelled',
    on_hold_reason TEXT DEFAULT NULL COMMENT 'Reason for holding (incident, dispute, verification)',
    
    -- Payout Link
    payout_id BIGINT DEFAULT NULL COMMENT 'Link to payout batch when PAID',
    
    -- Timestamps
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'When booking completed and settlement created',
    ready_at DATETIME DEFAULT NULL COMMENT 'When settlement became READY for payout',
    paid_at DATETIME DEFAULT NULL COMMENT 'When settlement was included in a payout batch',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
    
    -- Metadata
    notes TEXT DEFAULT NULL COMMENT 'Additional notes',
    metadata JSON DEFAULT NULL COMMENT 'Additional tracking (commission_rule_id, payment_ids, etc.)',
    
    PRIMARY KEY (settlement_id),
    
    -- Unique constraint: one settlement per booking
    UNIQUE KEY uk_booking_settlements_booking (booking_id),
    
    -- Indexes for performance
    KEY idx_booking_settlements_transport (transport_id),
    KEY idx_booking_settlements_status (status),
    KEY idx_booking_settlements_transport_status (transport_id, status, created_at DESC),
    KEY idx_booking_settlements_ready_at (ready_at),
    KEY idx_booking_settlements_payout (payout_id),
    
    -- Constraints
    CONSTRAINT chk_booking_settlements_amounts_nonneg
        CHECK (
            agreed_price_vnd >= 0 AND
            deposit_paid_vnd >= 0 AND
            remaining_paid_vnd >= 0 AND
            tip_vnd >= 0 AND
            total_collected_vnd >= 0 AND
            gateway_fee_vnd >= 0 AND
            platform_fee_vnd >= 0
        ),
    
    CONSTRAINT chk_booking_settlements_commission_rate
        CHECK (commission_rate_bps >= 0 AND commission_rate_bps <= 10000),
    
    -- Foreign keys
    CONSTRAINT fk_booking_settlements_booking
        FOREIGN KEY (booking_id) REFERENCES bookings(booking_id)
        ON DELETE CASCADE
        ON UPDATE NO ACTION,
    
    CONSTRAINT fk_booking_settlements_transport
        FOREIGN KEY (transport_id) REFERENCES transports(transport_id)
        ON DELETE NO ACTION
        ON UPDATE NO ACTION,
    
    CONSTRAINT fk_booking_settlements_payout
        FOREIGN KEY (payout_id) REFERENCES transport_payouts(payout_id)
        ON DELETE SET NULL
        ON UPDATE NO ACTION,

    CONSTRAINT chk_booking_settlements_status_dates
        CHECK (
            (status NOT IN ('READY', 'IN_PAYOUT') OR ready_at IS NOT NULL) AND
            (status != 'PAID' OR paid_at IS NOT NULL) AND
            (status != 'ON_HOLD' OR on_hold_reason IS NOT NULL)
        )
        
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Settlement records tracking financial breakdown of completed bookings';

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
  AND TABLE_NAME = 'booking_settlements';

-- ============================================================================
-- Display table structure
-- ============================================================================
DESCRIBE booking_settlements;
