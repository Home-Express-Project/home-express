-- ============================================================================
-- Modify evidence table to support booking-level evidence
-- ============================================================================
-- Migration: V20251201__modify_evidence_for_bookings.sql
-- Description: Extend evidence table to support both incident and booking evidence
-- Date: 2025-12-01
-- Issue: Phase 2.1 - Evidence Viewing System

-- ============================================================================
-- MODIFY EVIDENCE TABLE
-- ============================================================================
-- Purpose: Allow evidence to be attached to bookings directly (not just incidents)
--   - Make incident_id nullable to support booking-level evidence
--   - Add booking_id column for direct booking evidence
--   - Add evidence_type enum for categorization (PICKUP_PHOTO, DELIVERY_PHOTO, etc.)
--   - Add mime_type for better file type tracking
-- ============================================================================

-- Add booking_id column if it doesn't exist
SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'evidence'
      AND COLUMN_NAME = 'booking_id'
);

SET @sql = IF(@column_exists = 0,
    'ALTER TABLE evidence ADD COLUMN booking_id BIGINT DEFAULT NULL COMMENT ''Booking this evidence belongs to (for booking-level evidence)'' AFTER evidence_id',
    'SELECT ''Column booking_id already exists'' AS message'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add evidence_type column if it doesn't exist
SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'evidence'
      AND COLUMN_NAME = 'evidence_type'
);

SET @sql = IF(@column_exists = 0,
    'ALTER TABLE evidence ADD COLUMN evidence_type ENUM(''PICKUP_PHOTO'', ''DELIVERY_PHOTO'', ''DAMAGE_PHOTO'', ''SIGNATURE'', ''INVOICE'', ''OTHER'') DEFAULT ''OTHER'' COMMENT ''Type/category of evidence'' AFTER uploaded_by_user_id',
    'SELECT ''Column evidence_type already exists'' AS message'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add mime_type column if it doesn't exist
SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'evidence'
      AND COLUMN_NAME = 'mime_type'
);

SET @sql = IF(@column_exists = 0,
    'ALTER TABLE evidence ADD COLUMN mime_type VARCHAR(100) DEFAULT NULL COMMENT ''MIME type of the file (e.g., image/jpeg, application/pdf)'' AFTER file_name',
    'SELECT ''Column mime_type already exists'' AS message'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Make incident_id nullable (evidence can be for booking OR incident)
ALTER TABLE evidence
    MODIFY COLUMN incident_id BIGINT DEFAULT NULL COMMENT 'Incident this evidence belongs to (nullable for booking-level evidence)';

-- Add constraint if it doesn't exist
SET @constraint_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'evidence'
      AND CONSTRAINT_NAME = 'chk_evidence_reference'
);

SET @sql = IF(@constraint_exists = 0,
    'ALTER TABLE evidence ADD CONSTRAINT chk_evidence_reference CHECK ((booking_id IS NOT NULL AND incident_id IS NULL) OR (booking_id IS NULL AND incident_id IS NOT NULL))',
    'SELECT ''Constraint chk_evidence_reference already exists'' AS message'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add foreign key if it doesn't exist
SET @fk_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'evidence'
      AND CONSTRAINT_NAME = 'fk_evidence_booking'
);

SET @sql = IF(@fk_exists = 0,
    'ALTER TABLE evidence ADD CONSTRAINT fk_evidence_booking FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON DELETE CASCADE ON UPDATE NO ACTION',
    'SELECT ''Foreign key fk_evidence_booking already exists'' AS message'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add indexes if they don't exist
SET @index_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'evidence'
      AND INDEX_NAME = 'idx_evidence_booking'
);

SET @sql = IF(@index_exists = 0,
    'CREATE INDEX idx_evidence_booking ON evidence(booking_id, uploaded_at DESC)',
    'SELECT ''Index idx_evidence_booking already exists'' AS message'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'evidence'
      AND INDEX_NAME = 'idx_evidence_type'
);

SET @sql = IF(@index_exists = 0,
    'CREATE INDEX idx_evidence_type ON evidence(evidence_type, uploaded_at DESC)',
    'SELECT ''Index idx_evidence_type already exists'' AS message'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'evidence'
      AND INDEX_NAME = 'idx_evidence_booking_type'
);

SET @sql = IF(@index_exists = 0,
    'CREATE INDEX idx_evidence_booking_type ON evidence(booking_id, evidence_type, uploaded_at DESC)',
    'SELECT ''Index idx_evidence_booking_type already exists'' AS message'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================================================
-- Verify modifications
-- ============================================================================
SELECT 
    TABLE_NAME,
    TABLE_ROWS,
    CREATE_TIME,
    UPDATE_TIME,
    TABLE_COMMENT
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'evidence';

-- ============================================================================
-- Display updated table structure
-- ============================================================================
DESCRIBE evidence;

