-- Backfill core customer, catalog, and booking tables so later migrations have valid dependencies.
-- This allows V20251103+ scripts to reference bookings, customers, and product metadata.

CREATE TABLE IF NOT EXISTS customers (
    customer_id BIGINT NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    address TEXT DEFAULT NULL,
    date_of_birth DATE DEFAULT NULL,
    avatar_url TEXT DEFAULT NULL,
    preferred_language VARCHAR(10) DEFAULT 'vi',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (customer_id),
    KEY idx_customers_phone (phone),
    CONSTRAINT fk_customers_users
        FOREIGN KEY (customer_id) REFERENCES users(user_id)
        ON DELETE CASCADE,
    CONSTRAINT chk_customers_phone_vn
        CHECK (phone REGEXP '^0[1-9][0-9]{8}$')
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS customer_settings (
    customer_id BIGINT NOT NULL,
    language VARCHAR(10) NOT NULL DEFAULT 'vi',
    email_notifications TINYINT(1) NOT NULL DEFAULT 1,
    booking_updates TINYINT(1) NOT NULL DEFAULT 1,
    quotation_alerts TINYINT(1) NOT NULL DEFAULT 1,
    promotions TINYINT(1) NOT NULL DEFAULT 0,
    newsletter TINYINT(1) NOT NULL DEFAULT 0,
    profile_visibility ENUM('public','private') NOT NULL DEFAULT 'public',
    show_phone TINYINT(1) NOT NULL DEFAULT 1,
    show_email TINYINT(1) NOT NULL DEFAULT 0,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (customer_id),
    CONSTRAINT fk_customer_settings_customer
        FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS categories (
    category_id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    name_en VARCHAR(100) DEFAULT NULL,
    description TEXT DEFAULT NULL,
    icon VARCHAR(50) DEFAULT NULL,
    default_weight_kg DECIMAL(8,2) DEFAULT NULL,
    default_volume_m3 DECIMAL(6,2) DEFAULT NULL,
    default_length_cm DECIMAL(8,2) DEFAULT NULL,
    default_width_cm DECIMAL(8,2) DEFAULT NULL,
    default_height_cm DECIMAL(8,2) DEFAULT NULL,
    is_fragile_default BOOLEAN DEFAULT FALSE,
    requires_disassembly_default BOOLEAN DEFAULT FALSE,
    display_order INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (category_id),
    UNIQUE KEY uk_categories_name (name),
    KEY idx_categories_display (display_order),
    KEY idx_categories_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS sizes (
    size_id BIGINT NOT NULL AUTO_INCREMENT,
    category_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    weight_kg DECIMAL(8,2) DEFAULT NULL,
    height_cm DECIMAL(8,2) DEFAULT NULL,
    width_cm DECIMAL(8,2) DEFAULT NULL,
    depth_cm DECIMAL(8,2) DEFAULT NULL,
    price_multiplier DECIMAL(4,2) NOT NULL DEFAULT 1.00,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (size_id),
    UNIQUE KEY uk_sizes_category_name (category_id, name),
    KEY idx_sizes_category (category_id),
    CONSTRAINT fk_sizes_category
        FOREIGN KEY (category_id) REFERENCES categories(category_id)
        ON DELETE CASCADE,
    CONSTRAINT chk_sizes_dims
        CHECK (
            (weight_kg IS NULL OR weight_kg >= 0) AND
            (height_cm IS NULL OR height_cm > 0) AND
            (width_cm IS NULL OR width_cm > 0) AND
            (depth_cm IS NULL OR depth_cm > 0)
        ),
    CONSTRAINT chk_sizes_multiplier_bounds
        CHECK (price_multiplier >= 0.10 AND price_multiplier <= 10.00)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Standardized item sizes per category';

CREATE TABLE IF NOT EXISTS vn_provinces (
    province_code VARCHAR(6) NOT NULL,
    province_name VARCHAR(191) NOT NULL,
    codename VARCHAR(100),
    division_type VARCHAR(50),
    phone_code VARCHAR(10),
    PRIMARY KEY (province_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Vietnam provinces and centrally-run cities';

CREATE TABLE IF NOT EXISTS vn_districts (
    district_code VARCHAR(6) NOT NULL,
    district_name VARCHAR(191) NOT NULL,
    codename VARCHAR(100),
    division_type VARCHAR(50),
    short_codename VARCHAR(100),
    province_code VARCHAR(6) NOT NULL,
    PRIMARY KEY (district_code),
    KEY idx_vn_districts_province (province_code),
    CONSTRAINT fk_vn_districts_province
        FOREIGN KEY (province_code)
        REFERENCES vn_provinces (province_code)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Vietnam districts referencing provinces';

CREATE TABLE IF NOT EXISTS vn_wards (
    ward_code VARCHAR(6) NOT NULL,
    ward_name VARCHAR(191) NOT NULL,
    codename VARCHAR(100),
    division_type VARCHAR(50),
    short_codename VARCHAR(100),
    district_code VARCHAR(6) NOT NULL,
    PRIMARY KEY (ward_code),
    KEY idx_vn_wards_district (district_code),
    CONSTRAINT fk_vn_wards_district
        FOREIGN KEY (district_code)
        REFERENCES vn_districts (district_code)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Vietnam wards referencing districts';

CREATE TABLE IF NOT EXISTS bookings (
    booking_id BIGINT NOT NULL AUTO_INCREMENT,
    customer_id BIGINT NOT NULL,
    transport_id BIGINT DEFAULT NULL,
    pickup_address TEXT NOT NULL,
    pickup_latitude DECIMAL(10,8) DEFAULT NULL,
    pickup_longitude DECIMAL(11,8) DEFAULT NULL,
    pickup_floor INT DEFAULT NULL,
    pickup_has_elevator BOOLEAN DEFAULT FALSE,
    pickup_province_code VARCHAR(6) DEFAULT NULL,
    pickup_district_code VARCHAR(6) DEFAULT NULL,
    pickup_ward_code VARCHAR(6) DEFAULT NULL,
    delivery_address TEXT NOT NULL,
    delivery_latitude DECIMAL(10,8) DEFAULT NULL,
    delivery_longitude DECIMAL(11,8) DEFAULT NULL,
    delivery_floor INT DEFAULT NULL,
    delivery_has_elevator BOOLEAN DEFAULT FALSE,
    delivery_province_code VARCHAR(6) DEFAULT NULL,
    delivery_district_code VARCHAR(6) DEFAULT NULL,
    delivery_ward_code VARCHAR(6) DEFAULT NULL,
    preferred_date DATE NOT NULL,
    preferred_time_slot ENUM('MORNING', 'AFTERNOON', 'EVENING') DEFAULT NULL,
    actual_start_time DATETIME DEFAULT NULL,
    actual_end_time DATETIME DEFAULT NULL,
    distance_km DECIMAL(8,2) DEFAULT NULL,
    distance_source ENUM('GOOGLE', 'MAPBOX', 'OSRM', 'MANUAL') DEFAULT NULL,
    distance_calculated_at DATETIME DEFAULT NULL,
    estimated_price DECIMAL(12,0) DEFAULT NULL,
    final_price DECIMAL(12,0) DEFAULT NULL,
    status ENUM('PENDING', 'QUOTED', 'CONFIRMED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED') DEFAULT 'PENDING',
    notes TEXT DEFAULT NULL,
    special_requirements TEXT DEFAULT NULL,
    cancelled_by BIGINT DEFAULT NULL,
    cancellation_reason TEXT DEFAULT NULL,
    cancelled_at DATETIME DEFAULT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (booking_id),
    KEY idx_bookings_customer (customer_id),
    KEY idx_bookings_transport (transport_id),
    KEY idx_bookings_status (status),
    KEY idx_bookings_date (preferred_date),
    KEY idx_bookings_created (created_at),
    KEY idx_bookings_customer_status (customer_id, status),
    KEY idx_bookings_transport_status (transport_id, status),
    KEY idx_bookings_pickup_province (pickup_province_code),
    KEY idx_bookings_delivery_province (delivery_province_code),
    KEY idx_bookings_customer_date (customer_id, preferred_date),
    KEY idx_bookings_transport_date_status (transport_id, preferred_date, status),
    CONSTRAINT fk_bookings_customer
        FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
    CONSTRAINT fk_bookings_transport
        FOREIGN KEY (transport_id) REFERENCES transports(transport_id),
    CONSTRAINT fk_bookings_cancelled_by
        FOREIGN KEY (cancelled_by) REFERENCES users(user_id)
        ON DELETE SET NULL,
    CONSTRAINT fk_bookings_pickup_province
        FOREIGN KEY (pickup_province_code) REFERENCES vn_provinces(province_code)
        ON DELETE SET NULL,
    CONSTRAINT fk_bookings_pickup_district
        FOREIGN KEY (pickup_district_code) REFERENCES vn_districts(district_code)
        ON DELETE SET NULL,
    CONSTRAINT fk_bookings_pickup_ward
        FOREIGN KEY (pickup_ward_code) REFERENCES vn_wards(ward_code)
        ON DELETE SET NULL,
    CONSTRAINT fk_bookings_delivery_province
        FOREIGN KEY (delivery_province_code) REFERENCES vn_provinces(province_code)
        ON DELETE SET NULL,
    CONSTRAINT fk_bookings_delivery_district
        FOREIGN KEY (delivery_district_code) REFERENCES vn_districts(district_code)
        ON DELETE SET NULL,
    CONSTRAINT fk_bookings_delivery_ward
        FOREIGN KEY (delivery_ward_code) REFERENCES vn_wards(ward_code)
        ON DELETE SET NULL,
    CONSTRAINT chk_bookings_pickup_lat
        CHECK (pickup_latitude IS NULL OR (pickup_latitude BETWEEN -90 AND 90)),
    CONSTRAINT chk_bookings_pickup_lng
        CHECK (pickup_longitude IS NULL OR (pickup_longitude BETWEEN -180 AND 180)),
    CONSTRAINT chk_bookings_delivery_lat
        CHECK (delivery_latitude IS NULL OR (delivery_latitude BETWEEN -90 AND 90)),
    CONSTRAINT chk_bookings_delivery_lng
        CHECK (delivery_longitude IS NULL OR (delivery_longitude BETWEEN -180 AND 180)),
    CONSTRAINT chk_bookings_floors
        CHECK (
            (pickup_floor IS NULL OR pickup_floor >= 0) AND
            (delivery_floor IS NULL OR delivery_floor >= 0)
        ),
    CONSTRAINT chk_bookings_prices_positive
        CHECK (
            (estimated_price IS NULL OR estimated_price >= 0) AND
            (final_price IS NULL OR final_price >= 0)
        )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Customer bookings with Vietnamese address granularity';

CREATE TABLE IF NOT EXISTS transport_list (
    id BIGINT NOT NULL AUTO_INCREMENT,
    booking_id BIGINT NOT NULL,
    transport_id BIGINT NOT NULL,
    notified_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    notification_method ENUM('EMAIL', 'SMS', 'PUSH') DEFAULT 'EMAIL',
    has_viewed BOOLEAN DEFAULT FALSE,
    viewed_at DATETIME DEFAULT NULL,
    has_responded BOOLEAN DEFAULT FALSE,
    responded_at DATETIME DEFAULT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_transport_list_booking_transport (booking_id, transport_id),
    KEY idx_transport_list_booking (booking_id),
    KEY idx_transport_list_transport (transport_id),
    CONSTRAINT fk_transport_list_booking
        FOREIGN KEY (booking_id) REFERENCES bookings(booking_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_transport_list_transport
        FOREIGN KEY (transport_id) REFERENCES transports(transport_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Tracks transports notified for each booking';

CREATE TABLE IF NOT EXISTS booking_items (
    item_id BIGINT NOT NULL AUTO_INCREMENT,
    booking_id BIGINT NOT NULL,
    category_id BIGINT DEFAULT NULL,
    size_id BIGINT DEFAULT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT DEFAULT NULL,
    quantity INT NOT NULL DEFAULT 1,
    weight_kg DECIMAL(8,2) DEFAULT NULL,
    height_cm DECIMAL(8,2) DEFAULT NULL,
    width_cm DECIMAL(8,2) DEFAULT NULL,
    depth_cm DECIMAL(8,2) DEFAULT NULL,
    is_fragile BOOLEAN DEFAULT FALSE,
    requires_disassembly BOOLEAN DEFAULT FALSE,
    estimated_disassembly_time INT DEFAULT NULL,
    unit_price DECIMAL(12,0) DEFAULT NULL,
    total_price DECIMAL(12,0) DEFAULT NULL,
    ai_metadata JSON DEFAULT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (item_id),
    KEY idx_booking_items_booking (booking_id),
    KEY idx_booking_items_category (category_id),
    KEY idx_booking_items_size (size_id),
    CONSTRAINT fk_booking_items_booking
        FOREIGN KEY (booking_id) REFERENCES bookings(booking_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_booking_items_category
        FOREIGN KEY (category_id) REFERENCES categories(category_id)
        ON DELETE SET NULL,
    CONSTRAINT fk_booking_items_size
        FOREIGN KEY (size_id) REFERENCES sizes(size_id)
        ON DELETE SET NULL,
    CONSTRAINT chk_booking_items_positive_values
        CHECK (
            quantity > 0 AND
            (weight_kg IS NULL OR weight_kg >= 0) AND
            (unit_price IS NULL OR unit_price >= 0) AND
            (total_price IS NULL OR total_price >= 0)
        )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS booking_status_history (
    id BIGINT NOT NULL AUTO_INCREMENT,
    booking_id BIGINT NOT NULL,
    old_status ENUM('PENDING', 'QUOTED', 'CONFIRMED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED') DEFAULT NULL,
    new_status ENUM('PENDING', 'QUOTED', 'CONFIRMED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED') NOT NULL,
    changed_by BIGINT DEFAULT NULL,
    changed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_booking_status_history_booking (booking_id, changed_at),
    CONSTRAINT fk_booking_status_history_booking
        FOREIGN KEY (booking_id) REFERENCES bookings(booking_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_booking_status_history_user
        FOREIGN KEY (changed_by) REFERENCES users(user_id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Audit trail for booking status transitions';

CREATE TABLE IF NOT EXISTS contracts (
    contract_id BIGINT NOT NULL AUTO_INCREMENT,
    booking_id BIGINT NOT NULL,
    quotation_id BIGINT NOT NULL,
    contract_number VARCHAR(50) NOT NULL,
    terms_and_conditions TEXT NOT NULL,
    total_amount DECIMAL(12,0) NOT NULL,
    agreed_price_vnd BIGINT NOT NULL DEFAULT 0,
    deposit_required_vnd BIGINT NOT NULL DEFAULT 0,
    deposit_due_at DATETIME DEFAULT NULL,
    balance_due_at DATETIME DEFAULT NULL,
    customer_signed BOOLEAN DEFAULT FALSE,
    customer_signed_at DATETIME DEFAULT NULL,
    customer_signature_url TEXT DEFAULT NULL,
    customer_signed_ip VARCHAR(45) DEFAULT NULL,
    transport_signed BOOLEAN DEFAULT FALSE,
    transport_signed_at DATETIME DEFAULT NULL,
    transport_signature_url TEXT DEFAULT NULL,
    transport_signed_ip VARCHAR(45) DEFAULT NULL,
    status ENUM('DRAFT', 'ACTIVE', 'COMPLETED', 'TERMINATED') DEFAULT 'DRAFT',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (contract_id),
    UNIQUE KEY uk_contracts_booking (booking_id),
    UNIQUE KEY uk_contracts_number (contract_number),
    KEY idx_contracts_status (status),
    CONSTRAINT fk_contracts_booking
        FOREIGN KEY (booking_id) REFERENCES bookings(booking_id)
        ON DELETE CASCADE,
    CONSTRAINT chk_contracts_amounts_positive
        CHECK (total_amount > 0 AND agreed_price_vnd >= 0),
    CONSTRAINT chk_contracts_deposit_valid
        CHECK (deposit_required_vnd >= 0 AND deposit_required_vnd <= agreed_price_vnd)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Contracts capturing agreed commercial terms';
