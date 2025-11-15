-- ============================================================================
-- Create transports table - transport companies/providers
-- ============================================================================
-- Migration: V20251101_1__create_transports_table.sql
-- Description: Create the transports table for transport companies
-- Date: 2025-01-01
-- Note: This must run after V20251101 (users table) and before V20251102_1 (bookings)

CREATE TABLE IF NOT EXISTS transports (
    transport_id BIGINT NOT NULL PRIMARY KEY,
    
    -- Company info
    company_name VARCHAR(255) NOT NULL COMMENT 'Transport company name',
    business_license_number VARCHAR(50) NOT NULL UNIQUE COMMENT 'GPKD - 10 or 13 digits',
    tax_code VARCHAR(50) DEFAULT NULL UNIQUE COMMENT 'Tax code',
    phone VARCHAR(20) NOT NULL COMMENT 'Company phone number',
    address TEXT NOT NULL COMMENT 'Company address',
    city VARCHAR(100) NOT NULL COMMENT 'City',
    district VARCHAR(100) DEFAULT NULL COMMENT 'District',
    ward VARCHAR(100) DEFAULT NULL COMMENT 'Ward',
    
    -- Documents
    license_photo_url TEXT DEFAULT NULL COMMENT 'Business license photo URL',
    insurance_photo_url TEXT DEFAULT NULL COMMENT 'Insurance photo URL',
    
    -- Verification workflow
    verification_status ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING' COMMENT 'Verification status',
    verified_at DATETIME DEFAULT NULL COMMENT 'Verification timestamp',
    verified_by BIGINT DEFAULT NULL COMMENT 'Admin who verified',
    verification_notes TEXT DEFAULT NULL COMMENT 'Verification notes',
    
    -- Statistics
    total_bookings INT NOT NULL DEFAULT 0 COMMENT 'Total bookings count',
    completed_bookings INT NOT NULL DEFAULT 0 COMMENT 'Completed bookings count',
    cancelled_bookings INT NOT NULL DEFAULT 0 COMMENT 'Cancelled bookings count',
    average_rating DECIMAL(3,2) DEFAULT 0.00 COMMENT 'Average rating',
    
    -- KYC - Vietnamese specific
    national_id_number VARCHAR(12) DEFAULT NULL UNIQUE COMMENT 'CMND/CCCD number - 9 or 12 digits',
    national_id_type ENUM('CMND', 'CCCD', 'PASSPORT') DEFAULT NULL COMMENT 'ID type',
    national_id_issue_date DATE DEFAULT NULL COMMENT 'ID issue date',
    national_id_issuer VARCHAR(100) DEFAULT NULL COMMENT 'ID issuer',
    national_id_photo_front_url TEXT DEFAULT NULL COMMENT 'ID front photo URL',
    national_id_photo_back_url TEXT DEFAULT NULL COMMENT 'ID back photo URL',
    
    -- Banking info - VN banks
    bank_name VARCHAR(100) DEFAULT NULL COMMENT 'Bank name',
    bank_code VARCHAR(10) DEFAULT NULL COMMENT 'Bank code (FK to vn_banks)',
    bank_account_number VARCHAR(19) DEFAULT NULL COMMENT 'Bank account number',
    bank_account_holder VARCHAR(255) DEFAULT NULL COMMENT 'Account holder name',
    
    -- Timestamps
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation timestamp',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
    
    -- Foreign keys
    CONSTRAINT fk_transports_user
        FOREIGN KEY (transport_id) REFERENCES users(user_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_transports_verified_by
        FOREIGN KEY (verified_by) REFERENCES users(user_id)
        ON DELETE SET NULL,
    
    -- Indexes
    INDEX idx_transports_verification_status (verification_status),
    INDEX idx_transports_city (city),
    INDEX idx_transports_phone (phone),
    INDEX idx_transports_business_license (business_license_number),
    INDEX idx_transports_average_rating (average_rating)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Transport companies/providers with verification and KYC';

