-- ============================================================================
-- Create vn_banks table for Vietnamese bank reference data
-- ============================================================================
-- Migration: V20251149__create_vn_banks_table.sql
-- Description: Create vn_banks table for storing Vietnamese bank reference data
-- Date: 2025-02-18
-- Issue: Missing vn_banks table for bank reference data

-- ============================================================================
-- VN_BANKS TABLE
-- ============================================================================
-- Purpose: Store Vietnamese bank reference data
--   - Bank codes, names, logos for bank transfer payments
--   - NAPAS BIN (Bank Identification Number) for card payments
--   - SWIFT codes for international transfers
--   - Used for transport payout configuration
--   - Used for customer bank transfer payments
-- ============================================================================

CREATE TABLE IF NOT EXISTS vn_banks (
    bank_code VARCHAR(10) NOT NULL COMMENT 'Bank code (e.g., VCB, TCB, MB)',
    
    -- Bank Names
    bank_name VARCHAR(255) NOT NULL COMMENT 'Bank name in Vietnamese',
    bank_name_en VARCHAR(255) DEFAULT NULL COMMENT 'Bank name in English',
    
    -- Banking Identifiers
    napas_bin VARCHAR(8) DEFAULT NULL COMMENT 'NAPAS Bank Identification Number',
    swift_code VARCHAR(11) DEFAULT NULL COMMENT 'SWIFT/BIC code for international transfers',
    
    -- Status
    is_active BOOLEAN DEFAULT TRUE COMMENT 'Whether bank is active for payments',
    
    -- Branding
    logo_url TEXT DEFAULT NULL COMMENT 'URL to bank logo image',
    
    -- Metadata
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation timestamp',
    
    PRIMARY KEY (bank_code),
    
    -- Indexes for performance
    KEY idx_vn_banks_active (is_active, bank_name),
    KEY idx_vn_banks_napas (napas_bin),
    KEY idx_vn_banks_swift (swift_code),
    KEY idx_vn_banks_created (created_at DESC)
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Vietnamese banks for payment integration';

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
  AND TABLE_NAME = 'vn_banks';

-- ============================================================================
-- Display table structure
-- ============================================================================
DESCRIBE vn_banks;

