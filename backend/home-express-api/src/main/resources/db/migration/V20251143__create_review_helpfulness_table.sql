-- ============================================================================
-- Create review_helpfulness table for review voting
-- ============================================================================
-- Migration: V20251143__create_review_helpfulness_table.sql
-- Description: Create review_helpfulness table for tracking review helpfulness votes
-- Date: 2025-02-12
-- Issue: Missing review_helpfulness table for review voting

-- ============================================================================
-- REVIEW_HELPFULNESS TABLE
-- ============================================================================
-- Purpose: Track user votes on review helpfulness (upvotes/downvotes)
--   - Surface the most useful reviews
--   - Prevent duplicate votes (unique constraint on review_id + voter_id)
--   - Track helpful vs unhelpful votes
--   - Support review quality ranking
-- ============================================================================

CREATE TABLE IF NOT EXISTS review_helpfulness (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    
    -- References
    review_id BIGINT NOT NULL COMMENT 'Review being voted on',
    voter_id BIGINT NOT NULL COMMENT 'User who voted',
    
    -- Vote
    is_helpful BOOLEAN NOT NULL COMMENT 'TRUE = helpful, FALSE = not helpful',
    
    -- Metadata
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'When vote was cast',
    
    PRIMARY KEY (id),
    
    -- Unique constraint: one vote per user per review
    UNIQUE KEY uk_review_helpfulness (review_id, voter_id),
    
    -- Indexes for performance
    KEY idx_review_helpfulness_review (review_id),
    KEY idx_review_helpfulness_voter (voter_id),
    KEY idx_review_helpfulness_created (created_at DESC),
    
    -- Foreign keys
    CONSTRAINT fk_review_helpfulness_review
        FOREIGN KEY (review_id) REFERENCES reviews(review_id)
        ON DELETE CASCADE
        ON UPDATE NO ACTION,
    
    CONSTRAINT fk_review_helpfulness_voter
        FOREIGN KEY (voter_id) REFERENCES users(user_id)
        ON DELETE NO ACTION
        ON UPDATE NO ACTION
        
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Helpful voting to surface the most useful reviews';

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
  AND TABLE_NAME = 'review_helpfulness';

-- ============================================================================
-- Display table structure
-- ============================================================================
DESCRIBE review_helpfulness;

