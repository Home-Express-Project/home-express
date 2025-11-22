-- V20251112__create_customer_saved_items_table.sql
-- Table for customer's saved/draft items (items waiting in storage before creating booking)

CREATE TABLE customer_saved_items (
    saved_item_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id BIGINT NOT NULL,
    
    -- Item information
    name VARCHAR(255) NOT NULL,
    brand VARCHAR(100),
    model VARCHAR(200),
    category_id BIGINT,
    
    -- Physical attributes
    size VARCHAR(10) COMMENT 'S, M, L',
    weight_kg DECIMAL(10,2),
    dimensions JSON COMMENT 'JSON: {width_cm, height_cm, depth_cm}',
    
    -- Quantity and attributes
    quantity INT NOT NULL DEFAULT 1,
    is_fragile BOOLEAN DEFAULT FALSE,
    requires_disassembly BOOLEAN DEFAULT FALSE,
    requires_packaging BOOLEAN DEFAULT FALSE,
    
    -- Additional info
    notes TEXT,
    metadata JSON COMMENT 'JSON: {source, model_id, image_url, etc}',
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT fk_saved_items_customer FOREIGN KEY (customer_id) 
        REFERENCES customers(customer_id) ON DELETE CASCADE,
    CONSTRAINT fk_saved_items_category FOREIGN KEY (category_id) 
        REFERENCES categories(category_id) ON DELETE SET NULL,
    
    -- Indexes
    KEY idx_customer_created (customer_id, created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
