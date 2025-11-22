-- ============================================================================
-- Create saved_items table - customer saved items storage
-- ============================================================================
-- Migration: V3__Create_Saved_Items_Table.sql
-- Description: Create the saved_items table for customer item inventory
-- Date: 2025-11-15
-- Note: This table allows customers to save items and reuse them when creating bookings

CREATE TABLE IF NOT EXISTS `saved_items` (
    `saved_item_id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    
    -- Customer reference
    `customer_id` BIGINT NOT NULL COMMENT 'Customer ID (FK to customers)',
    
    -- Item basic info
    `name` VARCHAR(255) NOT NULL COMMENT 'Item name',
    `brand` VARCHAR(100) DEFAULT NULL COMMENT 'Brand/manufacturer',
    `model` VARCHAR(200) DEFAULT NULL COMMENT 'Model',
    
    -- Category
    `category_id` BIGINT DEFAULT NULL COMMENT 'Category ID (FK to categories)',
    
    -- Dimensions and weight
    `size` VARCHAR(50) DEFAULT NULL COMMENT 'Size (e.g., S, M, L)',
    `weight_kg` DECIMAL(8, 2) DEFAULT NULL COMMENT 'Weight in kilograms',
    `dimensions` JSON DEFAULT NULL COMMENT 'Dimensions as JSON {height, width, depth}',
    
    -- Valuation
    `declared_value_vnd` DECIMAL(15, 2) DEFAULT NULL COMMENT 'Declared value in VND',
    
    -- Quantity
    `quantity` INT NOT NULL DEFAULT 1 COMMENT 'Number of items',
    
    -- Handling flags
    `is_fragile` BOOLEAN DEFAULT FALSE COMMENT 'Is the item fragile',
    `requires_disassembly` BOOLEAN DEFAULT FALSE COMMENT 'Requires disassembly',
    `requires_packaging` BOOLEAN DEFAULT FALSE COMMENT 'Requires special packaging',
    
    -- Additional info
    `notes` TEXT DEFAULT NULL COMMENT 'Additional notes',
    `metadata` JSON DEFAULT NULL COMMENT 'Additional metadata as JSON',
    
    -- Timestamps
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation timestamp',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
    
    -- Foreign keys
    CONSTRAINT `fk_saved_items_customer_ref`
        FOREIGN KEY (`customer_id`) REFERENCES `customers`(`customer_id`)
        ON DELETE CASCADE,
    CONSTRAINT `fk_saved_items_category_ref`
        FOREIGN KEY (`category_id`) REFERENCES `categories`(`category_id`)
        ON DELETE SET NULL,
    
    -- Indexes
    INDEX `idx_saved_items_customer_id` (`customer_id`),
    INDEX `idx_saved_items_category_id` (`category_id`),
    INDEX `idx_saved_items_created_at` (`created_at`),
    INDEX `idx_saved_items_customer_created` (`customer_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Customer saved items for inventory/storage management';
