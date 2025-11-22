-- ============================================================================
-- Create evidence table for incident evidence files
-- ============================================================================
-- Migration: V20251141__create_evidence_table.sql
-- Description: Create evidence table for storing incident evidence files
-- Date: 2025-02-10
-- Issue: Missing evidence table for incident evidence management

-- ============================================================================
-- EVIDENCE TABLE
-- ============================================================================
-- Purpose: Store incident evidence files uploaded by customers, transports, or managers
--   - Support for images, videos, and documents
--   - Track file metadata (size, type, name)
--   - Link to incidents for investigation and resolution
--   - Track uploader for audit trail
-- ============================================================================

CREATE TABLE IF NOT EXISTS evidence (
    evidence_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    
    -- References
    incident_id BIGINT NOT NULL COMMENT 'Incident this evidence belongs to',
    uploaded_by_user_id BIGINT NOT NULL COMMENT 'User who uploaded the evidence',
    
    -- File Information
    file_type ENUM('IMAGE', 'VIDEO', 'DOCUMENT') NOT NULL COMMENT 'Type of evidence file',
    file_url TEXT NOT NULL COMMENT 'URL to the evidence file (S3, Cloudinary, etc.)',
    file_name VARCHAR(500) NOT NULL COMMENT 'Original file name',
    file_size_bytes BIGINT DEFAULT NULL COMMENT 'File size in bytes',
    
    -- Description
    description TEXT DEFAULT NULL COMMENT 'Description of the evidence',
    
    -- Metadata
    uploaded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'When evidence was uploaded',
    
    PRIMARY KEY (evidence_id),
    
    -- Indexes for performance
    KEY idx_evidence_incident (incident_id, uploaded_at DESC),
    KEY idx_evidence_uploader (uploaded_by_user_id, uploaded_at DESC),
    KEY idx_evidence_type (file_type, uploaded_at DESC),
    
    -- Foreign keys
    CONSTRAINT fk_evidence_incident
        FOREIGN KEY (incident_id) REFERENCES incidents(incident_id)
        ON DELETE CASCADE
        ON UPDATE NO ACTION,
    
    CONSTRAINT fk_evidence_uploaded_by
        FOREIGN KEY (uploaded_by_user_id) REFERENCES users(user_id)
        ON DELETE CASCADE
        ON UPDATE NO ACTION
        
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Incident evidence files with ownership and type tracking';

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
  AND TABLE_NAME = 'evidence';

-- ============================================================================
-- Display table structure
-- ============================================================================
DESCRIBE evidence;

