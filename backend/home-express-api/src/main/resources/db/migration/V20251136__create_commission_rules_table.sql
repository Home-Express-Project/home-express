-- ============================================================================
-- Create commission_rules table for commission calculation rules
-- ============================================================================
-- Migration: V20251136__create_commission_rules_table.sql
-- Description: Create commission_rules table for storing commission calculation rules and rates
-- Date: 2025-02-05
-- Issue: Missing commission_rules table for platform fee calculation

-- ============================================================================
-- COMMISSION_RULES TABLE
-- ============================================================================
-- Purpose: Define commission rules for platform fees calculation
--   - Support both transport-specific and default (platform-wide) rules
--   - Can be PERCENT-based (e.g., 15%) or FLAT fee (e.g., 50,000 VND)
--   - Time-versioned with effective_from/effective_to for historical rate changes
--   - Business Rule: Platform fee = (agreed_price × commission_rate) / 100 OR flat_fee_vnd
-- ============================================================================

CREATE TABLE IF NOT EXISTS commission_rules (
    rule_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    
    -- Rule Scope
    transport_id BIGINT DEFAULT NULL COMMENT 'NULL = default platform rule, otherwise transport-specific',
    
    -- Commission Structure
    rule_type ENUM('PERCENT', 'FLAT') NOT NULL DEFAULT 'PERCENT' COMMENT 'Commission type: percentage or flat fee',
    commission_rate DECIMAL(6,2) NOT NULL DEFAULT 0.00 COMMENT 'Commission rate percentage (0.00 to 100.00)',
    flat_fee_vnd BIGINT DEFAULT NULL COMMENT 'Flat commission in VND (for FLAT type)',
    
    -- Status
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Whether this rule is currently active',
    
    -- Effective Period (Time-versioned)
    effective_from DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Start date for this rule',
    effective_to DATETIME DEFAULT NULL COMMENT 'End date for this rule (NULL = indefinite)',
    
    -- Audit Fields
    created_by BIGINT DEFAULT NULL COMMENT 'Manager who created this rule',
    updated_by BIGINT DEFAULT NULL COMMENT 'Manager who last updated this rule',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation timestamp',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
    
    PRIMARY KEY (rule_id),
    
    -- Unique constraint: prevent overlapping rules for same transport
    UNIQUE KEY uk_commission_rules_transport_period (transport_id, effective_from, effective_to),
    
    -- Indexes for performance
    KEY idx_commission_rules_transport_active (transport_id, is_active, effective_from),
    KEY idx_commission_rules_effective (effective_from, effective_to, is_active),
    KEY idx_commission_rules_transport (transport_id),
    
    -- Constraints
    CONSTRAINT chk_commission_rules_type_value
        CHECK (
            (rule_type = 'PERCENT' AND commission_rate >= 0 AND commission_rate <= 100) OR
            (rule_type = 'FLAT' AND flat_fee_vnd IS NOT NULL AND flat_fee_vnd >= 0)
        ),
    
    CONSTRAINT chk_commission_rules_period
        CHECK (effective_to IS NULL OR effective_to > effective_from),
    
    -- Foreign keys
    CONSTRAINT fk_commission_rules_transport
        FOREIGN KEY (transport_id) REFERENCES transports(transport_id)
        ON DELETE CASCADE
        ON UPDATE NO ACTION,
    
    CONSTRAINT fk_commission_rules_created_by
        FOREIGN KEY (created_by) REFERENCES users(user_id)
        ON DELETE SET NULL
        ON UPDATE NO ACTION,
    
    CONSTRAINT fk_commission_rules_updated_by
        FOREIGN KEY (updated_by) REFERENCES users(user_id)
        ON DELETE SET NULL
        ON UPDATE NO ACTION
        
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Commission rules for platform fees (default or transport-specific)';

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
  AND TABLE_NAME = 'commission_rules';

-- ============================================================================
-- Display table structure
-- ============================================================================
DESCRIBE commission_rules;

