-- ============================================================================
-- Create distance_cache table for caching distance calculations
-- ============================================================================
-- Migration: V20251135__create_distance_cache_table.sql
-- Description: Create distance_cache table for caching distance calculations between locations
-- Date: 2025-02-04
-- Issue: Missing distance_cache table for distance API result caching

-- ============================================================================
-- DISTANCE_CACHE TABLE
-- ============================================================================
-- Purpose: Cache distance and duration calculations from external APIs
--   - Support multiple providers (GOOGLE, MAPBOX, OSRM)
--   - Support multiple modes (DRIVING, WALKING, BICYCLING)
--   - Use SHA-256 hashes for efficient lookup
--   - Auto-expire after 30 days
--   - Reduce API costs and improve performance
-- ============================================================================

CREATE TABLE IF NOT EXISTS distance_cache (
    distance_cache_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    
    -- API configuration
    provider ENUM('GOOGLE', 'MAPBOX', 'OSRM') NOT NULL DEFAULT 'GOOGLE' COMMENT 'Distance API provider',
    mode ENUM('DRIVING', 'WALKING', 'BICYCLING') NOT NULL DEFAULT 'DRIVING' COMMENT 'Travel mode',
    
    -- Location hashes for efficient lookup
    origin_hash CHAR(44) NOT NULL COMMENT 'Base64(SHA-256(origin address))',
    destination_hash CHAR(44) NOT NULL COMMENT 'Base64(SHA-256(destination address))',
    
    -- Full addresses (for debugging and display)
    origin_address TEXT NOT NULL COMMENT 'Full origin address',
    destination_address TEXT NOT NULL COMMENT 'Full destination address',
    
    -- Coordinates (optional, for map display)
    origin_latitude DECIMAL(10,8) DEFAULT NULL COMMENT 'Origin latitude',
    origin_longitude DECIMAL(11,8) DEFAULT NULL COMMENT 'Origin longitude',
    destination_latitude DECIMAL(10,8) DEFAULT NULL COMMENT 'Destination latitude',
    destination_longitude DECIMAL(11,8) DEFAULT NULL COMMENT 'Destination longitude',
    
    -- Calculated results
    distance_km DECIMAL(8,3) NOT NULL COMMENT 'Distance in kilometers',
    duration_minutes INT NOT NULL COMMENT 'Estimated duration in minutes',
    
    -- Cache expiration
    expires_at DATETIME NOT NULL DEFAULT (CURRENT_TIMESTAMP + INTERVAL 30 DAY) COMMENT 'Cache expiration (auto-expires in 30 days)',
    
    -- Timestamps
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation timestamp',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
    
    PRIMARY KEY (distance_cache_id),
    
    -- Unique constraint for efficient lookup
    UNIQUE KEY uk_distance_cache_lookup (provider, mode, origin_hash, destination_hash),
    
    -- Indexes for performance
    KEY idx_distance_cache_expires (expires_at),
    KEY idx_distance_cache_provider (provider),
    KEY idx_distance_cache_created (created_at),
    
    -- Constraints
    CONSTRAINT chk_distance_cache_values
        CHECK (distance_km >= 0 AND duration_minutes >= 0),
    
    CONSTRAINT chk_distance_cache_expiry
        CHECK (expires_at > created_at),
    
    CONSTRAINT chk_distance_cache_coordinates
        CHECK (
            (origin_latitude IS NULL OR (origin_latitude BETWEEN -90 AND 90)) AND
            (origin_longitude IS NULL OR (origin_longitude BETWEEN -180 AND 180)) AND
            (destination_latitude IS NULL OR (destination_latitude BETWEEN -90 AND 90)) AND
            (destination_longitude IS NULL OR (destination_longitude BETWEEN -180 AND 180))
        )
        
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Cache for distance/duration calculations from external APIs with auto-expiration';

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
  AND TABLE_NAME = 'distance_cache';

-- ============================================================================
-- Display table structure
-- ============================================================================
DESCRIBE distance_cache;

