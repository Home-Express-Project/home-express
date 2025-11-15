-- ============================================================================
-- Create payments table for payment transaction records
-- ============================================================================
-- Migration: V20251131__create_payments_table.sql
-- Description: Create payments table for storing payment transaction records
-- Date: 2025-01-31
-- Issue: Missing payments table for payment processing

-- ============================================================================
-- PAYMENTS TABLE
-- ============================================================================
-- Purpose: Store all payment transactions for bookings
--   - Support multiple payment methods (CASH, BANK_TRANSFER, , , ZALOPAY)
--   - Track payment status (PENDING, PROCESSING, COMPLETED, FAILED, REFUNDED)
--   - Support refunds with parent_payment_id reference
--   - Idempotency support to prevent double charges
--   - Gateway integration tracking
-- ============================================================================

CREATE TABLE IF NOT EXISTS payments (
    payment_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    booking_id BIGINT NOT NULL COMMENT 'Booking this payment is for',
    
    -- Payment Details (Vietnamese market - Integer VND only)
    amount DECIMAL(12,0) NOT NULL COMMENT 'Amount in VND (integer for gateways)',
    payment_method ENUM('CASH', 'BANK_TRANSFER') NOT NULL COMMENT 'Payment method used',
    payment_type ENUM('DEPOSIT', 'REMAINING_PAYMENT', 'TIP', 'REFUND') NOT NULL COMMENT 'Type of payment',
    bank_code VARCHAR(10) DEFAULT NULL COMMENT 'Vietnamese bank code (for bank transfers)',

    -- Status
    status ENUM('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'REFUNDED') DEFAULT 'PENDING' COMMENT 'Payment status',
    
    -- Refund Support
    parent_payment_id BIGINT DEFAULT NULL COMMENT 'Original payment for refunds (NULL if not a refund)',
    refund_reason VARCHAR(255) DEFAULT NULL COMMENT 'Reason for refund',
    
    -- Failure Tracking
    failure_code VARCHAR(50) DEFAULT NULL COMMENT 'Error code from payment gateway',
    failure_message TEXT DEFAULT NULL COMMENT 'Error message from payment gateway',

    -- Transaction Info
    transaction_id VARCHAR(255) DEFAULT NULL COMMENT 'Internal transaction reference',

    -- Idempotency (Prevent double charges)
    idempotency_key VARCHAR(64) DEFAULT NULL COMMENT 'Client-provided idempotency key',

    -- Confirmation
    confirmed_by BIGINT DEFAULT NULL COMMENT 'User who confirmed the payment (if applicable)',
    confirmed_at DATETIME DEFAULT NULL COMMENT 'When payment was confirmed',

    -- Timestamps
    paid_at DATETIME DEFAULT NULL COMMENT 'When payment was completed',
    refunded_at DATETIME DEFAULT NULL COMMENT 'When payment was refunded',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation timestamp',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',

    PRIMARY KEY (payment_id),
    
    -- Unique constraints
    UNIQUE KEY uk_payments_transaction_id (transaction_id),
    UNIQUE KEY uk_payments_idempotency (idempotency_key),
    
    -- Indexes for performance
    KEY idx_payments_booking (booking_id),
    KEY idx_payments_status (status),
    KEY idx_payments_booking_created (booking_id, created_at DESC),
    KEY idx_payments_payment_method (payment_method),
    KEY idx_payments_payment_type (payment_type),
    KEY idx_payments_parent (parent_payment_id),
    
    -- Constraints
    CONSTRAINT chk_payments_amount_positive
        CHECK (amount > 0),

    -- Note: Refund logic (payment_type='REFUND' requires parent_payment_id) is enforced at application level
    -- Cannot use CHECK constraint on parent_payment_id due to MySQL limitation with foreign key referential actions

    -- Foreign keys
    CONSTRAINT fk_payments_booking
        FOREIGN KEY (booking_id) REFERENCES bookings(booking_id)
        ON DELETE CASCADE
        ON UPDATE NO ACTION,
    
    CONSTRAINT fk_payments_parent
        FOREIGN KEY (parent_payment_id) REFERENCES payments(payment_id)
        ON DELETE SET NULL
        ON UPDATE NO ACTION
        
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Payment transaction records for bookings with gateway integration';

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
  AND TABLE_NAME = 'payments';

-- ============================================================================
-- Display table structure
-- ============================================================================
DESCRIBE payments;

