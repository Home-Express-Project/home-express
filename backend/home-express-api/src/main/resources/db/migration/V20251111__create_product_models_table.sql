-- V20251111__create_product_models_table.sql
-- Table to store product brands and models for autocomplete suggestions

CREATE TABLE product_models (
    model_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    brand VARCHAR(100) NOT NULL,
    model VARCHAR(200) NOT NULL,
    product_name VARCHAR(255),
    category_id BIGINT,
    weight_kg DECIMAL(10,2),
    dimensions_mm JSON COMMENT 'JSON: {width, height, depth}',
    source VARCHAR(50) COMMENT 'ikea_api, manual_entry, ocr_extraction',
    source_url TEXT,
    usage_count INT DEFAULT 1 COMMENT 'Number of times this model was used',
    last_used_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    UNIQUE KEY uk_brand_model (brand, model),
    KEY idx_brand (brand),
    KEY idx_usage_count (usage_count DESC),
    KEY idx_last_used (last_used_at DESC),
    
    FOREIGN KEY (category_id) REFERENCES categories(category_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Index for autocomplete search
CREATE INDEX idx_brand_model_search ON product_models(brand, model);
