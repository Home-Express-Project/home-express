-- ============================================================================
-- Seed sample vehicles for existing transports
-- ============================================================================
-- Purpose: Add sample vehicles so estimation API can work immediately
-- Note: This is for development/testing. Remove or modify for production.
-- ============================================================================

-- Insert sample vehicles for transports that are verified
INSERT INTO vehicles (transport_id, type, model, license_plate, capacity_kg, capacity_m3, length_cm, width_cm, height_cm, status, year, color, has_tail_lift, has_tools, description)
SELECT 
    t.transport_id,
    'truck_small',
    'Hyundai Porter H150',
    CONCAT('51H-', LPAD(t.transport_id, 5, '0')),
    1500.00,
    8.00,
    320.00,
    160.00,
    180.00,
    'ACTIVE',
    2021,
    'White',
    FALSE,
    TRUE,
    'Small truck suitable for household moving'
FROM transports t
WHERE t.verification_status = 'APPROVED'
  AND NOT EXISTS (
      SELECT 1 FROM vehicles v WHERE v.transport_id = t.transport_id
  )
LIMIT 3;

-- Insert medium trucks
INSERT INTO vehicles (transport_id, type, model, license_plate, capacity_kg, capacity_m3, length_cm, width_cm, height_cm, status, year, color, has_tail_lift, has_tools, description)
SELECT 
    t.transport_id,
    'truck_large',
    'Isuzu QKR77',
    CONCAT('51C-', LPAD(t.transport_id, 5, '0')),
    2500.00,
    15.00,
    420.00,
    190.00,
    200.00,
    'ACTIVE',
    2022,
    'Blue',
    TRUE,
    TRUE,
    'Large truck with hydraulic lift for heavy items'
FROM transports t
WHERE t.verification_status = 'APPROVED'
  AND NOT EXISTS (
      SELECT 1 FROM vehicles v WHERE v.transport_id = t.transport_id LIMIT 1
  )
LIMIT 2;

-- Insert vans
INSERT INTO vehicles (transport_id, type, model, license_plate, capacity_kg, capacity_m3, length_cm, width_cm, height_cm, status, year, color, has_tail_lift, has_tools, description)
SELECT 
    t.transport_id,
    'van',
    'Ford Transit',
    CONCAT('59A-', LPAD(t.transport_id, 5, '0')),
    1000.00,
    6.00,
    280.00,
    150.00,
    160.00,
    'ACTIVE',
    2020,
    'Silver',
    FALSE,
    TRUE,
    'Van suitable for small to medium moves'
FROM transports t
WHERE t.verification_status = 'APPROVED'
  AND (SELECT COUNT(*) FROM vehicles v WHERE v.transport_id = t.transport_id) < 2
LIMIT 5;

-- Add pricing for all vehicles (using simple pricing structure)
INSERT INTO vehicle_pricing (vehicle_id, base_price, price_per_km, price_per_helper, valid_from)
SELECT 
    v.vehicle_id,
    CASE 
        WHEN v.type = 'motorcycle' THEN 50000
        WHEN v.type = 'van' THEN 150000
        WHEN v.type = 'truck_small' THEN 250000
        WHEN v.type = 'truck_large' THEN 400000
        ELSE 200000
    END as base_price,
    CASE 
        WHEN v.type = 'motorcycle' THEN 3000
        WHEN v.type = 'van' THEN 5000
        WHEN v.type = 'truck_small' THEN 8000
        WHEN v.type = 'truck_large' THEN 12000
        ELSE 6000
    END as price_per_km,
    100000 as price_per_helper,
    NOW() as valid_from
FROM vehicles v
WHERE NOT EXISTS (
    SELECT 1 FROM vehicle_pricing vp WHERE vp.vehicle_id = v.vehicle_id
);

