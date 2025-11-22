-- ============================================================================
-- Create rating_summaries table for cached rating statistics
-- ============================================================================
-- Migration: V20251144__create_rating_summaries_table.sql
-- Description: Create rating_summaries table for storing aggregated rating statistics
-- Date: 2025-02-13
-- Issue: Missing rating_summaries table for performance optimization

-- ============================================================================
-- RATING_SUMMARIES TABLE
-- ============================================================================
-- Purpose: Store cached/aggregated rating statistics for O(1) lookups
--   - Average ratings (overall, punctuality, professionalism, communication, care)
--   - Total review count
--   - Star distribution (5-star, 4-star, 3-star, 2-star, 1-star counts)
--   - Separate summaries for AS_CUSTOMER and AS_TRANSPORT contexts
--   - Denormalized for query performance
-- ============================================================================

CREATE TABLE IF NOT EXISTS rating_summaries (
    summary_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    
    -- References
    target_user_id BIGINT NOT NULL COMMENT 'User being rated (reviewee)',
    context ENUM('AS_CUSTOMER', 'AS_TRANSPORT') NOT NULL COMMENT 'Rated as customer or transport',
    
    -- Aggregated Counts (multiplied by 10 for precision)
    total_count INT NOT NULL DEFAULT 0 COMMENT 'Total number of reviews',
    sum_overall_x10 BIGINT NOT NULL DEFAULT 0 COMMENT 'Sum of (rating * 10) for precision',
    sum_punctuality_x10 BIGINT NOT NULL DEFAULT 0 COMMENT 'Sum of (punctuality_rating * 10)',
    sum_professionalism_x10 BIGINT NOT NULL DEFAULT 0 COMMENT 'Sum of (professionalism_rating * 10)',
    sum_communication_x10 BIGINT NOT NULL DEFAULT 0 COMMENT 'Sum of (communication_rating * 10)',
    sum_care_x10 BIGINT NOT NULL DEFAULT 0 COMMENT 'Sum of (care_rating * 10)',
    
    -- Star Distribution
    count_5_star INT NOT NULL DEFAULT 0 COMMENT 'Number of 5-star reviews',
    count_4_star INT NOT NULL DEFAULT 0 COMMENT 'Number of 4-star reviews',
    count_3_star INT NOT NULL DEFAULT 0 COMMENT 'Number of 3-star reviews',
    count_2_star INT NOT NULL DEFAULT 0 COMMENT 'Number of 2-star reviews',
    count_1_star INT NOT NULL DEFAULT 0 COMMENT 'Number of 1-star reviews',
    
    -- Computed Averages (GENERATED columns for quick reads)
    avg_overall DECIMAL(3,2) GENERATED ALWAYS AS (
        CASE WHEN total_count > 0
            THEN ROUND(sum_overall_x10 / 10.0 / total_count, 2)
            ELSE 0
        END
    ) STORED COMMENT 'Average overall rating',
    
    avg_punctuality DECIMAL(3,2) GENERATED ALWAYS AS (
        CASE WHEN total_count > 0 AND sum_punctuality_x10 > 0
            THEN ROUND(sum_punctuality_x10 / 10.0 / total_count, 2)
            ELSE 0
        END
    ) STORED COMMENT 'Average punctuality rating',
    
    -- Metadata
    last_review_at DATETIME DEFAULT NULL COMMENT 'When last review was received',
    last_updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation timestamp',
    
    PRIMARY KEY (summary_id),
    
    -- Unique constraint: one summary per user per context
    UNIQUE KEY uk_rating_summaries (target_user_id, context),
    
    -- Indexes for performance
    KEY idx_rating_summaries_avg (avg_overall DESC),
    KEY idx_rating_summaries_context (context, avg_overall DESC),
    KEY idx_rating_summaries_updated (last_updated_at DESC),
    
    -- Foreign keys
    CONSTRAINT fk_rating_summaries_user
        FOREIGN KEY (target_user_id) REFERENCES users(user_id)
        ON DELETE CASCADE
        ON UPDATE NO ACTION
        
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Denormalized rating summaries for O(1) lookups';

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
  AND TABLE_NAME = 'rating_summaries';

-- ============================================================================
-- Display table structure
-- ============================================================================
DESCRIBE rating_summaries;

