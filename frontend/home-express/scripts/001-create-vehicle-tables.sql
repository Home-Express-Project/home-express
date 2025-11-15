-- ============================================================================
-- VEHICLE MANAGEMENT SCHEMA
-- Member 3: Vehicle, Pricing & Estimation System
-- ============================================================================

-- Vehicle table
CREATE TABLE IF NOT EXISTS vehicles (
  vehicle_id SERIAL PRIMARY KEY,
  transport_id INTEGER NOT NULL,
  
  -- Vehicle info
  type VARCHAR(50) NOT NULL CHECK (type IN ('motorcycle', 'van', 'truck_small', 'truck_large', 'other')),
  model VARCHAR(100) NOT NULL,
  license_plate VARCHAR(20) NOT NULL UNIQUE,
  
  -- Capacity
  capacity_kg DECIMAL(10,2) NOT NULL,
  capacity_m3 DECIMAL(10,2),
  
  -- Status
  status VARCHAR(20) NOT NULL DEFAULT 'available' CHECK (status IN ('available', 'in_use', 'maintenance', 'inactive')),
  
  -- Features
  year INTEGER,
  color VARCHAR(50),
  has_tail_lift BOOLEAN DEFAULT FALSE,
  has_tools BOOLEAN DEFAULT FALSE,
  description TEXT,
  image_url TEXT,
  
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  
  CONSTRAINT fk_transport FOREIGN KEY (transport_id) REFERENCES transports(transport_id) ON DELETE CASCADE
);

CREATE INDEX idx_vehicles_transport ON vehicles(transport_id);
CREATE INDEX idx_vehicles_status ON vehicles(status);
CREATE INDEX idx_vehicles_license_plate ON vehicles(license_plate);

-- Vehicle pricing table
CREATE TABLE IF NOT EXISTS vehicle_pricing (
  pricing_id SERIAL PRIMARY KEY,
  transport_id INTEGER NOT NULL,
  vehicle_id INTEGER NOT NULL,
  
  base_price DECIMAL(12,2) NOT NULL,
  
  -- Tiered distance pricing
  per_km_first_4km DECIMAL(10,2) NOT NULL,
  per_km_5_to_40km DECIMAL(10,2) NOT NULL,
  per_km_after_40km DECIMAL(10,2) NOT NULL,
  
  -- Multipliers
  peak_hour_multiplier DECIMAL(4,2) DEFAULT 1.0,
  weekend_multiplier DECIMAL(4,2) DEFAULT 1.0,
  holiday_multiplier DECIMAL(4,2) DEFAULT 1.0,
  
  -- Floor fees
  no_elevator_fee DECIMAL(10,2) DEFAULT 0,
  elevator_discount DECIMAL(10,2) DEFAULT 0,
  
  is_active BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  
  CONSTRAINT fk_vehicle_pricing_transport FOREIGN KEY (transport_id) REFERENCES transports(transport_id) ON DELETE CASCADE,
  CONSTRAINT fk_vehicle_pricing_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(vehicle_id) ON DELETE CASCADE,
  CONSTRAINT unique_vehicle_pricing UNIQUE (vehicle_id)
);

CREATE INDEX idx_vehicle_pricing_transport ON vehicle_pricing(transport_id);
CREATE INDEX idx_vehicle_pricing_vehicle ON vehicle_pricing(vehicle_id);

-- Category pricing table
CREATE TABLE IF NOT EXISTS category_pricing (
  pricing_id SERIAL PRIMARY KEY,
  transport_id INTEGER NOT NULL,
  category_id INTEGER NOT NULL,
  
  price_per_unit DECIMAL(12,2) NOT NULL,
  
  fragile_multiplier DECIMAL(4,2) DEFAULT 1.0,
  disassembly_multiplier DECIMAL(4,2) DEFAULT 1.0,
  heavy_multiplier DECIMAL(4,2) DEFAULT 1.0,
  
  min_price DECIMAL(12,2),
  max_price DECIMAL(12,2),
  
  is_active BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  
  CONSTRAINT fk_category_pricing_transport FOREIGN KEY (transport_id) REFERENCES transports(transport_id) ON DELETE CASCADE,
  CONSTRAINT fk_category_pricing_category FOREIGN KEY (category_id) REFERENCES categories(category_id) ON DELETE CASCADE,
  CONSTRAINT unique_category_pricing UNIQUE (transport_id, category_id)
);

CREATE INDEX idx_category_pricing_transport ON category_pricing(transport_id);
CREATE INDEX idx_category_pricing_category ON category_pricing(category_id);

-- Transport settings table
CREATE TABLE IF NOT EXISTS transport_settings (
  setting_id SERIAL PRIMARY KEY,
  transport_id INTEGER NOT NULL UNIQUE,
  
  -- Notifications
  email_notifications BOOLEAN DEFAULT TRUE,
  
  -- Service area
  service_radius_km INTEGER DEFAULT 50,
  
  -- Pricing
  minimum_job_value DECIMAL(12,2) DEFAULT 100000,
  
  -- Automation
  auto_accept_jobs BOOLEAN DEFAULT FALSE,
  
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  
  CONSTRAINT fk_transport_settings FOREIGN KEY (transport_id) REFERENCES transports(transport_id) ON DELETE CASCADE
);

CREATE INDEX idx_transport_settings_transport ON transport_settings(transport_id);

-- Jobs table (for bidding system)
CREATE TABLE IF NOT EXISTS jobs (
  job_id SERIAL PRIMARY KEY,
  booking_id INTEGER NOT NULL UNIQUE,
  customer_id INTEGER NOT NULL,
  
  -- Location
  pickup_address TEXT NOT NULL,
  delivery_address TEXT NOT NULL,
  distance_km DECIMAL(10,2),
  
  -- Requirements
  total_weight_kg DECIMAL(10,2),
  total_volume_m3 DECIMAL(10,2),
  requires_tail_lift BOOLEAN DEFAULT FALSE,
  requires_tools BOOLEAN DEFAULT FALSE,
  
  -- Building conditions
  pickup_floor INTEGER DEFAULT 1,
  pickup_has_elevator BOOLEAN DEFAULT TRUE,
  delivery_floor INTEGER DEFAULT 1,
  delivery_has_elevator BOOLEAN DEFAULT TRUE,
  
  -- Timing
  preferred_date TIMESTAMP NOT NULL,
  preferred_time_slot VARCHAR(20),
  
  -- Status
  status VARCHAR(20) NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN', 'BIDDING', 'ASSIGNED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
  
  -- Bidding
  bidding_expires_at TIMESTAMP,
  selected_bid_id INTEGER,
  
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  
  CONSTRAINT fk_job_booking FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON DELETE CASCADE,
  CONSTRAINT fk_job_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE
);

CREATE INDEX idx_jobs_status ON jobs(status);
CREATE INDEX idx_jobs_preferred_date ON jobs(preferred_date);
CREATE INDEX idx_jobs_distance ON jobs(distance_km);

-- Bids table
CREATE TABLE IF NOT EXISTS bids (
  bid_id SERIAL PRIMARY KEY,
  job_id INTEGER NOT NULL,
  transport_id INTEGER NOT NULL,
  vehicle_id INTEGER NOT NULL,
  
  -- Pricing
  base_price DECIMAL(12,2) NOT NULL,
  distance_price DECIMAL(12,2) NOT NULL,
  item_handling_price DECIMAL(12,2) NOT NULL,
  additional_services_price DECIMAL(12,2) DEFAULT 0,
  total_price DECIMAL(12,2) NOT NULL,
  
  -- Services
  includes_packaging BOOLEAN DEFAULT FALSE,
  includes_disassembly BOOLEAN DEFAULT FALSE,
  includes_insurance BOOLEAN DEFAULT FALSE,
  
  -- Timing
  estimated_duration_hours DECIMAL(4,1),
  proposed_start_time TIMESTAMP,
  
  -- Status
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED', 'WITHDRAWN', 'EXPIRED')),
  
  notes TEXT,
  
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  accepted_at TIMESTAMP,
  rejected_at TIMESTAMP,
  
  CONSTRAINT fk_bid_job FOREIGN KEY (job_id) REFERENCES jobs(job_id) ON DELETE CASCADE,
  CONSTRAINT fk_bid_transport FOREIGN KEY (transport_id) REFERENCES transports(transport_id) ON DELETE CASCADE,
  CONSTRAINT fk_bid_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(vehicle_id) ON DELETE CASCADE,
  CONSTRAINT unique_transport_job_bid UNIQUE (job_id, transport_id)
);

CREATE INDEX idx_bids_job ON bids(job_id);
CREATE INDEX idx_bids_transport ON bids(transport_id);
CREATE INDEX idx_bids_status ON bids(status);

-- Job status history
CREATE TABLE IF NOT EXISTS job_status_history (
  history_id SERIAL PRIMARY KEY,
  job_id INTEGER NOT NULL,
  old_status VARCHAR(20),
  new_status VARCHAR(20) NOT NULL,
  changed_by INTEGER,
  changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  notes TEXT,
  
  CONSTRAINT fk_job_status_history FOREIGN KEY (job_id) REFERENCES jobs(job_id) ON DELETE CASCADE
);

CREATE INDEX idx_job_status_history_job ON job_status_history(job_id);

-- Scan sessions table (for admin review pipeline)
CREATE TABLE IF NOT EXISTS scan_sessions (
  session_id SERIAL PRIMARY KEY,
  customer_id INTEGER NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'PROCESSING', 'NEEDS_REVIEW', 'REVIEWED', 'QUOTED', 'PUBLISHED', 'FAILED')),
  
  -- Images
  image_urls TEXT[],
  image_count INTEGER DEFAULT 0,
  
  -- AI Detection results
  detection_results JSONB,
  average_confidence DECIMAL(4,3),
  
  -- Extracted items
  items JSONB,
  
  -- System estimation
  estimated_price DECIMAL(12,2),
  estimated_weight_kg DECIMAL(10,2),
  estimated_volume_m3 DECIMAL(10,2),
  
  -- Admin review
  reviewed_by INTEGER,
  reviewed_at TIMESTAMP,
  review_notes TEXT,
  
  -- Force quote
  forced_quote_price DECIMAL(12,2),
  forced_quote_by INTEGER,
  forced_quote_at TIMESTAMP,
  
  -- Publishing
  published_at TIMESTAMP,
  bidding_expires_at TIMESTAMP,
  
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  
  CONSTRAINT fk_scan_session_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE
);

CREATE INDEX idx_scan_sessions_customer ON scan_sessions(customer_id);
CREATE INDEX idx_scan_sessions_status ON scan_sessions(status);

COMMENT ON TABLE vehicles IS 'Transport company vehicles with capacity and features';
COMMENT ON TABLE vehicle_pricing IS 'Pricing configuration per vehicle';
COMMENT ON TABLE category_pricing IS 'Pricing configuration per item category';
COMMENT ON TABLE transport_settings IS 'Transport company preferences and settings';
COMMENT ON TABLE jobs IS 'Jobs available for bidding by transporters';
COMMENT ON TABLE bids IS 'Bids submitted by transporters for jobs';
COMMENT ON TABLE scan_sessions IS 'Customer scan sessions for admin review';
