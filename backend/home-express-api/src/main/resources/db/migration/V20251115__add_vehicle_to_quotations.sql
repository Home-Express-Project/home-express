-- V20251115__add_vehicle_to_quotations.sql
-- Add vehicle information to quotations so customers know which vehicle will be used

ALTER TABLE quotations
ADD COLUMN vehicle_id BIGINT COMMENT 'Xe được chọn cho báo giá này';

-- Foreign key to vehicles table
ALTER TABLE quotations
ADD CONSTRAINT fk_quotations_vehicle
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(vehicle_id)
    ON DELETE SET NULL;

-- Index for querying quotations by vehicle
CREATE INDEX idx_quotations_vehicle ON quotations(vehicle_id);
