-- ============================================================================
-- Create price_history table for tracking historical price calculations
-- ============================================================================
-- Migration: V20251134__create_price_history_table.sql
-- Description: Create price_history table for tracking historical price changes and calculations
-- Date: 2025-02-03
-- Issue: Missing price_history table for price calculation audit trail

-- ============================================================================
-- PRICE_HISTORY TABLE
-- ============================================================================
-- Purpose: Audit trail for all price calculations
--   - Complete breakdown of price components
--   - Snapshot of pricing rules used
--   - Track which multipliers were applied
--   - Support for pricing analytics and debugging
-- ============================================================================

CREATE TABLE IF NOT EXISTS price_history (
    price_history_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    booking_id BIGINT DEFAULT NULL COMMENT 'Booking this calculation is for (NULL for estimates)',
    transport_id BIGINT NOT NULL COMMENT 'Transport company whose pricing was used',
    vehicle_id BIGINT DEFAULT NULL COMMENT 'Specific vehicle used',
    vehicle_type ENUM('motorcycle', 'van', 'truck_small', 'truck_large', 'other') DEFAULT NULL COMMENT 'Vehicle type',
    pricing_id BIGINT DEFAULT NULL COMMENT 'Vehicle pricing rule used',
    category_pricing_ids JSON DEFAULT NULL COMMENT 'Array of category_pricing IDs used in calculation',
    
    -- Monetary breakdown (VND integer)
    base_price_vnd DECIMAL(12,0) NOT NULL COMMENT 'Base price component (VND)',
    distance_price_vnd DECIMAL(12,0) NOT NULL COMMENT 'Distance-based price component (VND)',
    category_price_vnd DECIMAL(12,0) NOT NULL COMMENT 'Category/item-based price component (VND)',
    additional_fees_vnd DECIMAL(12,0) NOT NULL DEFAULT 0 COMMENT 'Additional fees (elevator, floor, etc.) (VND)',
    subtotal_vnd DECIMAL(12,0) NOT NULL COMMENT 'Subtotal before multipliers (VND)',
    
    -- Multipliers applied
    peak_applied BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Whether peak hour multiplier was applied',
    weekend_applied BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Whether weekend multiplier was applied',
    multiplier_effect DECIMAL(8,2) NOT NULL DEFAULT 1.00 COMMENT 'Combined multiplier effect',
    
    -- Final total
    total_vnd DECIMAL(12,0) NOT NULL COMMENT 'Final total price (VND)',
    currency CHAR(3) NOT NULL DEFAULT 'VND' COMMENT 'Currency code (always VND)',
    
    -- Audit metadata
    rule_snapshot JSON DEFAULT NULL COMMENT 'Snapshot of pricing configuration used (vehicle_pricing + category_pricing)',
    engine_version VARCHAR(20) DEFAULT NULL COMMENT 'Pricing engine version',
    calculated_by VARCHAR(100) DEFAULT 'pricing-service' COMMENT 'Service that performed calculation',
    calculated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'When calculation was performed',
    
    PRIMARY KEY (price_history_id),
    
    -- Indexes for performance
    KEY idx_ph_booking (booking_id, calculated_at DESC),
    KEY idx_ph_transport_time (transport_id, calculated_at DESC),
    KEY idx_ph_vehicle_pricing (pricing_id),
    KEY idx_ph_vehicle (vehicle_id),
    KEY idx_ph_calculated_at (calculated_at),
    
    -- Constraints
    CONSTRAINT chk_ph_currency_vnd
        CHECK (currency = 'VND'),
    
    CONSTRAINT chk_ph_amounts_nonneg
        CHECK (
            base_price_vnd >= 0 AND
            distance_price_vnd >= 0 AND
            category_price_vnd >= 0 AND
            additional_fees_vnd >= 0 AND
            subtotal_vnd >= 0 AND
            total_vnd >= 0
        ),
    
    CONSTRAINT chk_ph_multiplier_valid
        CHECK (multiplier_effect >= 1.00),
    
    -- Foreign keys
    CONSTRAINT fk_ph_booking
        FOREIGN KEY (booking_id) REFERENCES bookings(booking_id)
        ON DELETE SET NULL
        ON UPDATE NO ACTION,
    
    CONSTRAINT fk_ph_transport
        FOREIGN KEY (transport_id) REFERENCES transports(transport_id)
        ON DELETE NO ACTION
        ON UPDATE NO ACTION,
    
    CONSTRAINT fk_ph_vehicle
        FOREIGN KEY (vehicle_id) REFERENCES vehicles(vehicle_id)
        ON DELETE SET NULL
        ON UPDATE NO ACTION,
    
    CONSTRAINT fk_ph_vehicle_pricing
        FOREIGN KEY (pricing_id) REFERENCES vehicle_pricing(pricing_id)
        ON DELETE SET NULL
        ON UPDATE NO ACTION
        
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Audit trail for price calculations with detailed breakdown and rule snapshots';

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
  AND TABLE_NAME = 'price_history';

-- ============================================================================
-- Display table structure
-- ============================================================================
DESCRIBE price_history;

