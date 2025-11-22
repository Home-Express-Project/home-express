-- ============================================================================
-- Create product_models table - for intelligent autocomplete and analytics
-- ============================================================================
-- Migration: V4__Create_Product_Models_Table.sql
-- Description: Create the product_models table to store common product definitions
-- Date: 2025-11-21
-- Note: Stores brands and models to power the smart autocomplete
-- ============================================================================

CREATE TABLE IF NOT EXISTS `product_models` (
    `model_id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `brand` VARCHAR(100) NOT NULL COMMENT 'Brand/Manufacturer',
    `model` VARCHAR(200) NOT NULL COMMENT 'Model Name/Number',
    `product_name` VARCHAR(255) DEFAULT NULL COMMENT 'Full descriptive name',
    `category_id` BIGINT DEFAULT NULL COMMENT 'Mapped Category ID',
    `weight_kg` DECIMAL(10, 2) DEFAULT NULL COMMENT 'Typical weight',
    `dimensions_mm` JSON DEFAULT NULL COMMENT 'Dimensions in mm',
    `source` VARCHAR(50) DEFAULT 'system' COMMENT 'Source: system, user, ai',
    `source_url` TEXT DEFAULT NULL COMMENT 'Reference URL',
    `usage_count` INT NOT NULL DEFAULT 1 COMMENT 'Popularity score',
    `last_used_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Last usage timestamp',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Unique constraint to prevent duplicates
    CONSTRAINT `uk_brand_model` UNIQUE (`brand`, `model`),
    
    -- Index for search
    INDEX `idx_pm_brand` (`brand`),
    INDEX `idx_pm_model` (`model`),
    INDEX `idx_pm_search` (`brand`, `model`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
