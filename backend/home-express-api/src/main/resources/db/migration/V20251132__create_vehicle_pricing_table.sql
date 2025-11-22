-- ============================================================================
-- Create vehicle_pricing table for vehicle-specific pricing configuration
-- ============================================================================
-- Migration: V20251132__create_vehicle_pricing_table.sql
-- Description: Create vehicle_pricing table for storing vehicle-specific pricing configuration
-- Date: 2025-02-01
-- Issue: Missing vehicle_pricing table for transport pricing rules

-- ============================================================================
-- VEHICLE_PRICING TABLE
-- ============================================================================
-- Purpose: Time-versioned pricing rules for vehicle types
--   - Tiered distance pricing (0-4km, 5-40km, >40km)
--   - Elevator fees and bonuses
--   - Peak hour and weekend multipliers
--   - Valid from/to date range for price versioning
-- ============================================================================

CREATE TABLE IF NOT EXISTS vehicle_pricing (
    vehicle_pricing_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    transport_id BIGINT NOT NULL COMMENT 'Transport company that owns this pricing',
    vehicle_type ENUM('motorcycle', 'van', 'truck_small', 'truck_large', 'other') NOT NULL COMMENT 'Vehicle type',
    
    -- Base and distance pricing (integer VND values)
    base_price_vnd DECIMAL(12,0) NOT NULL COMMENT 'Base price in VND',
    per_km_first_4km_vnd DECIMAL(12,0) NOT NULL COMMENT 'Price per km for first 4km (VND)',
    per_km_5_to_40km_vnd DECIMAL(12,0) NOT NULL COMMENT 'Price per km for 5-40km range (VND)',
    per_km_after_40km_vnd DECIMAL(12,0) NOT NULL COMMENT 'Price per km after 40km (VND)',
    min_charge_vnd DECIMAL(12,0) DEFAULT NULL COMMENT 'Minimum charge if applicable (VND)',
    
    -- Elevator fees
    elevator_bonus_vnd DECIMAL(12,0) NOT NULL DEFAULT 0 COMMENT 'Discount when elevator available at both locations (VND)',
    no_elevator_fee_per_floor_vnd DECIMAL(12,0) NOT NULL DEFAULT 0 COMMENT 'Fee per floor when no elevator (VND)',
    no_elevator_floor_threshold INT NOT NULL DEFAULT 3 COMMENT 'Apply fee when floor > threshold',
    
    -- Time-based multipliers
    peak_hour_multiplier DECIMAL(4,2) NOT NULL DEFAULT 1.00 COMMENT 'Multiplier during peak hours',
    weekend_multiplier DECIMAL(4,2) NOT NULL DEFAULT 1.00 COMMENT 'Multiplier on weekends',
    peak_hour_start_1 TINYINT DEFAULT 7 COMMENT 'First peak hour start (0-23, VN timezone)',
    peak_hour_end_1 TINYINT DEFAULT 9 COMMENT 'First peak hour end (0-23, exclusive)',
    peak_hour_start_2 TINYINT DEFAULT 17 COMMENT 'Second peak hour start (0-23)',
    peak_hour_end_2 TINYINT DEFAULT 19 COMMENT 'Second peak hour end (0-23, exclusive)',
    timezone VARCHAR(50) NOT NULL DEFAULT 'Asia/Ho_Chi_Minh' COMMENT 'Timezone for peak hour calculation',
    
    -- Lifecycle
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Whether this pricing is currently active',
    valid_from DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Start date for this pricing',
    valid_to DATETIME DEFAULT NULL COMMENT 'End date for this pricing (NULL = indefinite)',
    
    -- Audit fields
    created_by BIGINT DEFAULT NULL COMMENT 'User who created this pricing',
    updated_by BIGINT DEFAULT NULL COMMENT 'User who last updated this pricing',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation timestamp',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
    
    PRIMARY KEY (vehicle_pricing_id),
    
    -- Indexes for performance (hot lookup path)
    KEY idx_vp_hot_lookup (transport_id, vehicle_type, is_active, valid_from, valid_to),
    KEY idx_vp_transport_active (transport_id, is_active),
    KEY idx_vp_validity (valid_from, valid_to),
    KEY idx_vp_vehicle_type (vehicle_type),
    
    -- Constraints
    CONSTRAINT chk_vp_money_positive
        CHECK (
            base_price_vnd >= 0 AND
            per_km_first_4km_vnd >= 0 AND
            per_km_5_to_40km_vnd >= 0 AND
            per_km_after_40km_vnd >= 0 AND
            (min_charge_vnd IS NULL OR min_charge_vnd >= 0) AND
            elevator_bonus_vnd >= 0 AND
            no_elevator_fee_per_floor_vnd >= 0
        ),
    
    CONSTRAINT chk_vp_multipliers_bounds
        CHECK (peak_hour_multiplier >= 1.00 AND weekend_multiplier >= 1.00),
    
    CONSTRAINT chk_vp_hours_valid
        CHECK (
            (peak_hour_start_1 IS NULL OR (peak_hour_start_1 BETWEEN 0 AND 23)) AND
            (peak_hour_end_1 IS NULL OR (peak_hour_end_1 BETWEEN 0 AND 23)) AND
            (peak_hour_start_2 IS NULL OR (peak_hour_start_2 BETWEEN 0 AND 23)) AND
            (peak_hour_end_2 IS NULL OR (peak_hour_end_2 BETWEEN 0 AND 23)) AND
            ((peak_hour_start_1 IS NULL AND peak_hour_end_1 IS NULL) OR (peak_hour_start_1 < peak_hour_end_1)) AND
            ((peak_hour_start_2 IS NULL AND peak_hour_end_2 IS NULL) OR (peak_hour_start_2 < peak_hour_end_2))
        ),
    
    CONSTRAINT chk_vp_valid_range
        CHECK (valid_to IS NULL OR valid_to > valid_from),
    
    CONSTRAINT chk_vp_floor_threshold
        CHECK (no_elevator_floor_threshold >= 0),
    
    -- Foreign keys
    CONSTRAINT fk_vp_transport
        FOREIGN KEY (transport_id) REFERENCES transports(transport_id)
        ON DELETE CASCADE
        ON UPDATE NO ACTION,
    
    CONSTRAINT fk_vp_created_by
        FOREIGN KEY (created_by) REFERENCES users(user_id)
        ON DELETE SET NULL
        ON UPDATE NO ACTION,
    
    CONSTRAINT fk_vp_updated_by
        FOREIGN KEY (updated_by) REFERENCES users(user_id)
        ON DELETE SET NULL
        ON UPDATE NO ACTION
        
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Time-versioned pricing rules for vehicle types with tiered distance pricing';

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
  AND TABLE_NAME = 'vehicle_pricing';

-- ============================================================================
-- Display table structure
-- ============================================================================
DESCRIBE vehicle_pricing;

