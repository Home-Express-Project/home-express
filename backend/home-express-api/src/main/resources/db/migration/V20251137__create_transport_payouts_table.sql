-- ============================================================================
-- Create transport_payouts table for payout batch records
-- ============================================================================
-- Migration: V20251138__create_transport_payouts_table.sql
-- Description: Create transport_payouts table for storing payout batch records
-- Date: 2025-02-07
-- Issue: Missing transport_payouts table for batch payment processing

-- ============================================================================
-- TRANSPORT_PAYOUTS TABLE
-- ============================================================================
-- Purpose: Batch payout transactions for transport companies
--   - Group multiple settlements into a single bank transfer
--   - Track payout status (PENDING, PROCESSING, COMPLETED, FAILED)
--   - Store bank account snapshot for audit trail
--   - Support for Vietnamese bank transfers
--   - Business Logic: total_amount = sum of all settlement items in batch
-- ============================================================================

CREATE TABLE IF NOT EXISTS transport_payouts (
    payout_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    
    -- Reference
    transport_id BIGINT NOT NULL COMMENT 'Transport company receiving payout',
    payout_number VARCHAR(50) NOT NULL COMMENT 'Unique payout identifier (e.g., PAYOUT-2025-W43-T123)',
    
    -- Amount
    total_amount_vnd BIGINT NOT NULL COMMENT 'Total payout amount (sum of all settlement items)',
    
    -- Item Count
    item_count INT NOT NULL DEFAULT 0 COMMENT 'Number of settlements in this payout batch',
    
    -- Payout Status
    status ENUM('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED') NOT NULL DEFAULT 'PENDING'
        COMMENT 'PENDING: not processed, PROCESSING: in progress, COMPLETED: done, FAILED: failed',
    
    -- Bank Account Snapshot (for audit trail - snapshot at payout time)
    bank_code VARCHAR(10) DEFAULT NULL COMMENT 'Vietnamese bank code (snapshot from transports.bank_code)',
    bank_account_number VARCHAR(19) DEFAULT NULL COMMENT 'Bank account number (snapshot from transports.bank_account_number)',
    bank_account_holder VARCHAR(255) DEFAULT NULL COMMENT 'Account holder name (snapshot from transports.bank_account_holder)',
    
    -- Timestamps
    processed_at DATETIME DEFAULT NULL COMMENT 'When payout processing started',
    completed_at DATETIME DEFAULT NULL COMMENT 'When payout was completed/failed',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation timestamp',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
    
    -- Failure Tracking & Transaction Reference
    failure_reason TEXT DEFAULT NULL COMMENT 'Why payout failed (invalid bank account, insufficient funds, etc.)',
    transaction_reference VARCHAR(255) DEFAULT NULL COMMENT 'Bank transaction reference number',
    
    -- Metadata
    notes TEXT DEFAULT NULL COMMENT 'Additional notes',
    
    PRIMARY KEY (payout_id),
    
    -- Unique constraint: payout number must be unique
    UNIQUE KEY uk_transport_payouts_payout_number (payout_number),
    
    -- Indexes for performance
    KEY idx_transport_payouts_transport_status (transport_id, status, created_at DESC),
    KEY idx_transport_payouts_status (status, processed_at DESC),
    KEY idx_transport_payouts_completed_at (completed_at DESC),
    KEY idx_transport_payouts_transport (transport_id),
    KEY idx_transport_payouts_created_at (created_at DESC),
    
    -- Constraints
    CONSTRAINT chk_transport_payouts_amount_positive
        CHECK (total_amount_vnd > 0),
    
    CONSTRAINT chk_transport_payouts_item_count
        CHECK (item_count >= 0),
    
    -- Foreign keys
    CONSTRAINT fk_transport_payouts_transport
        FOREIGN KEY (transport_id) REFERENCES transports(transport_id)
        ON DELETE NO ACTION
        ON UPDATE NO ACTION
        
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Batch payout transactions grouping multiple settlements for bank transfer';

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
  AND TABLE_NAME = 'transport_payouts';

-- ============================================================================
-- Display table structure
-- ============================================================================
DESCRIBE transport_payouts;

