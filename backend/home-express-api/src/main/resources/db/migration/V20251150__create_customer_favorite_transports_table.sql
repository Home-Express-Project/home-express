-- Migration: Create customer_favorite_transports table
-- Description: Allows customers to save their favorite transport providers for quick rebooking
-- Author: Home Express Team
-- Date: 2025-11-13

CREATE TABLE customer_favorite_transports (
    favorite_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary key for favorite transport records',
    customer_id BIGINT NOT NULL COMMENT 'Foreign key to customers table',
    transport_id BIGINT NOT NULL COMMENT 'Foreign key to transports table',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Timestamp when favorite was added',
    
    -- Constraints
    CONSTRAINT fk_customer_favorite_customer 
        FOREIGN KEY (customer_id) REFERENCES customers(customer_id) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE,
    
    CONSTRAINT fk_customer_favorite_transport 
        FOREIGN KEY (transport_id) REFERENCES transports(transport_id) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE,
    
    -- Unique constraint to prevent duplicate favorites
    CONSTRAINT uk_customer_transport_favorite 
        UNIQUE KEY (customer_id, transport_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Stores customer favorite transport providers for quick rebooking';

-- Indexes for query performance
CREATE INDEX idx_customer_favorite_customer_id ON customer_favorite_transports(customer_id);
CREATE INDEX idx_customer_favorite_transport_id ON customer_favorite_transports(transport_id);
CREATE INDEX idx_customer_favorite_created_at ON customer_favorite_transports(created_at);

