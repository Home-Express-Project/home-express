-- Create core lookup tables for Vietnamese administrative divisions

CREATE TABLE IF NOT EXISTS vn_provinces (
    province_code VARCHAR(6) NOT NULL,
    province_name VARCHAR(191) NOT NULL,
    codename VARCHAR(100),
    division_type VARCHAR(50),
    phone_code VARCHAR(10),
    PRIMARY KEY (province_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Vietnam provinces and centrally-run cities sourced from provinces.open-api.vn';

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
COMMENT='Vietnam districts and district-level towns sourced from provinces.open-api.vn';

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
COMMENT='Vietnam wards and communes sourced from provinces.open-api.vn';
