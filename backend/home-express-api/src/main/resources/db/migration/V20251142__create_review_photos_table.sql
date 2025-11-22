-- ============================================================================
-- Create review_photos table for review photo attachments
-- ============================================================================
-- Migration: V20251142__create_review_photos_table.sql
-- Description: Create review_photos table for storing photo attachments with reviews
-- Date: 2025-02-11
-- Issue: Missing review_photos table for review photo management

-- ============================================================================
-- REVIEW_PHOTOS TABLE
-- ============================================================================
-- Purpose: Store photo attachments that customers upload with their reviews
--   - Provide visual evidence of service quality
--   - Support multiple photos per review
--   - Display order for photo gallery
--   - Optional captions for context
-- ============================================================================

CREATE TABLE IF NOT EXISTS review_photos (
    photo_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    
    -- References
    review_id BIGINT NOT NULL COMMENT 'Review this photo belongs to',
    
    -- Photo Information
    photo_url TEXT NOT NULL COMMENT 'URL to the photo (S3, Cloudinary, etc.)',
    caption VARCHAR(200) DEFAULT NULL COMMENT 'Optional photo caption',
    display_order INT DEFAULT 0 COMMENT 'Display order in photo gallery',
    
    -- Metadata
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'When photo was uploaded',
    
    PRIMARY KEY (photo_id),
    
    -- Indexes for performance
    KEY idx_review_photos_review (review_id, display_order),
    KEY idx_review_photos_created (created_at DESC),
    
    -- Foreign keys
    CONSTRAINT fk_review_photos_review
        FOREIGN KEY (review_id) REFERENCES reviews(review_id)
        ON DELETE CASCADE
        ON UPDATE NO ACTION
        
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Photo attachments for reviews to provide visual evidence';

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
  AND TABLE_NAME = 'review_photos';

-- ============================================================================
-- Display table structure
-- ============================================================================
DESCRIBE review_photos;

