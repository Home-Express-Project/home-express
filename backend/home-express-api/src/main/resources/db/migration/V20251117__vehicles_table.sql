-- ============================================================================
-- FIX: Ensure vehicles table structure is correct
-- ============================================================================

USE home_express;

-- Check if vehicles table exists and has the correct structure
-- If not, create or modify it

CREATE TABLE IF NOT EXISTS vehicles (
    vehicle_id BIGINT NOT NULL AUTO_INCREMENT,
    transport_id BIGINT NOT NULL,
    
    -- Vehicle identification
    type ENUM('motorcycle', 'van', 'truck_small', 'truck_large', 'other') NOT NULL COMMENT 'Vehicle type',
    model VARCHAR(100) NOT NULL COMMENT 'Vehicle model/name',
    license_plate VARCHAR(20) NOT NULL COMMENT 'Original license plate as entered by user',
    
    -- Normalized license plates (for search/validation)
    license_plate_norm VARCHAR(20) NOT NULL DEFAULT '' COMMENT 'Normalized: UPPER + no spaces',
    license_plate_compact VARCHAR(20) NOT NULL DEFAULT '' COMMENT 'Compact: remove all special characters',
    
    -- Capacity specifications
    capacity_kg DECIMAL(8,2) NOT NULL COMMENT 'Weight capacity in kg',
    capacity_m3 DECIMAL(6,2) DEFAULT NULL COMMENT 'Volume capacity in cubic meters',
    length_cm DECIMAL(7,2) DEFAULT NULL COMMENT 'Length in cm',
    width_cm DECIMAL(7,2) DEFAULT NULL COMMENT 'Width in cm',
    height_cm DECIMAL(7,2) DEFAULT NULL COMMENT 'Height in cm',
    
    -- Status and features
    status ENUM('ACTIVE', 'INACTIVE', 'UNDER_MAINTENANCE') NOT NULL DEFAULT 'ACTIVE',
    year SMALLINT DEFAULT NULL COMMENT 'Manufacturing year',
    color VARCHAR(50) DEFAULT NULL,
    has_tail_lift BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Has hydraulic tail lift',
    has_tools BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Has moving tools/equipment',
    
    -- Additional info
    image_url VARCHAR(255) DEFAULT NULL,
    description TEXT DEFAULT NULL,
    
    -- Audit fields
    created_by BIGINT DEFAULT NULL,
    updated_by BIGINT DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Primary key
    PRIMARY KEY (vehicle_id),
    
    -- Unique constraints for license plates
    UNIQUE KEY uk_vehicles_lp_norm (license_plate_norm),
    UNIQUE KEY uk_vehicles_lp_compact (license_plate_compact),
    
    -- Indexes for performance
    KEY idx_vehicles_transport_status (transport_id, status),
    KEY idx_vehicles_type (type),
    KEY idx_vehicles_year (year),
    
    -- Foreign keys
    CONSTRAINT fk_vehicles_transport
        FOREIGN KEY (transport_id) REFERENCES transports(transport_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_vehicles_created_by
        FOREIGN KEY (created_by) REFERENCES users(user_id)
        ON DELETE SET NULL,
    CONSTRAINT fk_vehicles_updated_by
        FOREIGN KEY (updated_by) REFERENCES users(user_id)
        ON DELETE SET NULL
        
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Vehicles managed by transport companies';

-- Ensure license plate normalization works on servers without generated column support
DROP TRIGGER IF EXISTS trg_vehicles_bi_normalize;
CREATE TRIGGER trg_vehicles_bi_normalize
BEFORE INSERT ON vehicles
FOR EACH ROW
SET NEW.license_plate_norm = REPLACE(UPPER(COALESCE(NEW.license_plate, '')), ' ', ''),
    NEW.license_plate_compact = REPLACE(
        REPLACE(REPLACE(UPPER(COALESCE(NEW.license_plate, '')), ' ', ''), '-', ''),
        '.',
        ''
    );

DROP TRIGGER IF EXISTS trg_vehicles_bu_normalize;
CREATE TRIGGER trg_vehicles_bu_normalize
BEFORE UPDATE ON vehicles
FOR EACH ROW
SET NEW.license_plate_norm = REPLACE(UPPER(COALESCE(NEW.license_plate, '')), ' ', ''),
    NEW.license_plate_compact = REPLACE(
        REPLACE(REPLACE(UPPER(COALESCE(NEW.license_plate, '')), ' ', ''), '-', ''),
        '.',
        ''
    );

-- Upgrade to computed columns whenever the server version supports it (MySQL 5.7+)
/*!50700 ALTER TABLE vehicles
    MODIFY COLUMN license_plate_norm VARCHAR(20) GENERATED ALWAYS AS (REPLACE(UPPER(license_plate), ' ', '')) STORED COMMENT 'Normalized: UPPER + no spaces',
    MODIFY COLUMN license_plate_compact VARCHAR(20) GENERATED ALWAYS AS (
        REPLACE(REPLACE(REPLACE(UPPER(license_plate), ' ', ''), '-', ''), '.', '')
    ) STORED COMMENT 'Compact: remove all special characters'
*/;

/*!50700 DROP TRIGGER IF EXISTS trg_vehicles_bi_normalize */;
/*!50700 DROP TRIGGER IF EXISTS trg_vehicles_bu_normalize */;

-- Create vehicle_pricing table only if it doesn't exist
CREATE TABLE IF NOT EXISTS vehicle_pricing (
    pricing_id BIGINT NOT NULL AUTO_INCREMENT,
    vehicle_id BIGINT NOT NULL,
    
    -- Pricing components
    base_price DECIMAL(12,0) NOT NULL COMMENT 'Base price in VND',
    price_per_km DECIMAL(12,2) NOT NULL COMMENT 'Price per kilometer in VND',
    price_per_helper DECIMAL(12,0) DEFAULT NULL COMMENT 'Additional price per helper in VND',
    
    -- Time-based pricing
    valid_from DATETIME NOT NULL COMMENT 'Price validity start date',
    valid_to DATETIME DEFAULT NULL COMMENT 'Price validity end date (NULL = indefinite)',
    
    -- Audit fields
    created_by BIGINT DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Primary key
    PRIMARY KEY (pricing_id),
    
    -- Indexes
    KEY idx_vehicle_pricing_vehicle (vehicle_id),
    KEY idx_vehicle_pricing_validity (valid_from, valid_to),
    
    -- Foreign keys
    CONSTRAINT fk_vehicle_pricing_vehicle
        FOREIGN KEY (vehicle_id) REFERENCES vehicles(vehicle_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_vehicle_pricing_created_by
        FOREIGN KEY (created_by) REFERENCES users(user_id)
        ON DELETE SET NULL
        
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Time-versioned pricing for vehicles';

-- Only insert sample data if tables are empty
INSERT INTO vehicles (transport_id, type, model, license_plate, capacity_kg, capacity_m3, status, year, color, has_tail_lift, has_tools, description)
SELECT
    t.transport_id,
    'truck_small' as type,
    'Toyota Dyna' as model,
    CONCAT('51G-', LPAD(t.transport_id * 1000 + 123, 5, '0')) as license_plate,
    1500.00 as capacity_kg,
    8.00 as capacity_m3,
    'ACTIVE' as status,
    2020 as year,
    'White' as color,
    FALSE as has_tail_lift,
    TRUE as has_tools,
    'Standard small truck for city delivery' as description
FROM transports t
WHERE t.verification_status = 'APPROVED'
  AND NOT EXISTS (SELECT 1 FROM vehicles v WHERE v.transport_id = t.transport_id)
  AND (SELECT COUNT(*) FROM vehicles) = 0  -- Only if vehicles table is empty
    LIMIT 10;

-- Success message
SELECT 'Vehicles table structure verified and fixed!' as result;
