-- Ensure vn_* lookup tables include metadata columns required by seed migration

SET @schema := DATABASE();

-- vn_provinces.codename
SET @sql := (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = @schema
              AND TABLE_NAME = 'vn_provinces'
              AND COLUMN_NAME = 'codename'
        ),
        'DO 0',
        'ALTER TABLE vn_provinces ADD COLUMN codename VARCHAR(100) NULL AFTER province_name'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- vn_provinces.division_type
SET @sql := (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = @schema
              AND TABLE_NAME = 'vn_provinces'
              AND COLUMN_NAME = 'division_type'
        ),
        'DO 0',
        'ALTER TABLE vn_provinces ADD COLUMN division_type VARCHAR(50) NULL AFTER codename'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- vn_provinces.phone_code
SET @sql := (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = @schema
              AND TABLE_NAME = 'vn_provinces'
              AND COLUMN_NAME = 'phone_code'
        ),
        'DO 0',
        'ALTER TABLE vn_provinces ADD COLUMN phone_code VARCHAR(10) NULL AFTER division_type'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- vn_districts.codename
SET @sql := (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = @schema
              AND TABLE_NAME = 'vn_districts'
              AND COLUMN_NAME = 'codename'
        ),
        'DO 0',
        'ALTER TABLE vn_districts ADD COLUMN codename VARCHAR(100) NULL AFTER district_name'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- vn_districts.division_type
SET @sql := (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = @schema
              AND TABLE_NAME = 'vn_districts'
              AND COLUMN_NAME = 'division_type'
        ),
        'DO 0',
        'ALTER TABLE vn_districts ADD COLUMN division_type VARCHAR(50) NULL AFTER codename'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- vn_districts.short_codename
SET @sql := (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = @schema
              AND TABLE_NAME = 'vn_districts'
              AND COLUMN_NAME = 'short_codename'
        ),
        'DO 0',
        'ALTER TABLE vn_districts ADD COLUMN short_codename VARCHAR(100) NULL AFTER division_type'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- vn_wards.codename
SET @sql := (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = @schema
              AND TABLE_NAME = 'vn_wards'
              AND COLUMN_NAME = 'codename'
        ),
        'DO 0',
        'ALTER TABLE vn_wards ADD COLUMN codename VARCHAR(100) NULL AFTER ward_name'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- vn_wards.division_type
SET @sql := (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = @schema
              AND TABLE_NAME = 'vn_wards'
              AND COLUMN_NAME = 'division_type'
        ),
        'DO 0',
        'ALTER TABLE vn_wards ADD COLUMN division_type VARCHAR(50) NULL AFTER codename'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- vn_wards.short_codename
SET @sql := (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = @schema
              AND TABLE_NAME = 'vn_wards'
              AND COLUMN_NAME = 'short_codename'
        ),
        'DO 0',
        'ALTER TABLE vn_wards ADD COLUMN short_codename VARCHAR(100) NULL AFTER division_type'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
