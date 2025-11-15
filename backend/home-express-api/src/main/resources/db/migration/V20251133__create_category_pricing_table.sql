-- ============================================================================
-- Create category_pricing table for category-specific pricing configuration
-- ============================================================================
-- Migration: V20251133__create_category_pricing_table.sql
-- Description: Create category_pricing table for storing category-specific pricing configuration
-- Date: 2025-02-02
-- Issue: Missing category_pricing table for item category pricing rules

-- ============================================================================
-- CATEGORY_PRICING TABLE
-- ============================================================================
-- Purpose: Time-versioned pricing rules for item categories
--   - Price per unit for each category (optionally by size)
--   - Multipliers for fragile, disassembly, heavy items
--   - Valid from/to date range for price versioning
-- ============================================================================

CREATE TABLE IF NOT EXISTS category_pricing (
    category_pricing_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    transport_id BIGINT NOT NULL COMMENT 'Transport company that owns this pricing',
    category_id BIGINT NOT NULL COMMENT 'Item category this pricing applies to',
    size_id BIGINT DEFAULT NULL COMMENT 'Specific size (NULL = applies to all sizes of category)',
    
    -- Base pricing
    price_per_unit_vnd DECIMAL(12,0) NOT NULL COMMENT 'Price per unit in VND',
    
    -- Multipliers for special handling
    fragile_multiplier DECIMAL(4,2) NOT NULL DEFAULT 1.20 COMMENT 'Multiplier for fragile items (default 1.20 = 20% increase)',
    disassembly_multiplier DECIMAL(4,2) NOT NULL DEFAULT 1.30 COMMENT 'Multiplier for items requiring disassembly (default 1.30 = 30% increase)',
    heavy_multiplier DECIMAL(4,2) NOT NULL DEFAULT 1.50 COMMENT 'Multiplier for heavy items (default 1.50 = 50% increase)',
    heavy_threshold_kg DECIMAL(6,2) NOT NULL DEFAULT 100.00 COMMENT 'Weight threshold for heavy items (kg)',
    
    -- Lifecycle
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Whether this pricing is currently active',
    valid_from DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Start date for this pricing',
    valid_to DATETIME DEFAULT NULL COMMENT 'End date for this pricing (NULL = indefinite)',
    
    -- Audit fields
    created_by BIGINT DEFAULT NULL COMMENT 'User who created this pricing',
    updated_by BIGINT DEFAULT NULL COMMENT 'User who last updated this pricing',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation timestamp',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
    
    PRIMARY KEY (category_pricing_id),
    
    -- Indexes for performance (hot lookup path)
    KEY idx_cp_hot_lookup (transport_id, category_id, size_id, is_active, valid_from, valid_to),
    KEY idx_cp_transport_active (transport_id, is_active),
    KEY idx_cp_category (category_id),
    KEY idx_cp_size (size_id),
    KEY idx_cp_validity (valid_from, valid_to),
    
    -- Constraints
    CONSTRAINT chk_cp_money_positive
        CHECK (price_per_unit_vnd >= 0),
    
    CONSTRAINT chk_cp_multiplier_bounds
        CHECK (
            fragile_multiplier >= 1.00 AND fragile_multiplier <= 3.00 AND
            disassembly_multiplier >= 1.00 AND disassembly_multiplier <= 3.00 AND
            heavy_multiplier >= 1.00 AND heavy_multiplier <= 5.00
        ),
    
    CONSTRAINT chk_cp_valid_range
        CHECK (valid_to IS NULL OR valid_to > valid_from),
    
    CONSTRAINT chk_cp_heavy_threshold
        CHECK (heavy_threshold_kg > 0),
    
    -- Foreign keys
    CONSTRAINT fk_cp_transport
        FOREIGN KEY (transport_id) REFERENCES transports(transport_id)
        ON DELETE CASCADE
        ON UPDATE NO ACTION,
    
    CONSTRAINT fk_cp_category
        FOREIGN KEY (category_id) REFERENCES categories(category_id)
        ON DELETE CASCADE
        ON UPDATE NO ACTION,
    
    CONSTRAINT fk_cp_size
        FOREIGN KEY (size_id) REFERENCES sizes(size_id)
        ON DELETE SET NULL
        ON UPDATE NO ACTION,
    
    CONSTRAINT fk_cp_created_by
        FOREIGN KEY (created_by) REFERENCES users(user_id)
        ON DELETE SET NULL
        ON UPDATE NO ACTION,
    
    CONSTRAINT fk_cp_updated_by
        FOREIGN KEY (updated_by) REFERENCES users(user_id)
        ON DELETE SET NULL
        ON UPDATE NO ACTION
        
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Time-versioned pricing rules for item categories with special handling multipliers';

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
  AND TABLE_NAME = 'category_pricing';

-- ============================================================================
-- Display table structure
-- ============================================================================
DESCRIBE category_pricing;

