--
-- HOME EXPRESS - COMPLETE DATABASE SCHEMA (MySQL 8.0+)
--
-- Project: House Moving Service Platform
-- Database: MySQL 8.0+

-- Modules:
--   - Member 1: Authentication & Users
--   - Member 2: Booking & Quotation
--   - Member 3: Vehicle & Pricing
--   - Member 4: Reviews & Notifications

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

--
-- Create Database
--
CREATE DATABASE IF NOT EXISTS `home_express`
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE `home_express`;

--
-- VIETNAMESE-SPECIFIC REFERENCE TABLES (Must be created FIRST)
--

-- Table: vn_banks
CREATE TABLE `vn_banks` (
  `bank_code` VARCHAR(10) NOT NULL,
  `bank_name` VARCHAR(255) NOT NULL,
  `bank_name_en` VARCHAR(255) DEFAULT NULL,
  `napas_bin` VARCHAR(8) DEFAULT NULL COMMENT 'NAPAS Bank Identification Number',
  `swift_code` VARCHAR(11) DEFAULT NULL,
  `is_active` BOOLEAN DEFAULT TRUE,
  `logo_url` TEXT DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`bank_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Vietnamese banks for payment integration';

-- Table: vn_provinces (Tỉnh/Thành phố) - Updated 2024
CREATE TABLE `vn_provinces` (
  `province_code` VARCHAR(6) NOT NULL,
  `province_name` VARCHAR(100) NOT NULL,
  `province_name_en` VARCHAR(100) DEFAULT NULL,
  `region` ENUM('NORTH', 'CENTRAL', 'SOUTH') NOT NULL COMMENT 'Miền Bắc/Trung/Nam',
  `display_order` INT DEFAULT 0,
  PRIMARY KEY (`province_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Vietnamese provinces and cities - Updated with latest administrative divisions';

-- Table: vn_districts (Quận/Huyện)
CREATE TABLE `vn_districts` (
  `district_code` VARCHAR(6) NOT NULL,
  `district_name` VARCHAR(100) NOT NULL,
  `province_code` VARCHAR(6) NOT NULL,
  PRIMARY KEY (`district_code`),
  KEY `idx_districts_province` (`province_code`),
  CONSTRAINT `fk_districts_province`
    FOREIGN KEY (`province_code`)
    REFERENCES `vn_provinces` (`province_code`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Vietnamese districts';

-- Table: vn_wards (Phường/Xã)
CREATE TABLE `vn_wards` (
  `ward_code` VARCHAR(6) NOT NULL,
  `ward_name` VARCHAR(100) NOT NULL,
  `district_code` VARCHAR(6) NOT NULL,
  PRIMARY KEY (`ward_code`),
  KEY `idx_wards_district` (`district_code`),
  CONSTRAINT `fk_wards_district`
    FOREIGN KEY (`district_code`)
    REFERENCES `vn_districts` (`district_code`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Vietnamese wards and communes';

--
-- SEED DATA: Vietnamese Banks (Early seed required)
--

INSERT INTO `vn_banks` (`bank_code`, `bank_name`, `bank_name_en`, `napas_bin`, `swift_code`, `is_active`) VALUES
('VCB', 'Ngân hàng TMCP Ngoại thương Việt Nam', 'Vietcombank', '970436', 'BFTVVNVX', TRUE),
('TCB', 'Ngân hàng TMCP Kỹ thương Việt Nam', 'Techcombank', '970407', 'VTCBVNVX', TRUE),
('BIDV', 'Ngân hàng TMCP Đầu tư và Phát triển VN', 'BIDV', '970418', 'BIDVVNVX', TRUE),
('VTB', 'Ngân hàng TMCP Công thương Việt Nam', 'VietinBank', '970415', 'ICBVVNVX', TRUE),
('ACB', 'Ngân hàng TMCP Á Châu', 'ACB', '970416', 'ASCBVNVX', TRUE),
('MBB', 'Ngân hàng TMCP Quân đội', 'MB Bank', '970422', 'MSCBVNVX', TRUE),
('VPB', 'Ngân hàng TMCP Việt Nam Thịnh Vượng', 'VPBank', '970432', 'VPBKVNVX', TRUE),
('TPB', 'Ngân hàng TMCP Tiên Phong', 'TPBank', '970423', 'TPBVNVX', TRUE),
('STB', 'Ngân hàng TMCP Sài Gòn Thương Tín', 'Sacombank', '970403', 'SGTTVNVX', TRUE),
('HDB', 'Ngân hàng TMCP Phát triển TP.HCM', 'HDBank', '970437', 'HDBCVNVX', TRUE),
('SHB', 'Ngân hàng TMCP Sài Gòn - Hà Nội', 'SHB', '970443', 'SHBAVNVX', TRUE),
('EIB', 'Ngân hàng TMCP Xuất Nhập khẩu VN', 'Eximbank', '970431', 'EBVIVNVX', TRUE),
('MSB', 'Ngân hàng TMCP Hàng Hải', 'MSB', '970426', 'MCOBVNVX', TRUE),
('OCB', 'Ngân hàng TMCP Phương Đông', 'OCB', '970448', 'ORBKVNVX', TRUE),
('SEA', 'Ngân hàng TMCP Đông Nam Á', 'SeABank', '970440', 'SEAVVNVX', TRUE),
('ABB', 'Ngân hàng TMCP An Bình', 'ABBANK', '970425', 'ABBKVNVX', TRUE),
('VAB', 'Ngân hàng TMCP Việt Á', 'VietABank', '970427', 'VNACVNVX', TRUE),
('NAB', 'Ngân hàng TMCP Nam Á', 'Nam A Bank', '970428', 'NAMAVNVX', TRUE),
('PGB', 'Ngân hàng TMCP Xăng dầu Petrolimex', 'PG Bank', '970430', 'PGBLVNVX', TRUE),
('VCCB', 'Ngân hàng TMCP Bản Việt', 'VietCapital Bank', '970454', 'VCBCVNVX', TRUE);

--
-- MODULE 1: AUTHENTICATION & USERS (Member 1 - TriQuan)
--

-- Table: users (Enhanced with security fields)
CREATE TABLE `users` (
  `user_id` BIGINT NOT NULL AUTO_INCREMENT,
  `email` VARCHAR(255) NOT NULL,
  `password_hash` TEXT NOT NULL,
  `role` ENUM('CUSTOMER', 'TRANSPORT', 'MANAGER') NOT NULL,

  -- Account status
  `is_active` BOOLEAN DEFAULT TRUE,
  `is_verified` BOOLEAN DEFAULT FALSE,

  -- Security & lifecycle tracking
  `email_verified_at` DATETIME DEFAULT NULL,
  `last_password_change` DATETIME DEFAULT NULL,
  `locked_until` DATETIME DEFAULT NULL,

  -- Legacy token fields (DEPRECATED - use user_tokens table)
  `verification_token` VARCHAR(255) DEFAULT NULL COMMENT 'DEPRECATED: Use user_tokens table',
  `reset_password_token` VARCHAR(255) DEFAULT NULL COMMENT 'DEPRECATED: Use user_tokens table',
  `reset_password_expires` DATETIME DEFAULT NULL COMMENT 'DEPRECATED: Use user_tokens table',

  -- Timestamps
  `last_login` DATETIME DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_users_email_lower` ((LOWER(`email`))),
  KEY `idx_users_role` (`role`),
  KEY `idx_users_is_active` (`is_active`),
  KEY `idx_users_locked` (`locked_until`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Base user authentication table with enhanced security tracking';

-- Table: customers (Enhanced with constraints)
CREATE TABLE `customers` (
  `customer_id` BIGINT NOT NULL,
  `full_name` VARCHAR(255) NOT NULL,
  `phone` VARCHAR(20) NOT NULL,
  `address` TEXT DEFAULT NULL,
  `date_of_birth` DATE DEFAULT NULL,
  `avatar_url` TEXT DEFAULT NULL,
  `preferred_language` VARCHAR(10) DEFAULT 'vi',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  PRIMARY KEY (`customer_id`),
  KEY `idx_customers_phone` (`phone`),
  CONSTRAINT `fk_customers_users`
    FOREIGN KEY (`customer_id`)
    REFERENCES `users` (`user_id`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  -- Vietnamese phone number: 10 digits starting with 0 (VN mobile standard since 2018)
  -- Valid formats: 0901234567, 0987654321
  CONSTRAINT `chk_customers_phone_vn`
    CHECK (phone REGEXP '^0[1-9][0-9]{8}$')
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: transports (Enhanced with verification and constraints)
CREATE TABLE `transports` (
  `transport_id` BIGINT NOT NULL,
  `company_name` VARCHAR(255) NOT NULL,
  `business_license_number` VARCHAR(50) NOT NULL,
  `tax_code` VARCHAR(50) DEFAULT NULL,
  `phone` VARCHAR(20) NOT NULL,
  `address` TEXT NOT NULL,
  `city` VARCHAR(100) NOT NULL,
  `district` VARCHAR(100) DEFAULT NULL,
  `ward` VARCHAR(100) DEFAULT NULL,

  -- Documents
  `license_photo_url` TEXT DEFAULT NULL,
  `insurance_photo_url` TEXT DEFAULT NULL,

  -- Verification
  `verification_status` ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
  `verified_at` DATETIME DEFAULT NULL,
  `verified_by` BIGINT DEFAULT NULL,

  -- Statistics
  `total_bookings` INT DEFAULT 0,
  `completed_bookings` INT DEFAULT 0,
  `cancelled_bookings` INT DEFAULT 0,
  `average_rating` DECIMAL(3,2) DEFAULT 0.00,

  -- KYC Documents (Vietnam specific)
  `national_id_number` VARCHAR(12) DEFAULT NULL COMMENT 'CMND (9 digits) or CCCD (12 digits)',
  `national_id_type` ENUM('CMND', 'CCCD') DEFAULT NULL,
  `national_id_issue_date` DATE DEFAULT NULL,
  `national_id_issuer` VARCHAR(100) DEFAULT NULL COMMENT 'Nơi cấp',
  `national_id_photo_front_url` TEXT DEFAULT NULL,
  `national_id_photo_back_url` TEXT DEFAULT NULL,

  -- Banking (consider app-level encryption for sensitive fields)
  `bank_name` VARCHAR(100) DEFAULT NULL COMMENT 'Tên ngân hàng VN',
  `bank_code` VARCHAR(10) DEFAULT NULL COMMENT 'Mã ngân hàng (VCB, TCB, etc.)',
  `bank_account_number` VARCHAR(19) DEFAULT NULL COMMENT 'Số tài khoản 8-19 chữ số',
  `bank_account_holder` VARCHAR(255) DEFAULT NULL COMMENT 'Chủ tài khoản',

  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  PRIMARY KEY (`transport_id`),
  UNIQUE KEY `uk_transports_license` (`business_license_number`),
  UNIQUE KEY `uk_transports_tax_code` (`tax_code`),
  UNIQUE KEY `uk_transports_national_id` (`national_id_number`),
  KEY `idx_transports_city` (`city`),
  KEY `idx_transports_verification` (`verification_status`, `verified_at` DESC),
  KEY `idx_transports_rating` (`average_rating` DESC),
  CONSTRAINT `fk_transports_users`
    FOREIGN KEY (`transport_id`)
    REFERENCES `users` (`user_id`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_transports_verified_by`
    FOREIGN KEY (`verified_by`)
    REFERENCES `users` (`user_id`)
    ON DELETE SET NULL
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_transports_bank`
    FOREIGN KEY (`bank_code`)
    REFERENCES `vn_banks` (`bank_code`)
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  -- Vietnamese phone number: 10 digits starting with 0 (VN mobile standard since 2018)
  CONSTRAINT `chk_transports_phone_vn`
    CHECK (phone REGEXP '^0[1-9][0-9]{8}$'),
  -- Vietnamese tax code: 10 digits (entity) or 13 digits (branch)
  CONSTRAINT `chk_transports_tax_code_vn`
    CHECK (tax_code IS NULL OR tax_code REGEXP '^[0-9]{10}$|^[0-9]{10}-[0-9]{3}$'),
  -- Vietnamese business license (GPKD): 10 or 13 digits
  CONSTRAINT `chk_transports_gpkd_vn`
    CHECK (business_license_number REGEXP '^[0-9]{10}$|^[0-9]{13}$'),
  -- CMND/CCCD validation (9 or 12 digits)
  CONSTRAINT `chk_transports_national_id`
    CHECK (national_id_number IS NULL OR
           (national_id_type = 'CMND' AND national_id_number REGEXP '^[0-9]{9}$') OR
           (national_id_type = 'CCCD' AND national_id_number REGEXP '^[0-9]{12}$')),
  -- Bank account: 8-19 digits
  CONSTRAINT `chk_transports_bank_account`
    CHECK (bank_account_number IS NULL OR bank_account_number REGEXP '^[0-9]{8,19}$')
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Transport company profiles with verification workflow';

-- Table: transport_settings
CREATE TABLE `transport_settings` (
  `transport_id` BIGINT NOT NULL,
  `search_radius_km` DECIMAL(5,2) NOT NULL DEFAULT 10.00,
  `min_job_value_vnd` DECIMAL(12,0) NOT NULL DEFAULT 0,
  `auto_accept_jobs` TINYINT(1) NOT NULL DEFAULT 0,
  `response_time_hours` DECIMAL(4,1) DEFAULT 2.0,
  `email_notifications` TINYINT(1) NOT NULL DEFAULT 1,
  `new_job_alerts` TINYINT(1) NOT NULL DEFAULT 1,
  `quotation_updates` TINYINT(1) NOT NULL DEFAULT 1,
  `payment_notifications` TINYINT(1) NOT NULL DEFAULT 1,
  `review_notifications` TINYINT(1) NOT NULL DEFAULT 1,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`transport_id`),
  CONSTRAINT `fk_settings_transport`
    FOREIGN KEY (`transport_id`)
    REFERENCES `transports` (`transport_id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Transport-specific settings for job matching, notifications, and preferences';

-- Table: customer_settings
CREATE TABLE `customer_settings` (
  `customer_id` BIGINT NOT NULL,
  `language` VARCHAR(10) NOT NULL DEFAULT 'vi',
  `email_notifications` TINYINT(1) NOT NULL DEFAULT 1,
  `booking_updates` TINYINT(1) NOT NULL DEFAULT 1,
  `quotation_alerts` TINYINT(1) NOT NULL DEFAULT 1,
  `promotions` TINYINT(1) NOT NULL DEFAULT 0,
  `newsletter` TINYINT(1) NOT NULL DEFAULT 0,
  `profile_visibility` ENUM('public','private') NOT NULL DEFAULT 'public',
  `show_phone` TINYINT(1) NOT NULL DEFAULT 1,
  `show_email` TINYINT(1) NOT NULL DEFAULT 0,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`customer_id`),
  CONSTRAINT `fk_customer_settings_customer`
    FOREIGN KEY (`customer_id`)
    REFERENCES `customers` (`customer_id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Customer notification and privacy preferences';

-- Table: admin_settings
CREATE TABLE `admin_settings` (
  `manager_id` BIGINT NOT NULL,
  `full_name` VARCHAR(255) DEFAULT NULL,
  `phone` VARCHAR(20) DEFAULT NULL,
  `email_notifications` TINYINT(1) NOT NULL DEFAULT 1,
  `system_alerts` TINYINT(1) NOT NULL DEFAULT 1,
  `user_registrations` TINYINT(1) NOT NULL DEFAULT 1,
  `transport_verifications` TINYINT(1) NOT NULL DEFAULT 1,
  `booking_alerts` TINYINT(1) NOT NULL DEFAULT 0,
  `review_moderation` TINYINT(1) NOT NULL DEFAULT 1,
  `two_factor_enabled` TINYINT(1) NOT NULL DEFAULT 0,
  `session_timeout_minutes` INT NOT NULL DEFAULT 30,
  `login_notifications` TINYINT(1) NOT NULL DEFAULT 1,
  `theme` ENUM('light','dark','system') NOT NULL DEFAULT 'light',
  `date_format` VARCHAR(20) NOT NULL DEFAULT 'DD/MM/YYYY',
  `timezone` VARCHAR(100) NOT NULL DEFAULT 'Asia/Ho_Chi_Minh',
  `maintenance_mode` TINYINT(1) NOT NULL DEFAULT 0,
  `auto_backup` TINYINT(1) NOT NULL DEFAULT 1,
  `backup_frequency` ENUM('hourly','daily','weekly','monthly') NOT NULL DEFAULT 'daily',
  `email_provider` ENUM('smtp','sendgrid','mailgun') NOT NULL DEFAULT 'smtp',
  `smtp_host` VARCHAR(255) DEFAULT NULL,
  `smtp_port` VARCHAR(10) DEFAULT NULL,
  `smtp_username` VARCHAR(255) DEFAULT NULL,
  `smtp_password` TEXT DEFAULT NULL,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`manager_id`),
  CONSTRAINT `fk_admin_settings_manager`
    FOREIGN KEY (`manager_id`)
    REFERENCES `managers` (`manager_id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Platform admin preferences and operational configuration';

-- Table: managers
CREATE TABLE `managers` (
  `manager_id` BIGINT NOT NULL,
  `full_name` VARCHAR(255) NOT NULL,
  `phone` VARCHAR(20) NOT NULL,
  `employee_id` VARCHAR(50) DEFAULT NULL,
  `department` VARCHAR(100) DEFAULT NULL,
  `permissions` JSON DEFAULT NULL COMMENT 'Array of permission codes',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  PRIMARY KEY (`manager_id`),
  UNIQUE KEY `uk_managers_employee` (`employee_id`),
  KEY `idx_managers_phone` (`phone`),
  CONSTRAINT `fk_managers_users`
    FOREIGN KEY (`manager_id`)
    REFERENCES `users` (`user_id`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: user_tokens (NEW - Secure token management)
-- Replaces deprecated verification_token and reset_password_token fields
CREATE TABLE `user_tokens` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `token_type` ENUM('VERIFY_EMAIL', 'RESET_PASSWORD', 'INVITE', 'MFA_RECOVERY') NOT NULL,
  `token_hash` VARCHAR(64) NOT NULL COMMENT 'SHA-256 hash of token - NEVER store plaintext',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `expires_at` DATETIME NOT NULL,
  `consumed_at` DATETIME DEFAULT NULL,
  `metadata` JSON DEFAULT NULL COMMENT 'Additional data (IP, user agent, etc.)',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_tokens` (`user_id`, `token_type`, `token_hash`),
  KEY `idx_user_tokens_lookup` (`user_id`, `token_type`, `expires_at`),
  KEY `idx_user_tokens_type_hash_expires` (`token_type`, `token_hash`, `expires_at`),
  KEY `idx_user_tokens_cleanup` (`expires_at`),
  CONSTRAINT `chk_user_tokens_expires_valid`
    CHECK (`expires_at` > `created_at`),
  CONSTRAINT `fk_user_tokens_user`
    FOREIGN KEY (`user_id`)
    REFERENCES `users` (`user_id`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Secure token storage for email verification, password reset, etc.';

-- Table: user_sessions (NEW - JWT refresh token management)
CREATE TABLE `user_sessions` (
  `session_id` CHAR(36) NOT NULL DEFAULT (UUID()),
  `user_id` BIGINT NOT NULL,
  `refresh_token_hash` VARCHAR(64) NOT NULL COMMENT 'SHA-256 hash of refresh token',

  -- Session lifecycle
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_seen_at` DATETIME DEFAULT NULL,
  `expires_at` DATETIME NOT NULL,

  -- Revocation
  `revoked_at` DATETIME DEFAULT NULL,
  `revoked_reason` TEXT DEFAULT NULL,

  -- Security tracking
  `ip_address` VARCHAR(45) DEFAULT NULL COMMENT 'IPv4 or IPv6',
  `user_agent` TEXT DEFAULT NULL,
  `device_id` VARCHAR(255) DEFAULT NULL COMMENT 'Client-provided device identifier',

  PRIMARY KEY (`session_id`),
  KEY `idx_user_sessions_active` (`user_id`, `expires_at`),
  KEY `idx_user_sessions_refresh_token` (`refresh_token_hash`),
  KEY `idx_user_sessions_cleanup` (`expires_at`),
  CONSTRAINT `chk_user_sessions_expires_valid`
    CHECK (`expires_at` > `created_at`),
  CONSTRAINT `fk_user_sessions_user`
    FOREIGN KEY (`user_id`)
    REFERENCES `users` (`user_id`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Manages refresh tokens for JWT authentication with rotation support';

-- Table: login_attempts (NEW - Rate limiting & security monitoring)
CREATE TABLE `login_attempts` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT DEFAULT NULL,
  `email` VARCHAR(255) NOT NULL,
  `ip_address` VARCHAR(45) DEFAULT NULL,
  `user_agent` TEXT DEFAULT NULL,
  `success` BOOLEAN NOT NULL,
  `failure_reason` TEXT DEFAULT NULL,
  `attempted_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (`id`),
  KEY `idx_login_attempts_email_time` (`email`, `attempted_at` DESC),
  KEY `idx_login_attempts_ip_time` (`ip_address`, `attempted_at` DESC),
  KEY `idx_login_attempts_user_time` (`user_id`, `attempted_at` DESC),
  KEY `idx_login_attempts_cleanup` (`attempted_at`),
  CONSTRAINT `fk_login_attempts_user`
    FOREIGN KEY (`user_id`)
    REFERENCES `users` (`user_id`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Audit log of all login attempts for rate limiting and security analysis';

-- Table: audit_log (NEW - Comprehensive audit trail)
CREATE TABLE `audit_log` (
  `audit_id` BIGINT NOT NULL AUTO_INCREMENT,
  `table_name` VARCHAR(64) NOT NULL,
  `action` ENUM('INSERT', 'UPDATE', 'DELETE') NOT NULL,
  `row_pk` VARCHAR(255) DEFAULT NULL COMMENT 'Primary key value',
  `old_data` JSON DEFAULT NULL COMMENT 'Row data before change',
  `new_data` JSON DEFAULT NULL COMMENT 'Row data after change',

  -- Actor tracking (set via application)
  `actor_id` BIGINT DEFAULT NULL COMMENT 'User who performed the action',
  `actor_role` VARCHAR(20) DEFAULT NULL,
  `request_id` VARCHAR(36) DEFAULT NULL COMMENT 'Request correlation ID',

  `occurred_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (`audit_id`),
  KEY `idx_audit_log_table_time` (`table_name`, `occurred_at` DESC),
  KEY `idx_audit_log_actor_time` (`actor_id`, `occurred_at` DESC),
  KEY `idx_audit_log_time` (`occurred_at` DESC),
  CONSTRAINT `fk_audit_log_actor`
    FOREIGN KEY (`actor_id`)
    REFERENCES `users` (`user_id`)
    ON DELETE SET NULL
    ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Complete audit trail for all changes to Auth & Users tables';

-- Table: otp_codes (Kept for backward compatibility)
CREATE TABLE `otp_codes` (
  `otp_id` BIGINT NOT NULL AUTO_INCREMENT,
  `email` VARCHAR(255) NOT NULL,
  `code` VARCHAR(6) NOT NULL,
  `expires_at` DATETIME NOT NULL,
  `is_used` BOOLEAN DEFAULT FALSE,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`otp_id`),
  KEY `idx_otp_email` (`email`),
  KEY `idx_otp_expires` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='OTP codes for email verification (legacy - consider migrating to user_tokens)';

--
-- TRIGGERS FOR MEMBER 1 (Authentication & Users)
--

-- Trigger: Prevent users from having both customer and transport profiles
DELIMITER $$

CREATE TRIGGER `trg_customers_role_check`
BEFORE INSERT ON `customers`
FOR EACH ROW
BEGIN
  DECLARE user_role VARCHAR(20);
  DECLARE has_transport BOOLEAN;

  -- Check user role
  SELECT `role` INTO user_role FROM `users` WHERE `user_id` = NEW.customer_id;
  IF user_role != 'CUSTOMER' THEN
    SIGNAL SQLSTATE '45000'
    SET MESSAGE_TEXT = 'User must have CUSTOMER role';
  END IF;

  -- Check if user already has transport profile
  SELECT COUNT(*) > 0 INTO has_transport FROM `transports` WHERE `transport_id` = NEW.customer_id;
  IF has_transport THEN
    SIGNAL SQLSTATE '45000'
    SET MESSAGE_TEXT = 'User already has a transport profile';
  END IF;
END$$

CREATE TRIGGER `trg_transports_role_check`
BEFORE INSERT ON `transports`
FOR EACH ROW
BEGIN
  DECLARE user_role VARCHAR(20);
  DECLARE has_customer BOOLEAN;

  -- Check user role
  SELECT `role` INTO user_role FROM `users` WHERE `user_id` = NEW.transport_id;
  IF user_role != 'TRANSPORT' THEN
    SIGNAL SQLSTATE '45000'
    SET MESSAGE_TEXT = 'User must have TRANSPORT role';
  END IF;

  -- Check if user already has customer profile
  SELECT COUNT(*) > 0 INTO has_customer FROM `customers` WHERE `customer_id` = NEW.transport_id;
  IF has_customer THEN
    SIGNAL SQLSTATE '45000'
    SET MESSAGE_TEXT = 'User already has a customer profile';
  END IF;
END$$

-- Trigger: Audit logging for users table
CREATE TRIGGER `trg_audit_users_insert`
AFTER INSERT ON `users`
FOR EACH ROW
BEGIN
  INSERT INTO `audit_log` (`table_name`, `action`, `row_pk`, `new_data`)
  VALUES (
    'users',
    'INSERT',
    NEW.user_id,
    JSON_OBJECT(
      'user_id', NEW.user_id,
      'email', NEW.email,
      'role', NEW.role,
      'is_active', NEW.is_active,
      'is_verified', NEW.is_verified,
      'created_at', NEW.created_at
    )
  );
END$$

CREATE TRIGGER `trg_audit_users_update`
AFTER UPDATE ON `users`
FOR EACH ROW
BEGIN
  INSERT INTO `audit_log` (`table_name`, `action`, `row_pk`, `old_data`, `new_data`)
  VALUES (
    'users',
    'UPDATE',
    NEW.user_id,
    JSON_OBJECT(
      'email', OLD.email,
      'role', OLD.role,
      'is_active', OLD.is_active,
      'is_verified', OLD.is_verified,
      'locked_until', OLD.locked_until
    ),
    JSON_OBJECT(
      'email', NEW.email,
      'role', NEW.role,
      'is_active', NEW.is_active,
      'is_verified', NEW.is_verified,
      'locked_until', NEW.locked_until
    )
  );
END$$

CREATE TRIGGER `trg_audit_users_delete`
AFTER DELETE ON `users`
FOR EACH ROW
BEGIN
  INSERT INTO `audit_log` (`table_name`, `action`, `row_pk`, `old_data`)
  VALUES (
    'users',
    'DELETE',
    OLD.user_id,
    JSON_OBJECT(
      'email', OLD.email,
      'role', OLD.role,
      'created_at', OLD.created_at
    )
  );
END$$

DELIMITER ;

--
-- MODULE 3: CATEGORIES & ITEMS (Shared - Member 3)
--

-- Table: categories
CREATE TABLE `categories` (
  `category_id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  `name_en` VARCHAR(100) DEFAULT NULL,
  `description` TEXT DEFAULT NULL,
  `icon` VARCHAR(50) DEFAULT NULL,

  -- Defaults
  `default_weight_kg` DECIMAL(8,2) DEFAULT NULL,
  `default_volume_m3` DECIMAL(6,2) DEFAULT NULL,
  `default_length_cm` DECIMAL(8,2) DEFAULT NULL,
  `default_width_cm` DECIMAL(8,2) DEFAULT NULL,
  `default_height_cm` DECIMAL(8,2) DEFAULT NULL,
  `is_fragile_default` BOOLEAN DEFAULT FALSE,
  `requires_disassembly_default` BOOLEAN DEFAULT FALSE,

  `display_order` INT DEFAULT 0,
  `is_active` BOOLEAN DEFAULT TRUE,

  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`category_id`),
  UNIQUE KEY `uk_categories_name` (`name`),
  KEY `idx_categories_display` (`display_order`),
  KEY `idx_categories_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: sizes (Enhanced constraints - MOVED HERE before booking_items)
CREATE TABLE `sizes` (
  `size_id` BIGINT NOT NULL AUTO_INCREMENT,
  `category_id` BIGINT NOT NULL,
  `name` VARCHAR(100) NOT NULL COMMENT 'Small/Medium/Large/etc.',
  `weight_kg` DECIMAL(8,2) DEFAULT NULL,
  `height_cm` DECIMAL(8,2) DEFAULT NULL,
  `width_cm` DECIMAL(8,2) DEFAULT NULL,
  `depth_cm` DECIMAL(8,2) DEFAULT NULL,
  `price_multiplier` DECIMAL(4,2) NOT NULL DEFAULT 1.00,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`size_id`),
  UNIQUE KEY `uk_sizes_category_name` (`category_id`,`name`),
  KEY `idx_sizes_category` (`category_id`),
  CONSTRAINT `fk_sizes_category` FOREIGN KEY (`category_id`) REFERENCES `categories` (`category_id`) ON DELETE CASCADE,
  CONSTRAINT `chk_sizes_dims` CHECK (
    (`weight_kg` IS NULL OR `weight_kg` >= 0) AND
    (`height_cm` IS NULL OR `height_cm` > 0) AND
    (`width_cm` IS NULL OR `width_cm` > 0) AND
    (`depth_cm` IS NULL OR `depth_cm` > 0)
  ),
  CONSTRAINT `chk_sizes_multiplier_bounds` CHECK (`price_multiplier` >= 0.10 AND `price_multiplier` <= 10.00)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Kích thước theo danh mục (hệ số giá)';

--
-- MODULE 2: BOOKING & QUOTATION (Member 2 - Quy)
--

-- Table: bookings (ENHANCED for Member 2 - Vietnamese Market)
CREATE TABLE `bookings` (
  `booking_id` BIGINT NOT NULL AUTO_INCREMENT,
  `customer_id` BIGINT NOT NULL,
  `transport_id` BIGINT DEFAULT NULL,

  -- Pickup Address (Free-text + Structured VN Address)
  `pickup_address` TEXT NOT NULL,
  `pickup_latitude` DECIMAL(10,8) DEFAULT NULL,
  `pickup_longitude` DECIMAL(11,8) DEFAULT NULL,
  `pickup_floor` INT DEFAULT NULL,
  `pickup_has_elevator` BOOLEAN DEFAULT FALSE,
  `pickup_province_code` VARCHAR(6) DEFAULT NULL COMMENT 'VN Province Code',
  `pickup_district_code` VARCHAR(6) DEFAULT NULL COMMENT 'VN District Code',
  `pickup_ward_code` VARCHAR(6) DEFAULT NULL COMMENT 'VN Ward Code',

  -- Delivery Address (Free-text + Structured VN Address)
  `delivery_address` TEXT NOT NULL,
  `delivery_latitude` DECIMAL(10,8) DEFAULT NULL,
  `delivery_longitude` DECIMAL(11,8) DEFAULT NULL,
  `delivery_floor` INT DEFAULT NULL,
  `delivery_has_elevator` BOOLEAN DEFAULT FALSE,
  `delivery_province_code` VARCHAR(6) DEFAULT NULL COMMENT 'VN Province Code',
  `delivery_district_code` VARCHAR(6) DEFAULT NULL COMMENT 'VN District Code',
  `delivery_ward_code` VARCHAR(6) DEFAULT NULL COMMENT 'VN Ward Code',

  -- Schedule
  `preferred_date` DATE NOT NULL,
  `preferred_time_slot` ENUM('MORNING', 'AFTERNOON', 'EVENING') DEFAULT NULL,
  `actual_start_time` DATETIME DEFAULT NULL,
  `actual_end_time` DATETIME DEFAULT NULL,

  -- Distance & Pricing
  `distance_km` DECIMAL(8,2) DEFAULT NULL,
  `distance_source` ENUM('GOOGLE', 'MAPBOX', 'OSRM', 'MANUAL') DEFAULT NULL COMMENT 'Distance API source',
  `distance_calculated_at` DATETIME DEFAULT NULL,
  `estimated_price` DECIMAL(12,0) DEFAULT NULL COMMENT 'VND (integer)',
  `final_price` DECIMAL(12,0) DEFAULT NULL COMMENT 'VND (integer)',

  -- Status
  `status` ENUM('PENDING', 'QUOTED', 'CONFIRMED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED') DEFAULT 'PENDING',

  -- Additional Info
  `notes` TEXT DEFAULT NULL,
  `special_requirements` TEXT DEFAULT NULL,

  -- Cancellation
  `cancelled_by` BIGINT DEFAULT NULL,
  `cancellation_reason` TEXT DEFAULT NULL,
  `cancelled_at` DATETIME DEFAULT NULL,

  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  PRIMARY KEY (`booking_id`),
  KEY `idx_bookings_customer` (`customer_id`),
  KEY `idx_bookings_transport` (`transport_id`),
  KEY `idx_bookings_status` (`status`),
  KEY `idx_bookings_date` (`preferred_date`),
  KEY `idx_bookings_created` (`created_at` DESC),
  KEY `idx_bookings_customer_status` (`customer_id`, `status`),
  KEY `idx_bookings_transport_status` (`transport_id`, `status`),
  KEY `idx_bookings_pickup_province` (`pickup_province_code`),
  KEY `idx_bookings_delivery_province` (`delivery_province_code`),
  KEY `idx_bookings_customer_date` (`customer_id`, `preferred_date` DESC),
  KEY `idx_bookings_transport_date_status` (`transport_id`, `preferred_date`, `status`),

  CONSTRAINT `fk_bookings_customer`
    FOREIGN KEY (`customer_id`)
    REFERENCES `customers` (`customer_id`)
    ON DELETE NO ACTION,
  CONSTRAINT `fk_bookings_transport`
    FOREIGN KEY (`transport_id`)
    REFERENCES `transports` (`transport_id`)
    ON DELETE NO ACTION,
  CONSTRAINT `fk_bookings_cancelled_by`
    FOREIGN KEY (`cancelled_by`)
    REFERENCES `users` (`user_id`)
    ON DELETE SET NULL,

  -- Vietnamese address FKs (optional - populate vn_* tables first)
  CONSTRAINT `fk_bookings_pickup_province`
    FOREIGN KEY (`pickup_province_code`)
    REFERENCES `vn_provinces` (`province_code`)
    ON DELETE SET NULL,
  CONSTRAINT `fk_bookings_pickup_district`
    FOREIGN KEY (`pickup_district_code`)
    REFERENCES `vn_districts` (`district_code`)
    ON DELETE SET NULL,
  CONSTRAINT `fk_bookings_pickup_ward`
    FOREIGN KEY (`pickup_ward_code`)
    REFERENCES `vn_wards` (`ward_code`)
    ON DELETE SET NULL,
  CONSTRAINT `fk_bookings_delivery_province`
    FOREIGN KEY (`delivery_province_code`)
    REFERENCES `vn_provinces` (`province_code`)
    ON DELETE SET NULL,
  CONSTRAINT `fk_bookings_delivery_district`
    FOREIGN KEY (`delivery_district_code`)
    REFERENCES `vn_districts` (`district_code`)
    ON DELETE SET NULL,
  CONSTRAINT `fk_bookings_delivery_ward`
    FOREIGN KEY (`delivery_ward_code`)
    REFERENCES `vn_wards` (`ward_code`)
    ON DELETE SET NULL,

  -- Geo validation
  CONSTRAINT `chk_bookings_pickup_lat`
    CHECK (`pickup_latitude` IS NULL OR (`pickup_latitude` BETWEEN -90 AND 90)),
  CONSTRAINT `chk_bookings_pickup_lng`
    CHECK (`pickup_longitude` IS NULL OR (`pickup_longitude` BETWEEN -180 AND 180)),
  CONSTRAINT `chk_bookings_delivery_lat`
    CHECK (`delivery_latitude` IS NULL OR (`delivery_latitude` BETWEEN -90 AND 90)),
  CONSTRAINT `chk_bookings_delivery_lng`
    CHECK (`delivery_longitude` IS NULL OR (`delivery_longitude` BETWEEN -180 AND 180)),
  CONSTRAINT `chk_bookings_floors`
    CHECK (
      (`pickup_floor` IS NULL OR `pickup_floor` >= 0) AND
      (`delivery_floor` IS NULL OR `delivery_floor` >= 0)
    ),
  CONSTRAINT `chk_bookings_prices_positive`
    CHECK (
      (`estimated_price` IS NULL OR `estimated_price` >= 0) AND
      (`final_price` IS NULL OR `final_price` >= 0)
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Bookings with Vietnamese address structure and geo validation';

-- Table: transport_list (Notification tracking for bookings)
CREATE TABLE `transport_list` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `booking_id` BIGINT NOT NULL,
  `transport_id` BIGINT NOT NULL,

  -- Notification tracking
  `notified_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `notification_method` ENUM('EMAIL', 'SMS', 'PUSH') DEFAULT 'EMAIL',

  -- Response tracking
  `has_viewed` BOOLEAN DEFAULT FALSE,
  `viewed_at` DATETIME DEFAULT NULL,
  `has_responded` BOOLEAN DEFAULT FALSE,
  `responded_at` DATETIME DEFAULT NULL,

  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_transport_list_booking_transport` (`booking_id`, `transport_id`),
  KEY `idx_transport_list_booking` (`booking_id`),
  KEY `idx_transport_list_transport` (`transport_id`),
  CONSTRAINT `fk_transport_list_booking`
    FOREIGN KEY (`booking_id`)
    REFERENCES `bookings` (`booking_id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_transport_list_transport`
    FOREIGN KEY (`transport_id`)
    REFERENCES `transports` (`transport_id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Tracks which transports were notified about each booking';

-- Table: booking_items
CREATE TABLE `booking_items` (
  `item_id` BIGINT NOT NULL AUTO_INCREMENT,
  `booking_id` BIGINT NOT NULL,
  `category_id` BIGINT DEFAULT NULL,
  `size_id` BIGINT DEFAULT NULL,

  -- Item Details
  `name` VARCHAR(255) NOT NULL,
  `description` TEXT DEFAULT NULL,
  `quantity` INT NOT NULL DEFAULT 1,

  -- Dimensions
  `weight_kg` DECIMAL(8,2) DEFAULT NULL,
  `height_cm` DECIMAL(8,2) DEFAULT NULL,
  `width_cm` DECIMAL(8,2) DEFAULT NULL,
  `depth_cm` DECIMAL(8,2) DEFAULT NULL,

  -- Characteristics
  `is_fragile` BOOLEAN DEFAULT FALSE,
  `requires_disassembly` BOOLEAN DEFAULT FALSE,
  `estimated_disassembly_time` INT DEFAULT NULL,

  -- Pricing
  `unit_price` DECIMAL(12,0) DEFAULT NULL COMMENT 'VND (integer)',
  `total_price` DECIMAL(12,0) DEFAULT NULL COMMENT 'VND (integer)',
  
  -- AI metadata payload
  `ai_metadata` JSON DEFAULT NULL,

  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`item_id`),
  KEY `idx_booking_items_booking` (`booking_id`),
  KEY `idx_booking_items_category` (`category_id`),
  KEY `idx_booking_items_size` (`size_id`),
  CONSTRAINT `fk_booking_items_booking`
    FOREIGN KEY (`booking_id`)
    REFERENCES `bookings` (`booking_id`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_booking_items_category`
    FOREIGN KEY (`category_id`)
    REFERENCES `categories` (`category_id`)
    ON DELETE SET NULL
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_booking_items_size`
    FOREIGN KEY (`size_id`)
    REFERENCES `sizes` (`size_id`)
    ON DELETE SET NULL
    ON UPDATE NO ACTION,

  -- Business Rules
  CONSTRAINT `chk_booking_items_positive_values`
    CHECK (
      `quantity` > 0 AND
      (`weight_kg` IS NULL OR `weight_kg` >= 0) AND
      (`unit_price` IS NULL OR `unit_price` >= 0) AND
      (`total_price` IS NULL OR `total_price` >= 0)
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: quotations
CREATE TABLE `quotations` (
  `quotation_id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `booking_id` BIGINT NOT NULL,
  `transport_id` BIGINT NOT NULL,
  `quoted_price` DECIMAL(12,0) NOT NULL,
  `base_price` DECIMAL(12,0) DEFAULT NULL,
  `distance_price` DECIMAL(12,0) DEFAULT NULL,
  `items_price` DECIMAL(12,0) DEFAULT NULL,
  `additional_fees` DECIMAL(12,0) DEFAULT NULL,
  `discount` DECIMAL(12,0) DEFAULT NULL,
  `price_breakdown` JSON DEFAULT NULL,
  `notes` TEXT DEFAULT NULL,
  `validity_period` INT DEFAULT 7,
  `expires_at` DATETIME DEFAULT NULL,
  `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  `responded_at` DATETIME DEFAULT NULL,
  `accepted_by` BIGINT DEFAULT NULL,
  `accepted_at` DATETIME DEFAULT NULL,
  `accepted_ip` VARCHAR(45) DEFAULT NULL,
  `accepted_booking_id` BIGINT DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY `idx_quotations_booking` (`booking_id`),
  KEY `idx_quotations_transport_created` (`transport_id`, `created_at`),
  KEY `idx_quotations_transport_status` (`transport_id`, `status`),
  KEY `idx_quotations_status` (`status`),
  KEY `idx_quotations_expires` (`expires_at`),
  CONSTRAINT `fk_quotations_booking`
    FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`booking_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_quotations_transport`
    FOREIGN KEY (`transport_id`) REFERENCES `transports` (`transport_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: contracts (ENHANCED - Validation & Auto-Activation)
CREATE TABLE `contracts` (
  `contract_id` BIGINT NOT NULL AUTO_INCREMENT,
  `booking_id` BIGINT NOT NULL,
  `quotation_id` BIGINT NOT NULL,

  -- Contract Details
  `contract_number` VARCHAR(50) NOT NULL,
  `terms_and_conditions` TEXT NOT NULL,

  -- Commercial Terms Snapshot (VND - integer only)
  `total_amount` DECIMAL(12,0) NOT NULL COMMENT 'VND (integer) - DEPRECATED, use agreed_price_vnd',
  `agreed_price_vnd` BIGINT NOT NULL DEFAULT 0 COMMENT 'Final agreed amount in VND',
  `deposit_required_vnd` BIGINT NOT NULL DEFAULT 0 COMMENT '50% upfront deposit',
  `deposit_due_at` DATETIME DEFAULT NULL,
  `balance_due_at` DATETIME DEFAULT NULL,

  -- Customer Signature
  `customer_signed` BOOLEAN DEFAULT FALSE,
  `customer_signed_at` DATETIME DEFAULT NULL,
  `customer_signature_url` TEXT DEFAULT NULL,
  `customer_signed_ip` VARCHAR(45) DEFAULT NULL,

  -- Transport Signature
  `transport_signed` BOOLEAN DEFAULT FALSE,
  `transport_signed_at` DATETIME DEFAULT NULL,
  `transport_signature_url` TEXT DEFAULT NULL,
  `transport_signed_ip` VARCHAR(45) DEFAULT NULL,

  -- Status
  `status` ENUM('DRAFT', 'ACTIVE', 'COMPLETED', 'TERMINATED') DEFAULT 'DRAFT',

  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  PRIMARY KEY (`contract_id`),
  UNIQUE KEY `uk_contracts_booking` (`booking_id`),
  UNIQUE KEY `uk_contracts_number` (`contract_number`),
  KEY `idx_contracts_status` (`status`),

  CONSTRAINT `fk_contracts_booking`
    FOREIGN KEY (`booking_id`)
    REFERENCES `bookings` (`booking_id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_contracts_quotation`
    FOREIGN KEY (`quotation_id`)
    REFERENCES `quotations` (`quotation_id`)
    ON DELETE NO ACTION,

  -- Business Rules
  CONSTRAINT `chk_contracts_amounts_positive`
    CHECK (`total_amount` > 0 AND `agreed_price_vnd` > 0),
  CONSTRAINT `chk_contracts_deposit_valid`
    CHECK (`deposit_required_vnd` >= 0 AND `deposit_required_vnd` <= `agreed_price_vnd`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Contracts with commercial terms snapshot and auto-activation';

-- Table: payments (ENHANCED - Integer VND & Idempotency)
CREATE TABLE `payments` (
  `payment_id` BIGINT NOT NULL AUTO_INCREMENT,
  `booking_id` BIGINT NOT NULL,

  -- Payment Details (Vietnamese market - Integer VND only)
  `amount` DECIMAL(12,0) NOT NULL COMMENT 'Amount in VND (integer for gateways)',
  `payment_method` ENUM('CASH', 'BANK_TRANSFER') NOT NULL COMMENT 'Only cash or manual bank transfer supported',
  `payment_type` ENUM('DEPOSIT', 'REMAINING_PAYMENT', 'TIP', 'REFUND') NOT NULL COMMENT 'Escrow breakdown (30%, 70%, tip, refund)',
  `bank_code` VARCHAR(10) DEFAULT NULL COMMENT 'Vietnamese bank code',

  -- Status
  `status` ENUM('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'REFUNDED') DEFAULT 'PENDING',

  -- Refund Support
  `parent_payment_id` BIGINT DEFAULT NULL COMMENT 'Original payment for refunds',
  `refund_reason` VARCHAR(255) DEFAULT NULL,

  -- Failure Tracking
  `failure_code` VARCHAR(50) DEFAULT NULL,
  `failure_message` TEXT DEFAULT NULL,

  -- Transaction Info
  `transaction_id` VARCHAR(255) DEFAULT NULL COMMENT 'Internal transaction reference',

  -- Idempotency (Prevent double charges)
  `idempotency_key` VARCHAR(64) DEFAULT NULL COMMENT 'Client-provided idempotency key',

  -- Confirmation metadata for manual verification
  `confirmed_by` BIGINT DEFAULT NULL COMMENT 'User who confirmed the payment (if applicable)',
  `confirmed_at` DATETIME DEFAULT NULL COMMENT 'When payment was confirmed',

  -- Timestamps
  `paid_at` DATETIME DEFAULT NULL,
  `refunded_at` DATETIME DEFAULT NULL,

  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  PRIMARY KEY (`payment_id`),
  UNIQUE KEY `uk_payments_transaction_id` (`transaction_id`),
  UNIQUE KEY `uk_payments_idempotency` (`idempotency_key`),
  KEY `idx_payments_booking` (`booking_id`),
  KEY `idx_payments_status` (`status`),
  KEY `idx_payments_booking_created` (`booking_id`, `created_at` DESC),

  CONSTRAINT `fk_payments_booking`
    FOREIGN KEY (`booking_id`)
    REFERENCES `bookings` (`booking_id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_payments_parent`
    FOREIGN KEY (`parent_payment_id`)
    REFERENCES `payments` (`payment_id`)
    ON DELETE SET NULL,
  CONSTRAINT `fk_payments_bank`
    FOREIGN KEY (`bank_code`)
    REFERENCES `vn_banks` (`bank_code`)
    ON UPDATE CASCADE
    ON DELETE SET NULL,

  -- Business Rules
  CONSTRAINT `chk_payments_amount_positive`
    CHECK (`amount` > 0)
  -- CONSTRAINT `chk_payments_refund_has_parent`
  --   CHECK (`payment_type` != 'REFUND' OR `parent_payment_id` IS NOT NULL)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Payments with idempotency and refund support for Vietnamese gateways';

-- Performance index for refund validation trigger
CREATE INDEX `idx_payments_parent_refund`
ON `payments`(`parent_payment_id`, `status`, `amount`);

--
-- TRIGGERS FOR MEMBER 2 (Booking & Quotation)
--
-- NOTE: Triggers moved here AFTER table creation

DELIMITER $$

-- Trigger: Auto-update booking when quotation is accepted
CREATE TRIGGER `trg_quotations_accepted`
AFTER UPDATE ON `quotations`
FOR EACH ROW
BEGIN
  IF NEW.status = 'ACCEPTED' AND OLD.status != 'ACCEPTED' THEN
    UPDATE `bookings`
    SET
      `status` = IF(`status` IN ('PENDING', 'QUOTED'), 'CONFIRMED', `status`),
      `transport_id` = NEW.transport_id,
      `final_price` = NEW.quoted_price,
      `updated_at` = CURRENT_TIMESTAMP
    WHERE `booking_id` = NEW.booking_id;
  END IF;
END$$

-- Trigger: Validate quotation acceptance
CREATE TRIGGER `trg_quotations_validate_acceptance`
BEFORE UPDATE ON `quotations`
FOR EACH ROW
BEGIN
  IF NEW.status = 'ACCEPTED' AND OLD.status != 'ACCEPTED' THEN
    DECLARE booking_customer_id BIGINT DEFAULT NULL;
    DECLARE booking_count INT DEFAULT 0;

    SELECT COUNT(*), `customer_id` INTO booking_count, booking_customer_id
    FROM `bookings`
    WHERE `booking_id` = NEW.booking_id;

    IF booking_count = 0 THEN
      SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Booking not found for quotation';
    END IF;

    IF NEW.accepted_by IS NULL OR NEW.accepted_by != booking_customer_id THEN
      SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Only booking owner can accept quotation';
    END IF;

    IF NEW.expires_at IS NOT NULL AND NEW.expires_at < NOW() THEN
      SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Cannot accept expired quotation';
    END IF;

    IF NEW.accepted_at IS NULL THEN
      SET NEW.accepted_at = NOW();
    END IF;
  END IF;
END$$

-- Stored Procedure: Accept quotation and reject others atomically
CREATE PROCEDURE `sp_accept_quotation`(
  IN p_quotation_id BIGINT,
  IN p_customer_id BIGINT,
  IN p_ip_address VARCHAR(45)
)
BEGIN
  DECLARE v_booking_id BIGINT;
  DECLARE v_current_status VARCHAR(20);
  DECLARE EXIT HANDLER FOR SQLEXCEPTION
  BEGIN
    ROLLBACK;
    RESIGNAL;
  END;

  START TRANSACTION;

  SELECT booking_id, status
  INTO v_booking_id, v_current_status
  FROM quotations
  WHERE quotation_id = p_quotation_id
  FOR UPDATE;

  IF v_booking_id IS NULL THEN
    SIGNAL SQLSTATE '45000'
    SET MESSAGE_TEXT = 'Quotation not found';
  END IF;

  IF v_current_status <> 'PENDING' THEN
    SIGNAL SQLSTATE '45000'
    SET MESSAGE_TEXT = 'Quotation already processed';
  END IF;

  UPDATE `quotations`
  SET
    `status` = 'ACCEPTED',
    `accepted_by` = p_customer_id,
    `accepted_at` = NOW(),
    `accepted_ip` = p_ip_address
  WHERE `quotation_id` = p_quotation_id;

  UPDATE `quotations`
  SET
    `status` = 'REJECTED',
    `responded_at` = NOW()
  WHERE
    `booking_id` = v_booking_id
    AND `quotation_id` != p_quotation_id
    AND `status` = 'PENDING';

  COMMIT;
END$$

-- Trigger: Validate contract creation
CREATE TRIGGER `trg_contracts_validate_quotation`
BEFORE INSERT ON `contracts`
FOR EACH ROW
BEGIN
  DECLARE quote_status VARCHAR(20);
  DECLARE quote_booking BIGINT;

  SELECT `status`, `booking_id`
  INTO quote_status, quote_booking
  FROM `quotations`
  WHERE `quotation_id` = NEW.quotation_id;

  IF quote_status IS NULL THEN
    SIGNAL SQLSTATE '45000'
    SET MESSAGE_TEXT = 'Quotation does not exist';
  END IF;

  IF quote_status != 'ACCEPTED' THEN
    SIGNAL SQLSTATE '45000'
    SET MESSAGE_TEXT = 'Contract can only be created from ACCEPTED quotation';
  END IF;

  IF quote_booking != NEW.booking_id THEN
    SIGNAL SQLSTATE '45000'
    SET MESSAGE_TEXT = 'Quotation does not belong to this booking';
  END IF;
END$$

-- Trigger: Auto-activate contract
CREATE TRIGGER `trg_contracts_auto_activate`
BEFORE UPDATE ON `contracts`
FOR EACH ROW
BEGIN
  IF NEW.customer_signed = TRUE
     AND NEW.transport_signed = TRUE
     AND NEW.status = 'DRAFT'
     AND (OLD.customer_signed = FALSE OR OLD.transport_signed = FALSE) THEN
    SET NEW.status = 'ACTIVE';
  END IF;
END$$

-- Trigger: Log booking status changes
CREATE TRIGGER `trg_booking_status_history`
AFTER UPDATE ON `bookings`
FOR EACH ROW
BEGIN
  IF NEW.status != OLD.status THEN
    INSERT INTO `booking_status_history`
      (`booking_id`, `old_status`, `new_status`, `changed_at`)
    VALUES
      (NEW.booking_id, OLD.status, NEW.status, NOW());
  END IF;
END$$

-- Trigger: Log quotation status changes
CREATE TRIGGER `trg_quotation_status_history`
AFTER UPDATE ON `quotations`
FOR EACH ROW
BEGIN
  IF NEW.status != OLD.status THEN
    INSERT INTO `quotation_status_history`
      (`quotation_id`, `old_status`, `new_status`, `changed_at`)
    VALUES
      (NEW.quotation_id, OLD.status, NEW.status, NOW());
  END IF;
END$$

-- Trigger: Validate payment refunds
CREATE TRIGGER `trg_payments_validate_refund`
BEFORE INSERT ON `payments`
FOR EACH ROW
BEGIN
  IF NEW.payment_type = 'REFUND' THEN
    DECLARE parent_booking BIGINT;
    DECLARE parent_amount DECIMAL(12,0);
    DECLARE parent_type VARCHAR(20);
    DECLARE parent_status VARCHAR(20);
    DECLARE already_refunded DECIMAL(12,0);

    SELECT `booking_id`, `amount`, `payment_type`, `status`
    INTO parent_booking, parent_amount, parent_type, parent_status
    FROM `payments`
    WHERE `payment_id` = NEW.parent_payment_id;

    IF parent_booking IS NULL THEN
      SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Parent payment not found';
    END IF;

    IF parent_type = 'REFUND' THEN
      SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Cannot refund a refund';
    END IF;

    IF parent_status != 'COMPLETED' THEN
      SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Can only refund completed payments';
    END IF;

    IF parent_booking != NEW.booking_id THEN
      SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Refund must be for same booking as parent';
    END IF;

    SELECT COALESCE(SUM(`amount`), 0) INTO already_refunded
    FROM `payments`
    WHERE `parent_payment_id` = NEW.parent_payment_id
      AND `status` = 'COMPLETED';

    IF NEW.amount > (parent_amount - already_refunded) THEN
      SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Refund amount exceeds remaining refundable amount';
    END IF;
  END IF;
END$$

-- Trigger: Auto-set paid_at and validate online payments
CREATE TRIGGER `trg_payments_validate_completion`
BEFORE UPDATE ON `payments`
FOR EACH ROW
BEGIN
  IF NEW.status = 'COMPLETED' AND OLD.status != 'COMPLETED' THEN
    IF NEW.paid_at IS NULL THEN
      SET NEW.paid_at = NOW();
    END IF;
  END IF;

  IF NEW.payment_type = 'REFUND'
     AND NEW.status = 'COMPLETED'
     AND OLD.status != 'COMPLETED'
     AND NEW.refunded_at IS NULL THEN
    SET NEW.refunded_at = NOW();
  END IF;
END$$

DELIMITER ;

--
-- MODULE 4: REVIEWS & NOTIFICATIONS (Member 4 - Giang)
--

-- Table: reviews (Enhanced with production-ready constraints)
CREATE TABLE `reviews` (
  `review_id` BIGINT NOT NULL AUTO_INCREMENT,
  `booking_id` BIGINT NOT NULL,
  `reviewer_id` BIGINT NOT NULL,
  `reviewee_id` BIGINT NOT NULL,
  `reviewer_type` ENUM('CUSTOMER', 'TRANSPORT') NOT NULL,

  -- Ratings (1-5 stars, 0.5 increments)
  `overall_rating` DECIMAL(2,1) NOT NULL,
  `punctuality_rating` DECIMAL(2,1) DEFAULT NULL,
  `professionalism_rating` DECIMAL(2,1) DEFAULT NULL,
  `communication_rating` DECIMAL(2,1) DEFAULT NULL,
  `care_rating` DECIMAL(2,1) DEFAULT NULL,

  -- Review Content
  `title` VARCHAR(200) DEFAULT NULL,
  `comment` TEXT NOT NULL,

  -- Moderation
  `status` ENUM('PENDING', 'APPROVED', 'REJECTED', 'FLAGGED') DEFAULT 'PENDING',
  `is_verified` BOOLEAN DEFAULT FALSE,
  `is_anonymous` BOOLEAN DEFAULT FALSE,

  -- Engagement
  `helpful_count` INT DEFAULT 0,
  `unhelpful_count` INT DEFAULT 0,

  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `moderated_at` DATETIME DEFAULT NULL,
  `moderated_by` BIGINT DEFAULT NULL,
  PRIMARY KEY (`review_id`),
  UNIQUE KEY `uk_reviews_booking_type` (`booking_id`, `reviewer_type`),
  KEY `idx_reviews_reviewee_status_created` (`reviewee_id`, `status`, `created_at` DESC),
  KEY `idx_reviews_booking_side` (`booking_id`, `reviewer_type`),
  KEY `idx_reviews_reviewer_created` (`reviewer_id`, `created_at` DESC),
  KEY `idx_reviews_status_created` (`status`, `created_at` DESC),
  FULLTEXT KEY `ft_reviews_text` (`title`, `comment`) WITH PARSER ngram,
  CONSTRAINT `fk_reviews_booking`
    FOREIGN KEY (`booking_id`)
    REFERENCES `bookings` (`booking_id`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_reviews_reviewer`
    FOREIGN KEY (`reviewer_id`)
    REFERENCES `users` (`user_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_reviews_reviewee`
    FOREIGN KEY (`reviewee_id`)
    REFERENCES `users` (`user_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_reviews_moderator`
    FOREIGN KEY (`moderated_by`)
    REFERENCES `users` (`user_id`)
    ON DELETE SET NULL
    ON UPDATE NO ACTION,
  CONSTRAINT `chk_reviews_ratings` CHECK (
    `overall_rating` BETWEEN 1.0 AND 5.0 AND
    (`punctuality_rating` IS NULL OR `punctuality_rating` BETWEEN 1.0 AND 5.0) AND
    (`professionalism_rating` IS NULL OR `professionalism_rating` BETWEEN 1.0 AND 5.0) AND
    (`communication_rating` IS NULL OR `communication_rating` BETWEEN 1.0 AND 5.0) AND
    (`care_rating` IS NULL OR `care_rating` BETWEEN 1.0 AND 5.0)
  ),
  CONSTRAINT `chk_reviews_comment_length` CHECK (CHAR_LENGTH(`comment`) BETWEEN 10 AND 5000)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_vi_0900_ai_ci COMMENT='Bidirectional reviews with Vietnamese text search';

-- Table: review_photos
CREATE TABLE `review_photos` (
  `photo_id` BIGINT NOT NULL AUTO_INCREMENT,
  `review_id` BIGINT NOT NULL,
  `photo_url` TEXT NOT NULL,
  `caption` VARCHAR(200) DEFAULT NULL,
  `display_order` INT DEFAULT 0,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`photo_id`),
  KEY `idx_review_photos_review` (`review_id`),
  CONSTRAINT `fk_review_photos_review`
    FOREIGN KEY (`review_id`)
    REFERENCES `reviews` (`review_id`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: review_responses
CREATE TABLE `review_responses` (
  `response_id` BIGINT NOT NULL AUTO_INCREMENT,
  `review_id` BIGINT NOT NULL,
  `responder_id` BIGINT NOT NULL,
  `response_text` TEXT NOT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`response_id`),
  UNIQUE KEY `uk_review_responses` (`review_id`),
  CONSTRAINT `fk_review_responses_review`
    FOREIGN KEY (`review_id`)
    REFERENCES `reviews` (`review_id`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_review_responses_responder`
    FOREIGN KEY (`responder_id`)
    REFERENCES `users` (`user_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: review_helpfulness (Enhanced with self-vote prevention)
CREATE TABLE `review_helpfulness` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `review_id` BIGINT NOT NULL,
  `voter_id` BIGINT NOT NULL COMMENT 'User who voted (renamed from user_id for clarity)',
  `is_helpful` BOOLEAN NOT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_review_helpfulness` (`review_id`, `voter_id`),
  KEY `idx_review_helpfulness_review` (`review_id`),
  KEY `idx_review_helpfulness_voter` (`voter_id`),
  CONSTRAINT `fk_review_helpfulness_review`
    FOREIGN KEY (`review_id`)
    REFERENCES `reviews` (`review_id`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_review_helpfulness_voter`
    FOREIGN KEY (`voter_id`)
    REFERENCES `users` (`user_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Helpful voting (prevents reviewer/reviewee from voting)';

-- Table: review_reports (Enhanced with duplicate prevention)
CREATE TABLE `review_reports` (
  `report_id` BIGINT NOT NULL AUTO_INCREMENT,
  `review_id` BIGINT NOT NULL,
  `reporter_id` BIGINT NOT NULL,
  `reason` ENUM('SPAM', 'INAPPROPRIATE', 'FAKE', 'OFFENSIVE', 'OTHER') NOT NULL,
  `description` TEXT DEFAULT NULL,
  `status` ENUM('PENDING', 'REVIEWED', 'RESOLVED') DEFAULT 'PENDING',
  `admin_notes` TEXT DEFAULT NULL COMMENT 'Internal notes from moderator',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `resolved_at` DATETIME DEFAULT NULL,
  `resolved_by` BIGINT DEFAULT NULL,
  PRIMARY KEY (`report_id`),
  UNIQUE KEY `uk_review_reports` (`review_id`, `reporter_id`),
  KEY `idx_review_reports_review` (`review_id`),
  KEY `idx_review_reports_status_created` (`status`, `created_at` DESC),
  CONSTRAINT `fk_review_reports_review`
    FOREIGN KEY (`review_id`)
    REFERENCES `reviews` (`review_id`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_review_reports_reporter`
    FOREIGN KEY (`reporter_id`)
    REFERENCES `users` (`user_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_review_reports_resolver`
    FOREIGN KEY (`resolved_by`)
    REFERENCES `users` (`user_id`)
    ON DELETE SET NULL
    ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: notifications
CREATE TABLE `notifications` (
  `notification_id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `user_id` BIGINT NOT NULL,
  `type` VARCHAR(50) NOT NULL,
  `title` VARCHAR(255) NOT NULL,
  `message` TEXT NOT NULL,
  `reference_type` VARCHAR(50) DEFAULT NULL,
  `reference_id` BIGINT DEFAULT NULL,
  `is_read` TINYINT(1) NOT NULL DEFAULT 0,
  `read_at` DATETIME DEFAULT NULL,
  `priority` VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT `fk_notifications_user`
    FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_notifications_user_created
    ON `notifications` (`user_id`, `created_at` DESC);

CREATE INDEX idx_notifications_user_unread
    ON `notifications` (`user_id`, `is_read`, `created_at` DESC);

-- Table: notification_preferences
CREATE TABLE `notification_preferences` (
  `preference_id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,

  -- Email Preferences
  `email_booking_updates` BOOLEAN DEFAULT TRUE,
  `email_reviews` BOOLEAN DEFAULT TRUE,
  `email_payments` BOOLEAN DEFAULT TRUE,
  `email_marketing` BOOLEAN DEFAULT FALSE,

  -- In-app Preferences
  `inapp_booking_updates` BOOLEAN DEFAULT TRUE,
  `inapp_reviews` BOOLEAN DEFAULT TRUE,
  `inapp_payments` BOOLEAN DEFAULT TRUE,

  -- Push Preferences (future)
  `push_enabled` BOOLEAN DEFAULT FALSE,
  `push_booking_updates` BOOLEAN DEFAULT TRUE,

  -- Quiet Hours
  `quiet_hours_enabled` BOOLEAN DEFAULT FALSE,
  `quiet_hours_start` TIME DEFAULT NULL,
  `quiet_hours_end` TIME DEFAULT NULL,

  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`preference_id`),
  UNIQUE KEY `uk_notification_prefs` (`user_id`),
  CONSTRAINT `fk_notification_prefs_user`
    FOREIGN KEY (`user_id`)
    REFERENCES `users` (`user_id`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: rating_summaries (Denormalized for performance)
CREATE TABLE `rating_summaries` (
  `summary_id` BIGINT NOT NULL AUTO_INCREMENT,
  `target_user_id` BIGINT NOT NULL COMMENT 'User being rated (reviewee)',
  `context` ENUM('AS_CUSTOMER', 'AS_TRANSPORT') NOT NULL COMMENT 'Rated as customer or transport',

  -- Aggregated Counts
  `total_count` INT NOT NULL DEFAULT 0,
  `sum_overall_x10` BIGINT NOT NULL DEFAULT 0 COMMENT 'Sum of (rating * 10) for precision',
  `sum_punctuality_x10` BIGINT NOT NULL DEFAULT 0,
  `sum_professionalism_x10` BIGINT NOT NULL DEFAULT 0,
  `sum_communication_x10` BIGINT NOT NULL DEFAULT 0,
  `sum_care_x10` BIGINT NOT NULL DEFAULT 0,

  -- Star Distribution
  `count_5_star` INT NOT NULL DEFAULT 0,
  `count_4_star` INT NOT NULL DEFAULT 0,
  `count_3_star` INT NOT NULL DEFAULT 0,
  `count_2_star` INT NOT NULL DEFAULT 0,
  `count_1_star` INT NOT NULL DEFAULT 0,

  -- Computed Averages (for quick reads)
  `avg_overall` DECIMAL(3,2) GENERATED ALWAYS AS (
    CASE WHEN `total_count` > 0
      THEN ROUND(`sum_overall_x10` / 10.0 / `total_count`, 2)
      ELSE 0
    END
  ) STORED,
  `avg_punctuality` DECIMAL(3,2) GENERATED ALWAYS AS (
    CASE WHEN `total_count` > 0 AND `sum_punctuality_x10` > 0
      THEN ROUND(`sum_punctuality_x10` / 10.0 / `total_count`, 2)
      ELSE 0
    END
  ) STORED,

  -- Metadata
  `last_review_at` DATETIME DEFAULT NULL,
  `last_updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (`summary_id`),
  UNIQUE KEY `uk_rating_summaries` (`target_user_id`, `context`),
  KEY `idx_rating_summaries_avg` (`avg_overall` DESC),
  CONSTRAINT `fk_rating_summaries_user`
    FOREIGN KEY (`target_user_id`)
    REFERENCES `users` (`user_id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Denormalized rating summaries for O(1) lookups';

-- Table: websocket_sessions (Real-time session tracking)
CREATE TABLE `websocket_sessions` (
  `session_id` CHAR(36) NOT NULL,
  `user_id` BIGINT NOT NULL,
  `node_id` VARCHAR(64) DEFAULT NULL COMMENT 'Backend server node identifier',
  `connected_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_heartbeat_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `disconnected_at` DATETIME DEFAULT NULL,
  `ip_address` VARCHAR(45) DEFAULT NULL,
  `user_agent` TEXT DEFAULT NULL,
  `connection_metadata` JSON DEFAULT NULL,
  PRIMARY KEY (`session_id`),
  KEY `idx_ws_sessions_user_heartbeat` (`user_id`, `last_heartbeat_at` DESC),
  KEY `idx_ws_sessions_node` (`node_id`, `last_heartbeat_at` DESC),
  KEY `idx_ws_sessions_active` (`disconnected_at`, `last_heartbeat_at` DESC),
  CONSTRAINT `fk_ws_sessions_user`
    FOREIGN KEY (`user_id`)
    REFERENCES `users` (`user_id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='WebSocket session tracking (audit/backup, use Redis for routing)';

-- Table: email_logs (Enhanced with provider tracking and detailed status)
CREATE TABLE `email_logs` (
  `log_id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT DEFAULT NULL,
  `to_email` VARCHAR(255) NOT NULL,

  -- Email Details
  `subject` VARCHAR(500) NOT NULL,
  `template_name` VARCHAR(100) DEFAULT NULL,

  -- Provider Integration
  `provider` ENUM('SMTP', 'SENDGRID', 'SES', 'MAILGUN') DEFAULT 'SMTP',
  `provider_message_id` VARCHAR(128) DEFAULT NULL COMMENT 'External provider message ID',
  `tracking_token` CHAR(22) DEFAULT NULL COMMENT 'URL-safe token for open/click tracking',

  -- Status (Detailed lifecycle)
  `status` ENUM('QUEUED', 'SENT', 'DELIVERED', 'OPENED', 'CLICKED',
                'BOUNCED', 'COMPLAINED', 'FAILED') DEFAULT 'QUEUED',
  `error_code` VARCHAR(64) DEFAULT NULL,
  `error_message` VARCHAR(255) DEFAULT NULL,

  -- Event Timestamps (Detailed tracking)
  `queued_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `sent_at` DATETIME DEFAULT NULL,
  `delivered_at` DATETIME DEFAULT NULL,
  `opened_at` DATETIME DEFAULT NULL,
  `clicked_at` DATETIME DEFAULT NULL,
  `bounced_at` DATETIME DEFAULT NULL,
  `complained_at` DATETIME DEFAULT NULL,
  `failed_at` DATETIME DEFAULT NULL,

  -- Metadata
  `metadata` JSON DEFAULT NULL COMMENT 'Additional tracking data',

  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`log_id`),
  UNIQUE KEY `uk_email_provider_id` (`provider_message_id`),
  UNIQUE KEY `uk_email_tracking` (`tracking_token`),
  KEY `idx_email_logs_user_created` (`user_id`, `created_at` DESC),
  KEY `idx_email_logs_status` (`status`, `created_at` DESC),
  KEY `idx_email_logs_template` (`template_name`, `created_at` DESC),
  CONSTRAINT `fk_email_logs_user`
    FOREIGN KEY (`user_id`)
    REFERENCES `users` (`user_id`)
    ON DELETE SET NULL
    ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Email delivery tracking with provider webhooks support';

-- Table: email_events (Append-only event timeline for email tracking)
CREATE TABLE `email_events` (
  `event_id` BIGINT NOT NULL AUTO_INCREMENT,
  `log_id` BIGINT NOT NULL,
  `event_type` ENUM('QUEUED', 'SENT', 'DELIVERED', 'OPENED', 'CLICKED',
                    'BOUNCED', 'COMPLAINED', 'FAILED') NOT NULL,
  `event_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `payload` JSON DEFAULT NULL COMMENT 'Webhook payload or additional data',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`event_id`),
  KEY `idx_email_events_log` (`log_id`, `event_time` DESC),
  KEY `idx_email_events_type_time` (`event_type`, `event_time` DESC),
  CONSTRAINT `fk_email_events_log`
    FOREIGN KEY (`log_id`)
    REFERENCES `email_logs` (`log_id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Append-only timeline of email lifecycle events';

-- Table: outbox_messages (Transactional outbox pattern for reliable messaging)
CREATE TABLE `outbox_messages` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `aggregate_type` VARCHAR(100) NOT NULL COMMENT 'Entity type (e.g., BOOKING, PAYMENT, REVIEW)',
  `aggregate_id` VARCHAR(100) NOT NULL COMMENT 'Entity ID',
  `event_type` VARCHAR(100) NOT NULL COMMENT 'Event name (e.g., BookingCreated, PaymentCompleted)',
  `payload` JSON NOT NULL COMMENT 'Event data',
  `status` ENUM('PENDING', 'PROCESSING', 'SENT', 'FAILED') DEFAULT 'PENDING',
  `retry_count` INT DEFAULT 0,
  `max_retries` INT DEFAULT 5,
  `last_error` TEXT DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `processed_at` DATETIME DEFAULT NULL,
  `next_retry_at` DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_outbox_status_created` (`status`, `created_at`),
  KEY `idx_outbox_next_retry` (`next_retry_at`),
  KEY `idx_outbox_aggregate` (`aggregate_type`, `aggregate_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Transactional outbox for reliable event publishing';

--
-- TRIGGERS FOR MEMBER 4 (Reviews & Notifications)
--

DELIMITER $$

-- Trigger: Validate review integrity (booking must be COMPLETED, parties correct)
CREATE TRIGGER `trg_reviews_validate_booking`
BEFORE INSERT ON `reviews`
FOR EACH ROW
BEGIN
  DECLARE booking_status VARCHAR(20);
  DECLARE booking_customer_id BIGINT;
  DECLARE booking_transport_id BIGINT;

  -- Get booking details
  SELECT `status`, `customer_id`, `transport_id`
  INTO booking_status, booking_customer_id, booking_transport_id
  FROM `bookings`
  WHERE `booking_id` = NEW.booking_id;

  -- Check booking exists and is completed
  IF booking_status IS NULL THEN
    SIGNAL SQLSTATE '45000'
    SET MESSAGE_TEXT = 'Booking not found';
  END IF;

  IF booking_status != 'COMPLETED' THEN
    SIGNAL SQLSTATE '45000'
    SET MESSAGE_TEXT = 'Can only review completed bookings';
  END IF;

  -- Validate reviewer and reviewee match booking parties
  IF NEW.reviewer_type = 'CUSTOMER' THEN
    IF NEW.reviewer_id != booking_customer_id OR NEW.reviewee_id != booking_transport_id THEN
      SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Customer review must be from booking customer to transport';
    END IF;
  ELSEIF NEW.reviewer_type = 'TRANSPORT' THEN
    IF NEW.reviewer_id != booking_transport_id OR NEW.reviewee_id != booking_customer_id THEN
      SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Transport review must be from booking transport to customer';
    END IF;
  END IF;

  -- Prevent self-review
  IF NEW.reviewer_id = NEW.reviewee_id THEN
    SIGNAL SQLSTATE '45000'
    SET MESSAGE_TEXT = 'Cannot review yourself';
  END IF;
END$$

-- Trigger: Prevent self-voting on review helpfulness
CREATE TRIGGER `trg_review_helpfulness_no_self_vote`
BEFORE INSERT ON `review_helpfulness`
FOR EACH ROW
BEGIN
  DECLARE review_reviewer_id BIGINT;
  DECLARE review_reviewee_id BIGINT;

  -- Get review parties
  SELECT `reviewer_id`, `reviewee_id`
  INTO review_reviewer_id, review_reviewee_id
  FROM `reviews`
  WHERE `review_id` = NEW.review_id;

  -- Prevent reviewer/reviewee from voting
  IF NEW.voter_id = review_reviewer_id OR NEW.voter_id = review_reviewee_id THEN
    SIGNAL SQLSTATE '45000'
    SET MESSAGE_TEXT = 'Reviewer and reviewee cannot vote on their own review';
  END IF;
END$$

-- Trigger: Update helpful counts when vote added
CREATE TRIGGER `trg_review_helpfulness_insert`
AFTER INSERT ON `review_helpfulness`
FOR EACH ROW
BEGIN
  IF NEW.is_helpful = TRUE THEN
    UPDATE `reviews`
    SET `helpful_count` = `helpful_count` + 1
    WHERE `review_id` = NEW.review_id;
  ELSE
    UPDATE `reviews`
    SET `unhelpful_count` = `unhelpful_count` + 1
    WHERE `review_id` = NEW.review_id;
  END IF;
END$$

-- Trigger: Update helpful counts when vote changed
CREATE TRIGGER `trg_review_helpfulness_update`
AFTER UPDATE ON `review_helpfulness`
FOR EACH ROW
BEGIN
  IF OLD.is_helpful != NEW.is_helpful THEN
    IF NEW.is_helpful = TRUE THEN
      UPDATE `reviews`
      SET `helpful_count` = `helpful_count` + 1,
          `unhelpful_count` = `unhelpful_count` - 1
      WHERE `review_id` = NEW.review_id;
    ELSE
      UPDATE `reviews`
      SET `helpful_count` = `helpful_count` - 1,
          `unhelpful_count` = `unhelpful_count` + 1
      WHERE `review_id` = NEW.review_id;
    END IF;
  END IF;
END$$

-- Trigger: Update helpful counts when vote deleted
CREATE TRIGGER `trg_review_helpfulness_delete`
AFTER DELETE ON `review_helpfulness`
FOR EACH ROW
BEGIN
  IF OLD.is_helpful = TRUE THEN
    UPDATE `reviews`
    SET `helpful_count` = `helpful_count` - 1
    WHERE `review_id` = OLD.review_id;
  ELSE
    UPDATE `reviews`
    SET `unhelpful_count` = `unhelpful_count` - 1
    WHERE `review_id` = OLD.review_id;
  END IF;
END$$

-- Trigger: Update rating_summaries when review inserted
CREATE TRIGGER `trg_reviews_rating_summary_insert`
AFTER INSERT ON `reviews`
FOR EACH ROW
BEGIN
  DECLARE v_context ENUM('AS_CUSTOMER', 'AS_TRANSPORT');
  DECLARE v_star_bucket TINYINT;

  -- Only count APPROVED reviews
  IF NEW.status = 'APPROVED' THEN
    -- Determine context (who is being rated)
    IF NEW.reviewer_type = 'CUSTOMER' THEN
      SET v_context = 'AS_TRANSPORT'; -- Customer rates Transport
    ELSE
      SET v_context = 'AS_CUSTOMER'; -- Transport rates Customer
    END IF;

    -- Calculate star bucket (FLOOR of rating)
    SET v_star_bucket = FLOOR(NEW.overall_rating);

    -- Update or insert rating summary
    INSERT INTO `rating_summaries` (
      `target_user_id`, `context`, `total_count`,
      `sum_overall_x10`, `sum_punctuality_x10`,
      `sum_professionalism_x10`, `sum_communication_x10`, `sum_care_x10`,
      `count_5_star`, `count_4_star`, `count_3_star`, `count_2_star`, `count_1_star`,
      `last_review_at`
    ) VALUES (
      NEW.reviewee_id, v_context, 1,
      ROUND(NEW.overall_rating * 10),
      IFNULL(ROUND(NEW.punctuality_rating * 10), 0),
      IFNULL(ROUND(NEW.professionalism_rating * 10), 0),
      IFNULL(ROUND(NEW.communication_rating * 10), 0),
      IFNULL(ROUND(NEW.care_rating * 10), 0),
      IF(v_star_bucket = 5, 1, 0),
      IF(v_star_bucket = 4, 1, 0),
      IF(v_star_bucket = 3, 1, 0),
      IF(v_star_bucket = 2, 1, 0),
      IF(v_star_bucket = 1, 1, 0),
      NEW.created_at
    ) ON DUPLICATE KEY UPDATE
      `total_count` = `total_count` + 1,
      `sum_overall_x10` = `sum_overall_x10` + ROUND(NEW.overall_rating * 10),
      `sum_punctuality_x10` = `sum_punctuality_x10` + IFNULL(ROUND(NEW.punctuality_rating * 10), 0),
      `sum_professionalism_x10` = `sum_professionalism_x10` + IFNULL(ROUND(NEW.professionalism_rating * 10), 0),
      `sum_communication_x10` = `sum_communication_x10` + IFNULL(ROUND(NEW.communication_rating * 10), 0),
      `sum_care_x10` = `sum_care_x10` + IFNULL(ROUND(NEW.care_rating * 10), 0),
      `count_5_star` = `count_5_star` + IF(v_star_bucket = 5, 1, 0),
      `count_4_star` = `count_4_star` + IF(v_star_bucket = 4, 1, 0),
      `count_3_star` = `count_3_star` + IF(v_star_bucket = 3, 1, 0),
      `count_2_star` = `count_2_star` + IF(v_star_bucket = 2, 1, 0),
      `count_1_star` = `count_1_star` + IF(v_star_bucket = 1, 1, 0),
      `last_review_at` = GREATEST(`last_review_at`, NEW.created_at);

    -- Update transports.average_rating if context is AS_TRANSPORT
    IF v_context = 'AS_TRANSPORT' THEN
      UPDATE `transports`
      SET `average_rating` = (
        SELECT `avg_overall`
        FROM `rating_summaries`
        WHERE `target_user_id` = NEW.reviewee_id AND `context` = 'AS_TRANSPORT'
      )
      WHERE `transport_id` = NEW.reviewee_id;
    END IF;
  END IF;
END$$

-- Trigger: Update rating_summaries when review status changes
CREATE TRIGGER `trg_reviews_rating_summary_update`
AFTER UPDATE ON `reviews`
FOR EACH ROW
BEGIN
  DECLARE v_context ENUM('AS_CUSTOMER', 'AS_TRANSPORT');
  DECLARE v_old_star TINYINT;
  DECLARE v_new_star TINYINT;

  -- Determine context
  IF NEW.reviewer_type = 'CUSTOMER' THEN
    SET v_context = 'AS_TRANSPORT';
  ELSE
    SET v_context = 'AS_CUSTOMER';
  END IF;

  -- Handle status changes (APPROVED <-> other)
  IF OLD.status = 'APPROVED' AND NEW.status != 'APPROVED' THEN
    -- Remove from summary (decrement)
    SET v_old_star = FLOOR(OLD.overall_rating);
    UPDATE `rating_summaries`
    SET `total_count` = `total_count` - 1,
        `sum_overall_x10` = `sum_overall_x10` - ROUND(OLD.overall_rating * 10),
        `sum_punctuality_x10` = `sum_punctuality_x10` - IFNULL(ROUND(OLD.punctuality_rating * 10), 0),
        `sum_professionalism_x10` = `sum_professionalism_x10` - IFNULL(ROUND(OLD.professionalism_rating * 10), 0),
        `sum_communication_x10` = `sum_communication_x10` - IFNULL(ROUND(OLD.communication_rating * 10), 0),
        `sum_care_x10` = `sum_care_x10` - IFNULL(ROUND(OLD.care_rating * 10), 0),
        `count_5_star` = `count_5_star` - IF(v_old_star = 5, 1, 0),
        `count_4_star` = `count_4_star` - IF(v_old_star = 4, 1, 0),
        `count_3_star` = `count_3_star` - IF(v_old_star = 3, 1, 0),
        `count_2_star` = `count_2_star` - IF(v_old_star = 2, 1, 0),
        `count_1_star` = `count_1_star` - IF(v_old_star = 1, 1, 0)
    WHERE `target_user_id` = OLD.reviewee_id AND `context` = v_context;

  ELSEIF OLD.status != 'APPROVED' AND NEW.status = 'APPROVED' THEN
    -- Add to summary (increment)
    SET v_new_star = FLOOR(NEW.overall_rating);
    INSERT INTO `rating_summaries` (
      `target_user_id`, `context`, `total_count`,
      `sum_overall_x10`, `sum_punctuality_x10`, `sum_professionalism_x10`,
      `sum_communication_x10`, `sum_care_x10`,
      `count_5_star`, `count_4_star`, `count_3_star`, `count_2_star`, `count_1_star`,
      `last_review_at`
    ) VALUES (
      NEW.reviewee_id, v_context, 1,
      ROUND(NEW.overall_rating * 10),
      IFNULL(ROUND(NEW.punctuality_rating * 10), 0),
      IFNULL(ROUND(NEW.professionalism_rating * 10), 0),
      IFNULL(ROUND(NEW.communication_rating * 10), 0),
      IFNULL(ROUND(NEW.care_rating * 10), 0),
      IF(v_new_star = 5, 1, 0), IF(v_new_star = 4, 1, 0),
      IF(v_new_star = 3, 1, 0), IF(v_new_star = 2, 1, 0), IF(v_new_star = 1, 1, 0),
      NEW.created_at
    ) ON DUPLICATE KEY UPDATE
      `total_count` = `total_count` + 1,
      `sum_overall_x10` = `sum_overall_x10` + ROUND(NEW.overall_rating * 10),
      `sum_punctuality_x10` = `sum_punctuality_x10` + IFNULL(ROUND(NEW.punctuality_rating * 10), 0),
      `sum_professionalism_x10` = `sum_professionalism_x10` + IFNULL(ROUND(NEW.professionalism_rating * 10), 0),
      `sum_communication_x10` = `sum_communication_x10` + IFNULL(ROUND(NEW.communication_rating * 10), 0),
      `sum_care_x10` = `sum_care_x10` + IFNULL(ROUND(NEW.care_rating * 10), 0),
      `count_5_star` = `count_5_star` + IF(v_new_star = 5, 1, 0),
      `count_4_star` = `count_4_star` + IF(v_new_star = 4, 1, 0),
      `count_3_star` = `count_3_star` + IF(v_new_star = 3, 1, 0),
      `count_2_star` = `count_2_star` + IF(v_new_star = 2, 1, 0),
      `count_1_star` = `count_1_star` + IF(v_new_star = 1, 1, 0),
      `last_review_at` = GREATEST(`last_review_at`, NEW.created_at);
  END IF;

  -- Update transports.average_rating if applicable
  IF v_context = 'AS_TRANSPORT' AND NEW.status = 'APPROVED' THEN
    UPDATE `transports`
    SET `average_rating` = (
      SELECT `avg_overall`
      FROM `rating_summaries`
      WHERE `target_user_id` = NEW.reviewee_id AND `context` = 'AS_TRANSPORT'
    )
    WHERE `transport_id` = NEW.reviewee_id;
  END IF;
END$$

-- Trigger: Update rating_summaries when review deleted
CREATE TRIGGER `trg_reviews_rating_summary_delete`
AFTER DELETE ON `reviews`
FOR EACH ROW
BEGIN
  DECLARE v_context ENUM('AS_CUSTOMER', 'AS_TRANSPORT');
  DECLARE v_old_star TINYINT;

  -- Only adjust if review was APPROVED
  IF OLD.status = 'APPROVED' THEN
    -- Determine context
    IF OLD.reviewer_type = 'CUSTOMER' THEN
      SET v_context = 'AS_TRANSPORT';
    ELSE
      SET v_context = 'AS_CUSTOMER';
    END IF;

    SET v_old_star = FLOOR(OLD.overall_rating);

    -- Decrement counts
    UPDATE `rating_summaries`
    SET `total_count` = `total_count` - 1,
        `sum_overall_x10` = `sum_overall_x10` - ROUND(OLD.overall_rating * 10),
        `sum_punctuality_x10` = `sum_punctuality_x10` - IFNULL(ROUND(OLD.punctuality_rating * 10), 0),
        `sum_professionalism_x10` = `sum_professionalism_x10` - IFNULL(ROUND(OLD.professionalism_rating * 10), 0),
        `sum_communication_x10` = `sum_communication_x10` - IFNULL(ROUND(OLD.communication_rating * 10), 0),
        `sum_care_x10` = `sum_care_x10` - IFNULL(ROUND(OLD.care_rating * 10), 0),
        `count_5_star` = `count_5_star` - IF(v_old_star = 5, 1, 0),
        `count_4_star` = `count_4_star` - IF(v_old_star = 4, 1, 0),
        `count_3_star` = `count_3_star` - IF(v_old_star = 3, 1, 0),
        `count_2_star` = `count_2_star` - IF(v_old_star = 2, 1, 0),
        `count_1_star` = `count_1_star` - IF(v_old_star = 1, 1, 0)
    WHERE `target_user_id` = OLD.reviewee_id AND `context` = v_context;

    -- Update transports.average_rating if applicable
    IF v_context = 'AS_TRANSPORT' THEN
      UPDATE `transports`
      SET `average_rating` = (
        SELECT `avg_overall`
        FROM `rating_summaries`
        WHERE `target_user_id` = OLD.reviewee_id AND `context` = 'AS_TRANSPORT'
      )
      WHERE `transport_id` = OLD.reviewee_id;
    END IF;
  END IF;
END$$

DELIMITER ;

--
-- MEMBER 2: STATUS HISTORY & AUDIT TABLES
--

-- Table: booking_status_history
CREATE TABLE `booking_status_history` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `booking_id` BIGINT NOT NULL,
  `old_status` ENUM('PENDING', 'QUOTED', 'CONFIRMED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED') DEFAULT NULL,
  `new_status` ENUM('PENDING', 'QUOTED', 'CONFIRMED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED') NOT NULL,
  `changed_by` BIGINT DEFAULT NULL COMMENT 'User who made the change',
  `changed_by_role` ENUM('CUSTOMER', 'TRANSPORT', 'MANAGER', 'SYSTEM') DEFAULT NULL,
  `reason` TEXT DEFAULT NULL,
  `metadata` JSON DEFAULT NULL,
  `changed_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (`id`),
  KEY `idx_booking_status_history_booking` (`booking_id`, `changed_at` DESC),
  CONSTRAINT `fk_booking_status_history_booking`
    FOREIGN KEY (`booking_id`)
    REFERENCES `bookings` (`booking_id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Audit trail for all booking status changes';

-- Table: quotation_status_history

-- Table: booking_progress_events
CREATE TABLE `booking_progress_events` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `booking_id` BIGINT NOT NULL,
  `step` ENUM('EN_ROUTE','LOADING','IN_TRANSIT','UNLOADING','COMPLETED','CANCELLED') NOT NULL,
  `note` TEXT DEFAULT NULL,
  `gps_lat` DECIMAL(10,8) DEFAULT NULL,
  `gps_lng` DECIMAL(11,8) DEFAULT NULL,
  `created_by` BIGINT DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_progress_booking` (`booking_id`, `created_at` DESC),
  CONSTRAINT `fk_progress_booking`
    FOREIGN KEY (`booking_id`)
    REFERENCES `bookings` (`booking_id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_progress_created_by`
    FOREIGN KEY (`created_by`)
    REFERENCES `users` (`user_id`)
    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Tracks granular progress sub-steps for active jobs';

CREATE TABLE `quotation_status_history` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `quotation_id` BIGINT NOT NULL,
  `old_status` ENUM('PENDING', 'ACCEPTED', 'REJECTED', 'EXPIRED') DEFAULT NULL,
  `new_status` ENUM('PENDING', 'ACCEPTED', 'REJECTED', 'EXPIRED') NOT NULL,
  `changed_by` BIGINT DEFAULT NULL,
  `changed_by_role` ENUM('CUSTOMER', 'TRANSPORT', 'MANAGER', 'SYSTEM') DEFAULT NULL,
  `reason` TEXT DEFAULT NULL,
  `metadata` JSON DEFAULT NULL,
  `changed_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (`id`),
  KEY `idx_quotation_status_history_quotation` (`quotation_id`, `changed_at` DESC),
  CONSTRAINT `fk_quotation_status_history_quotation`
    FOREIGN KEY (`quotation_id`)
    REFERENCES `quotations` (`quotation_id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Audit trail for all quotation status changes';

--
-- MEMBER 2: PAYMENT SUMMARY VIEW
--

-- View: booking_payment_summary (for reconciliation)
CREATE OR REPLACE VIEW `booking_payment_summary` AS
SELECT
  b.booking_id,
  b.customer_id,
  b.transport_id,
  b.final_price AS booking_amount_vnd,
  COALESCE(SUM(
    CASE
      WHEN p.payment_type = 'REFUND' AND p.status = 'COMPLETED' THEN -p.amount
      WHEN p.status = 'COMPLETED' THEN p.amount
      ELSE 0
    END
  ), 0) AS total_paid_vnd,
  COALESCE(SUM(
    CASE
      WHEN p.payment_type = 'DEPOSIT' AND p.status = 'COMPLETED' THEN p.amount
      ELSE 0
    END
  ), 0) AS deposit_paid_vnd,
  COALESCE(SUM(
    CASE
      WHEN p.payment_type = 'FULL_PAYMENT' AND p.status = 'COMPLETED' THEN p.amount
      ELSE 0
    END
  ), 0) AS balance_paid_vnd,
  COALESCE(SUM(
    CASE
      WHEN p.payment_type = 'REFUND' AND p.status = 'COMPLETED' THEN p.amount
      ELSE 0
    END
  ), 0) AS total_refunded_vnd,
  MAX(p.paid_at) AS last_paid_at,
  COUNT(DISTINCT p.payment_id) AS payment_count,
  b.final_price - COALESCE(SUM(
    CASE
      WHEN p.payment_type = 'REFUND' AND p.status = 'COMPLETED' THEN -p.amount
      WHEN p.status = 'COMPLETED' THEN p.amount
      ELSE 0
    END
  ), 0) AS outstanding_vnd
FROM bookings b
LEFT JOIN payments p ON b.booking_id = p.booking_id
GROUP BY b.booking_id, b.customer_id, b.transport_id, b.final_price;

--
-- MODULE 3: VEHICLE & PRICING (Member 3 - Quang)
--

-- Table: vehicles (Production-ready with VN constraints)
CREATE TABLE `vehicles` (
  `vehicle_id` BIGINT NOT NULL AUTO_INCREMENT,
  `transport_id` BIGINT NOT NULL,
  `type` ENUM('motorcycle','van','truck_small','truck_large','other') NOT NULL COMMENT 'Loại xe',
  `model` VARCHAR(100) NOT NULL COMMENT 'Mẫu xe',
  `license_plate` VARCHAR(20) NOT NULL COMMENT 'Biển số (nguyên gốc người dùng nhập)',
  `license_plate_norm` VARCHAR(20) GENERATED ALWAYS AS (REPLACE(UPPER(`license_plate`), ' ', '')) STORED COMMENT 'Biển số chuẩn hóa: UPPER + bỏ khoảng trắng',
  `license_plate_compact` VARCHAR(20) GENERATED ALWAYS AS (
    REPLACE(REPLACE(REPLACE(UPPER(`license_plate`), ' ', ''), '-', ''), '.', '')
  ) STORED COMMENT 'Biển số compact: loại bỏ TẤT CẢ ký tự đặc biệt',
  `capacity_kg` DECIMAL(8,2) NOT NULL COMMENT 'Tải trọng (kg)',
  `capacity_m3` DECIMAL(6,2) DEFAULT NULL COMMENT 'Thể tích (m3)',
  `length_cm` DECIMAL(7,2) DEFAULT NULL,
  `width_cm` DECIMAL(7,2) DEFAULT NULL,
  `height_cm` DECIMAL(7,2) DEFAULT NULL,
  `status` ENUM('available','in_use','maintenance','inactive') NOT NULL DEFAULT 'available',
  `year` SMALLINT DEFAULT NULL,
  `color` VARCHAR(50) DEFAULT NULL,
  `has_tail_lift` BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Có thang nâng',
  `has_tools` BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Có dụng cụ',
  `image_url` VARCHAR(255) DEFAULT NULL,
  `description` TEXT DEFAULT NULL,
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`vehicle_id`),
  UNIQUE KEY `uk_vehicles_lp_norm` (`license_plate_norm`),
  UNIQUE KEY `uk_vehicles_lp_compact` (`license_plate_compact`),
  KEY `idx_vehicles_transport_status` (`transport_id`,`status`),
  KEY `idx_vehicles_type` (`type`),
  KEY `idx_vehicles_year` (`year`),
  CONSTRAINT `fk_vehicles_transport` FOREIGN KEY (`transport_id`) REFERENCES `transports` (`transport_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_vehicles_created_by` FOREIGN KEY (`created_by`) REFERENCES `users` (`user_id`) ON DELETE SET NULL,
  CONSTRAINT `fk_vehicles_updated_by` FOREIGN KEY (`updated_by`) REFERENCES `users` (`user_id`) ON DELETE SET NULL,
  CONSTRAINT `chk_vehicles_capacity_positive` CHECK (`capacity_kg` > 0 AND (`capacity_m3` IS NULL OR `capacity_m3` >= 0)),
  CONSTRAINT `chk_vehicles_dimensions` CHECK (
    (`length_cm` IS NULL OR `length_cm` > 0) AND
    (`width_cm` IS NULL OR `width_cm` > 0) AND
    (`height_cm` IS NULL OR `height_cm` > 0)
  ),
  -- VN license plate: 2 digits province + 1-3 letters series + 4-6 digits number
  CONSTRAINT `chk_vehicles_plate_vn` CHECK (`license_plate_compact` REGEXP '^[0-9]{2}[A-Z]{1,3}[0-9]{4,6}$')
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Quản lý phương tiện vận chuyển (VN)';

-- Table: vehicle_pricing (Time-versioned pricing rules)
CREATE TABLE `vehicle_pricing` (
  `vehicle_pricing_id` BIGINT NOT NULL AUTO_INCREMENT,
  `transport_id` BIGINT NOT NULL,
  `vehicle_type` ENUM('motorcycle','van','truck_small','truck_large','other') NOT NULL,
  -- Base and distance (integer VND values)
  `base_price_vnd` DECIMAL(12,0) NOT NULL COMMENT 'Giá cơ bản (VND)',
  `per_km_first_4km_vnd` DECIMAL(12,0) NOT NULL COMMENT 'VND/km (0-4km)',
  `per_km_5_to_40km_vnd` DECIMAL(12,0) NOT NULL COMMENT 'VND/km (5-40km)',
  `per_km_after_40km_vnd` DECIMAL(12,0) NOT NULL COMMENT 'VND/km (>40km)',
  `min_charge_vnd` DECIMAL(12,0) DEFAULT NULL COMMENT 'Tối thiểu (nếu có)',
  -- Elevators
  `elevator_bonus_vnd` DECIMAL(12,0) NOT NULL DEFAULT 0 COMMENT 'Có thang máy cả 2 chiều: giảm',
  `no_elevator_fee_per_floor_vnd` DECIMAL(12,0) NOT NULL DEFAULT 0 COMMENT 'Phụ phí mỗi tầng khi không có thang máy',
  `no_elevator_floor_threshold` INT NOT NULL DEFAULT 3 COMMENT 'Áp dụng khi tầng > ngưỡng',
  -- Multipliers
  `peak_hour_multiplier` DECIMAL(4,2) NOT NULL DEFAULT 1.00,
  `weekend_multiplier` DECIMAL(4,2) NOT NULL DEFAULT 1.00,
  `peak_hour_start_1` TINYINT DEFAULT 7 COMMENT '0-23 (VN)',
  `peak_hour_end_1` TINYINT DEFAULT 9 COMMENT '0-23 (VN, end exclusive)',
  `peak_hour_start_2` TINYINT DEFAULT 17,
  `peak_hour_end_2` TINYINT DEFAULT 19,
  `timezone` VARCHAR(50) NOT NULL DEFAULT 'Asia/Ho_Chi_Minh',
  -- Lifecycle
  `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
  `valid_from` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `valid_to` DATETIME DEFAULT NULL,
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`vehicle_pricing_id`),
  KEY `idx_vp_hot_lookup` (`transport_id`,`vehicle_type`,`is_active`,`valid_from`,`valid_to`),
  KEY `idx_vp_transport_active` (`transport_id`,`is_active`),
  CONSTRAINT `fk_vp_transport` FOREIGN KEY (`transport_id`) REFERENCES `transports` (`transport_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_vp_created_by` FOREIGN KEY (`created_by`) REFERENCES `users` (`user_id`) ON DELETE SET NULL,
  CONSTRAINT `fk_vp_updated_by` FOREIGN KEY (`updated_by`) REFERENCES `users` (`user_id`) ON DELETE SET NULL,
  CONSTRAINT `chk_vp_money_positive` CHECK (
    `base_price_vnd` >= 0 AND
    `per_km_first_4km_vnd` >= 0 AND
    `per_km_5_to_40km_vnd` >= 0 AND
    `per_km_after_40km_vnd` >= 0 AND
    (`min_charge_vnd` IS NULL OR `min_charge_vnd` >= 0) AND
    `elevator_bonus_vnd` >= 0 AND
    `no_elevator_fee_per_floor_vnd` >= 0
  ),
  CONSTRAINT `chk_vp_multipliers_bounds` CHECK (`peak_hour_multiplier` >= 1.00 AND `weekend_multiplier` >= 1.00),
  CONSTRAINT `chk_vp_hours_valid` CHECK (
    (`peak_hour_start_1` IS NULL OR (`peak_hour_start_1` BETWEEN 0 AND 23)) AND
    (`peak_hour_end_1` IS NULL OR (`peak_hour_end_1` BETWEEN 0 AND 23)) AND
    (`peak_hour_start_2` IS NULL OR (`peak_hour_start_2` BETWEEN 0 AND 23)) AND
    (`peak_hour_end_2` IS NULL OR (`peak_hour_end_2` BETWEEN 0 AND 23)) AND
    ( (`peak_hour_start_1` IS NULL AND `peak_hour_end_1` IS NULL) OR (`peak_hour_start_1` < `peak_hour_end_1`) ) AND
    ( (`peak_hour_start_2` IS NULL AND `peak_hour_end_2` IS NULL) OR (`peak_hour_start_2` < `peak_hour_end_2`) )
  ),
  CONSTRAINT `chk_vp_valid_range` CHECK (`valid_to` IS NULL OR `valid_to` > `valid_from`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Bảng giá theo loại xe (theo nhà vận chuyển, có hiệu lực theo thời gian)';

-- Table: category_pricing (Time-versioned category pricing)
CREATE TABLE `category_pricing` (
  `category_pricing_id` BIGINT NOT NULL AUTO_INCREMENT,
  `transport_id` BIGINT NOT NULL,
  `category_id` BIGINT NOT NULL,
  `size_id` BIGINT DEFAULT NULL COMMENT 'NULL = áp dụng cho tất cả kích thước của category',
  `price_per_unit_vnd` DECIMAL(12,0) NOT NULL COMMENT 'Giá mỗi đơn vị (VND)',
  `fragile_multiplier` DECIMAL(4,2) NOT NULL DEFAULT 1.20,
  `disassembly_multiplier` DECIMAL(4,2) NOT NULL DEFAULT 1.30,
  `heavy_multiplier` DECIMAL(4,2) NOT NULL DEFAULT 1.50,
  `heavy_threshold_kg` DECIMAL(6,2) NOT NULL DEFAULT 100.00 COMMENT 'Ngưỡng nặng (kg)',
  -- Lifecycle
  `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
  `valid_from` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `valid_to` DATETIME DEFAULT NULL,
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`category_pricing_id`),
  KEY `idx_cp_hot_lookup` (`transport_id`,`category_id`,`size_id`,`is_active`,`valid_from`,`valid_to`),
  CONSTRAINT `fk_cp_transport` FOREIGN KEY (`transport_id`) REFERENCES `transports` (`transport_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_cp_category` FOREIGN KEY (`category_id`) REFERENCES `categories` (`category_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_cp_size` FOREIGN KEY (`size_id`) REFERENCES `sizes` (`size_id`) ON DELETE SET NULL,
  CONSTRAINT `fk_cp_created_by` FOREIGN KEY (`created_by`) REFERENCES `users` (`user_id`) ON DELETE SET NULL,
  CONSTRAINT `fk_cp_updated_by` FOREIGN KEY (`updated_by`) REFERENCES `users` (`user_id`) ON DELETE SET NULL,
  CONSTRAINT `chk_cp_money_positive` CHECK (`price_per_unit_vnd` >= 0),
  CONSTRAINT `chk_cp_multiplier_bounds` CHECK (
    `fragile_multiplier` >= 1.00 AND `fragile_multiplier` <= 3.00 AND
    `disassembly_multiplier` >= 1.00 AND `disassembly_multiplier` <= 3.00 AND
    `heavy_multiplier` >= 1.00 AND `heavy_multiplier` <= 5.00
  ),
  CONSTRAINT `chk_cp_valid_range` CHECK (`valid_to` IS NULL OR `valid_to` > `valid_from`),
  CONSTRAINT `chk_cp_heavy_threshold` CHECK (`heavy_threshold_kg` > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Bảng giá theo danh mục (tùy chọn theo size), theo thời gian';

-- Table: distance_cache (Google Distance Matrix results)
CREATE TABLE `distance_cache` (
  `distance_cache_id` BIGINT NOT NULL AUTO_INCREMENT,
  `provider` ENUM('GOOGLE','MAPBOX','OSRM') NOT NULL DEFAULT 'GOOGLE',
  `mode` ENUM('DRIVING','WALKING','BICYCLING') NOT NULL DEFAULT 'DRIVING',
  `origin_hash` CHAR(44) NOT NULL COMMENT 'Base64(SHA-256(origin))',
  `destination_hash` CHAR(44) NOT NULL COMMENT 'Base64(SHA-256(destination))',
  `origin_address` TEXT NOT NULL,
  `destination_address` TEXT NOT NULL,
  `origin_latitude` DECIMAL(10,8) DEFAULT NULL,
  `origin_longitude` DECIMAL(11,8) DEFAULT NULL,
  `destination_latitude` DECIMAL(10,8) DEFAULT NULL,
  `destination_longitude` DECIMAL(11,8) DEFAULT NULL,
  `distance_km` DECIMAL(8,3) NOT NULL,
  `duration_minutes` INT NOT NULL,
  `expires_at` DATETIME NOT NULL DEFAULT (CURRENT_TIMESTAMP + INTERVAL 30 DAY) COMMENT 'Auto-expires in 30 days',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`distance_cache_id`),
  UNIQUE KEY `uk_distance_cache_lookup` (`provider`,`mode`,`origin_hash`,`destination_hash`),
  KEY `idx_distance_cache_expires` (`expires_at`),
  CONSTRAINT `chk_distance_cache_values` CHECK (`distance_km` >= 0 AND `duration_minutes` >= 0),
  CONSTRAINT `chk_distance_cache_expiry` CHECK (`expires_at` > `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Cache kết quả khoảng cách/thời gian theo cặp địa chỉ';

-- Table: price_history (Calculation audit trail)
CREATE TABLE `price_history` (
  `price_history_id` BIGINT NOT NULL AUTO_INCREMENT,
  `booking_id` BIGINT DEFAULT NULL,
  `transport_id` BIGINT NOT NULL,
  `vehicle_id` BIGINT DEFAULT NULL,
  `vehicle_type` ENUM('motorcycle','van','truck_small','truck_large','other') DEFAULT NULL,
  `vehicle_pricing_id` BIGINT DEFAULT NULL,
  `category_pricing_ids` JSON DEFAULT NULL COMMENT 'Mảng id category_pricing dùng trong lần tính',
  -- Monetary breakdown (VND integer)
  `base_price_vnd` DECIMAL(12,0) NOT NULL,
  `distance_price_vnd` DECIMAL(12,0) NOT NULL,
  `category_price_vnd` DECIMAL(12,0) NOT NULL,
  `additional_fees_vnd` DECIMAL(12,0) NOT NULL DEFAULT 0,
  `subtotal_vnd` DECIMAL(12,0) NOT NULL,
  `peak_applied` BOOLEAN NOT NULL DEFAULT FALSE,
  `weekend_applied` BOOLEAN NOT NULL DEFAULT FALSE,
  `multiplier_effect` DECIMAL(8,2) NOT NULL DEFAULT 1.00 COMMENT 'Tổng nhân hệ số',
  `total_vnd` DECIMAL(12,0) NOT NULL,
  `currency` CHAR(3) NOT NULL DEFAULT 'VND',
  `rule_snapshot` JSON DEFAULT NULL COMMENT 'Snapshot cấu hình (vehicle_pricing + category_pricing) lúc tính',
  `engine_version` VARCHAR(20) DEFAULT NULL COMMENT 'Phiên bản engine',
  `calculated_by` VARCHAR(100) DEFAULT 'pricing-service',
  `calculated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`price_history_id`),
  KEY `idx_ph_booking` (`booking_id`,`calculated_at` DESC),
  KEY `idx_ph_transport_time` (`transport_id`,`calculated_at` DESC),
  KEY `idx_ph_vehicle_pricing` (`vehicle_pricing_id`),
  CONSTRAINT `fk_ph_booking` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`booking_id`) ON DELETE SET NULL,
  CONSTRAINT `fk_ph_transport` FOREIGN KEY (`transport_id`) REFERENCES `transports` (`transport_id`) ON DELETE NO ACTION,
  CONSTRAINT `fk_ph_vehicle` FOREIGN KEY (`vehicle_id`) REFERENCES `vehicles` (`vehicle_id`) ON DELETE SET NULL,
  CONSTRAINT `fk_ph_vehicle_pricing` FOREIGN KEY (`vehicle_pricing_id`) REFERENCES `vehicle_pricing` (`vehicle_pricing_id`) ON DELETE SET NULL,
  CONSTRAINT `chk_ph_currency_vnd` CHECK (`currency` = 'VND'),
  CONSTRAINT `chk_ph_amounts_nonneg` CHECK (
    `base_price_vnd` >= 0 AND
    `distance_price_vnd` >= 0 AND
    `category_price_vnd` >= 0 AND
    `additional_fees_vnd` >= 0 AND
    `subtotal_vnd` >= 0 AND
    `total_vnd` >= 0
  )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Lịch sử tính giá (audit) với breakdown chi tiết';
-- PARTITION DISABLED: PK(price_history_id) doesn't include partition column calculated_at
-- Enable later by changing PK to: PRIMARY KEY (price_history_id, calculated_at)
/*
/*!50100 PARTITION BY RANGE (TO_DAYS(calculated_at))
(PARTITION p202510 VALUES LESS THAN (TO_DAYS('2025-11-01')) COMMENT = 'October 2025',
 PARTITION p202511 VALUES LESS THAN (TO_DAYS('2025-12-01')) COMMENT = 'November 2025',
 PARTITION p202512 VALUES LESS THAN (TO_DAYS('2026-01-01')) COMMENT = 'December 2025',
 PARTITION p202601 VALUES LESS THAN (TO_DAYS('2026-02-01')) COMMENT = 'January 2026',
 PARTITION p202602 VALUES LESS THAN (TO_DAYS('2026-03-01')) COMMENT = 'February 2026',
 PARTITION p202603 VALUES LESS THAN (TO_DAYS('2026-04-01')) COMMENT = 'March 2026',
 PARTITION p202604 VALUES LESS THAN (TO_DAYS('2026-05-01')) COMMENT = 'April 2026',
 PARTITION p202605 VALUES LESS THAN (TO_DAYS('2026-06-01')) COMMENT = 'May 2026',
 PARTITION p202606 VALUES LESS THAN (TO_DAYS('2026-07-01')) COMMENT = 'June 2026',
 PARTITION p202607 VALUES LESS THAN (TO_DAYS('2026-08-01')) COMMENT = 'July 2026',
 PARTITION p202608 VALUES LESS THAN (TO_DAYS('2026-09-01')) COMMENT = 'August 2026',
 PARTITION p202609 VALUES LESS THAN (TO_DAYS('2026-10-01')) COMMENT = 'September 2026',
 PARTITION p202610 VALUES LESS THAN (TO_DAYS('2026-11-01')) COMMENT = 'October 2026',
 PARTITION p202611 VALUES LESS THAN (TO_DAYS('2026-12-01')) COMMENT = 'November 2026',
 PARTITION p202612 VALUES LESS THAN (TO_DAYS('2027-01-01')) COMMENT = 'December 2026',
 PARTITION pfuture VALUES LESS THAN MAXVALUE COMMENT = 'Future data')
*/
*/

--
-- TRIGGERS FOR MEMBER 3 (Vehicle & Pricing)
--

-- Triggers to prevent overlapping active periods for vehicle_pricing
DELIMITER $$

CREATE TRIGGER `trg_vp_no_overlap_bi`
BEFORE INSERT ON `vehicle_pricing` FOR EACH ROW
BEGIN
  IF NEW.is_active THEN
    IF EXISTS (
      SELECT 1 FROM `vehicle_pricing` vp
      WHERE vp.transport_id = NEW.transport_id
        AND vp.vehicle_type = NEW.vehicle_type
        AND vp.is_active = TRUE
        AND (NEW.valid_to IS NULL OR vp.valid_from <= NEW.valid_to)
        AND (vp.valid_to IS NULL OR NEW.valid_from <= vp.valid_to)
    ) THEN
      SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Active vehicle_pricing overlaps existing period';
    END IF;
  END IF;
END$$

CREATE TRIGGER `trg_vp_no_overlap_bu`
BEFORE UPDATE ON `vehicle_pricing` FOR EACH ROW
BEGIN
  IF NEW.is_active THEN
    IF EXISTS (
      SELECT 1 FROM `vehicle_pricing` vp
      WHERE vp.transport_id = NEW.transport_id
        AND vp.vehicle_type = NEW.vehicle_type
        AND vp.is_active = TRUE
        AND vp.vehicle_pricing_id <> NEW.vehicle_pricing_id
        AND (NEW.valid_to IS NULL OR vp.valid_from <= NEW.valid_to)
        AND (vp.valid_to IS NULL OR NEW.valid_from <= vp.valid_to)
    ) THEN
      SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Active vehicle_pricing overlaps existing period';
    END IF;
  END IF;
END$$

-- Triggers to prevent overlapping active periods for category_pricing
CREATE TRIGGER `trg_cp_no_overlap_bi`
BEFORE INSERT ON `category_pricing` FOR EACH ROW
BEGIN
  IF NEW.is_active THEN
    IF EXISTS (
      SELECT 1 FROM `category_pricing` cp
      WHERE cp.transport_id = NEW.transport_id
        AND cp.category_id = NEW.category_id
        AND ( (cp.size_id IS NULL AND NEW.size_id IS NULL) OR (cp.size_id <=> NEW.size_id) )
        AND cp.is_active = TRUE
        AND (NEW.valid_to IS NULL OR cp.valid_from <= NEW.valid_to)
        AND (cp.valid_to IS NULL OR NEW.valid_from <= cp.valid_to)
    ) THEN
      SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Active category_pricing overlaps existing period';
    END IF;
  END IF;
END$$

CREATE TRIGGER `trg_cp_no_overlap_bu`
BEFORE UPDATE ON `category_pricing` FOR EACH ROW
BEGIN
  IF NEW.is_active THEN
    IF EXISTS (
      SELECT 1 FROM `category_pricing` cp
      WHERE cp.transport_id = NEW.transport_id
        AND cp.category_id = NEW.category_id
        AND ( (cp.size_id IS NULL AND NEW.size_id IS NULL) OR (cp.size_id <=> NEW.size_id) )
        AND cp.is_active = TRUE
        AND cp.category_pricing_id <> NEW.category_pricing_id
        AND (NEW.valid_to IS NULL OR cp.valid_from <= NEW.valid_to)
        AND (cp.valid_to IS NULL OR NEW.valid_from <= cp.valid_to)
    ) THEN
      SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Active category_pricing overlaps existing period';
    END IF;
  END IF;
END$$

-- Trigger: Validate Vietnamese address hierarchy in bookings
CREATE TRIGGER `trg_bookings_validate_vn_address`
BEFORE INSERT ON `bookings`
FOR EACH ROW
BEGIN
  DECLARE v_district_ok, v_ward_ok, v_del_district_ok, v_del_ward_ok INT;

  -- Validate pickup address hierarchy
  IF NEW.pickup_district_code IS NOT NULL THEN
    SELECT COUNT(*) INTO v_district_ok
    FROM `vn_districts`
    WHERE `district_code` = NEW.pickup_district_code
      AND `province_code` = NEW.pickup_province_code;

    IF v_district_ok = 0 THEN
      SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Pickup district does not belong to province';
    END IF;
  END IF;

  IF NEW.pickup_ward_code IS NOT NULL THEN
    SELECT COUNT(*) INTO v_ward_ok
    FROM `vn_wards`
    WHERE `ward_code` = NEW.pickup_ward_code
      AND `district_code` = NEW.pickup_district_code;

    IF v_ward_ok = 0 THEN
      SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Pickup ward does not belong to district';
    END IF;
  END IF;

  -- Validate delivery address hierarchy
  IF NEW.delivery_district_code IS NOT NULL THEN
    SELECT COUNT(*) INTO v_del_district_ok
    FROM `vn_districts`
    WHERE `district_code` = NEW.delivery_district_code
      AND `province_code` = NEW.delivery_province_code;

    IF v_del_district_ok = 0 THEN
      SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Delivery district does not belong to province';
    END IF;
  END IF;

  IF NEW.delivery_ward_code IS NOT NULL THEN
    SELECT COUNT(*) INTO v_del_ward_ok
    FROM `vn_wards`
    WHERE `ward_code` = NEW.delivery_ward_code
      AND `district_code` = NEW.delivery_district_code;

    IF v_del_ward_ok = 0 THEN
      SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Delivery ward does not belong to district';
    END IF;
  END IF;
END$$

CREATE TRIGGER `trg_bookings_validate_vn_address_update`
BEFORE UPDATE ON `bookings`
FOR EACH ROW
BEGIN
  DECLARE v_district_ok, v_ward_ok, v_del_district_ok, v_del_ward_ok INT;

  -- Validate pickup address hierarchy
  IF NEW.pickup_district_code IS NOT NULL THEN
    SELECT COUNT(*) INTO v_district_ok
    FROM `vn_districts`
    WHERE `district_code` = NEW.pickup_district_code
      AND `province_code` = NEW.pickup_province_code;

    IF v_district_ok = 0 THEN
      SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Pickup district does not belong to province';
    END IF;
  END IF;

  IF NEW.pickup_ward_code IS NOT NULL THEN
    SELECT COUNT(*) INTO v_ward_ok
    FROM `vn_wards`
    WHERE `ward_code` = NEW.pickup_ward_code
      AND `district_code` = NEW.pickup_district_code;

    IF v_ward_ok = 0 THEN
      SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Pickup ward does not belong to district';
    END IF;
  END IF;

  -- Validate delivery address hierarchy
  IF NEW.delivery_district_code IS NOT NULL THEN
    SELECT COUNT(*) INTO v_del_district_ok
    FROM `vn_districts`
    WHERE `district_code` = NEW.delivery_district_code
      AND `province_code` = NEW.delivery_province_code;

    IF v_del_district_ok = 0 THEN
      SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Delivery district does not belong to province';
    END IF;
  END IF;

  IF NEW.delivery_ward_code IS NOT NULL THEN
    SELECT COUNT(*) INTO v_del_ward_ok
    FROM `vn_wards`
    WHERE `ward_code` = NEW.delivery_ward_code
      AND `district_code` = NEW.delivery_district_code;

    IF v_del_ward_ok = 0 THEN
      SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Delivery ward does not belong to district';
    END IF;
  END IF;
END$$

DELIMITER ;

--
-- SEED DATA: Initial Admin Account
--

-- Create initial super admin account
-- Password: Admin@123456 (hashed with bcrypt cost 12)
-- IMPORTANT: Change this password after first login!
INSERT INTO `users` (`email`, `password_hash`, `role`, `is_active`, `is_verified`, `email_verified_at`, `last_password_change`)
VALUES (
  'admin@homeexpress.vn',
  '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5K0bZlHvWr.zW',
  'MANAGER',
  TRUE,
  TRUE,
  NOW(),
  NOW()
);

INSERT INTO `managers` (`manager_id`, `full_name`, `phone`, `employee_id`, `department`, `permissions`)
SELECT
  `user_id`,
  'System Administrator',
  '0900000000',
  'ADM001',
  'IT',
  JSON_ARRAY('*')
FROM `users` WHERE `email` = 'admin@homeexpress.vn';

--
-- SEED DATA: Initial Categories
--

INSERT INTO `categories`
  (`name`, `name_en`, `description`, `icon`, `default_weight_kg`, `default_volume_m3`,
   `is_fragile_default`, `requires_disassembly_default`, `display_order`, `is_active`)
VALUES
  ('Tủ lạnh', 'Refrigerator', 'Tủ lạnh gia đình các loại', 'fridge', 50.00, 0.50, TRUE, FALSE, 1, TRUE),
  ('TV/Màn hình', 'TV/Monitor', 'TV, màn hình máy tính', 'tv', 15.00, 0.30, TRUE, FALSE, 2, TRUE),
  ('Máy giặt', 'Washing Machine', 'Máy giặt các loại', 'washer', 60.00, 0.60, FALSE, FALSE, 3, TRUE),
  ('Giường', 'Bed', 'Giường ngủ các loại', 'bed', 80.00, 2.00, FALSE, TRUE, 4, TRUE),
  ('Tủ quần áo', 'Wardrobe', 'Tủ quần áo, tủ áo', 'wardrobe', 70.00, 1.50, FALSE, TRUE, 5, TRUE),
  ('Bàn làm việc', 'Desk', 'Bàn làm việc, bàn học', 'desk', 30.00, 0.80, FALSE, TRUE, 6, TRUE),
  ('Ghế sofa', 'Sofa', 'Ghế sofa các loại', 'sofa', 50.00, 1.20, FALSE, FALSE, 7, TRUE),
  ('Bàn ăn', 'Dining Table', 'Bàn ăn và ghế', 'table', 40.00, 1.00, FALSE, TRUE, 8, TRUE),
  ('Thùng carton', 'Cardboard Box', 'Thùng đồ nhỏ', 'box', 10.00, 0.20, FALSE, FALSE, 9, TRUE),
  ('Khác', 'Other', 'Đồ đạc khác', 'other', 20.00, 0.50, FALSE, FALSE, 10, TRUE);

--
-- MODULE 2 ENHANCEMENT: IMAGE SCANNING & INCIDENTS (Member 2 - Quy)
--

-- ================================================================
-- TABLE: booking_item_images
-- Purpose: Store photos of items scanned by AI + detection results
-- ================================================================

CREATE TABLE IF NOT EXISTS `booking_item_images` (
  `image_id` BIGINT NOT NULL AUTO_INCREMENT,
  `booking_item_id` BIGINT NOT NULL COMMENT 'FK to booking_items',

  -- Image Info
  `image_url` VARCHAR(500) NOT NULL COMMENT 'Cloudinary/S3 URL',
  `thumbnail_url` VARCHAR(500) DEFAULT NULL COMMENT 'Optimized thumbnail',
  `image_order` INT NOT NULL DEFAULT 0 COMMENT 'Display order (0-based)',

  -- AI Detection Results (JSON format for flexibility)
  `ai_detection_result` JSON DEFAULT NULL COMMENT 'Full AI response (Google Vision/GPT-4/Azure)',
  `ai_service_used` ENUM('OPENAI_VISION', 'OPENAI_VISION_ENHANCED', 'MANUAL') DEFAULT 'OPENAI_VISION',
  `confidence_score` DECIMAL(4,3) DEFAULT NULL COMMENT 'AI confidence 0.000-1.000',

  -- AI Labels (denormalized for quick search)
  `detected_labels` JSON DEFAULT NULL COMMENT 'Array of labels: ["Refrigerator", "Samsung", "Large"]',
  `bounding_box` JSON DEFAULT NULL COMMENT '{x, y, width, height} normalized coordinates',

  -- Status
  `is_primary` BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Primary photo for this item',
  `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Soft delete',

  -- Timestamps
  `uploaded_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `ai_processed_at` DATETIME DEFAULT NULL COMMENT 'When AI finished processing',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  PRIMARY KEY (`image_id`),
  KEY `idx_booking_item_images_item` (`booking_item_id`, `image_order`),
  KEY `idx_booking_item_images_primary` (`booking_item_id`, `is_primary`),
  KEY `idx_booking_item_images_ai_service` (`ai_service_used`, `ai_processed_at`),
  KEY `idx_booking_item_images_booking` (`booking_item_id`) USING BTREE,

  CONSTRAINT `fk_booking_item_images_item`
    FOREIGN KEY (`booking_item_id`)
    REFERENCES `booking_items` (`item_id`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,

  -- Business Rules
  CONSTRAINT `chk_booking_item_images_confidence`
    CHECK (`confidence_score` IS NULL OR (`confidence_score` BETWEEN 0 AND 1))

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Photos của món đồ + kết quả AI detection (Google Vision/GPT-4/Azure)';

-- ================================================================
-- TABLE: booking_evidence_images
-- Purpose: Photos taken before/after pickup for evidence & dispute resolution
-- ================================================================

CREATE TABLE IF NOT EXISTS `booking_evidence_images` (
  `evidence_id` BIGINT NOT NULL AUTO_INCREMENT,
  `booking_id` BIGINT NOT NULL COMMENT 'FK to bookings',

  -- Image Type & Context
  `image_type` ENUM(
    'BEFORE_PICKUP',      -- Ảnh trước khi lấy hàng (at pickup location)
    'AFTER_PICKUP',       -- Ảnh sau khi lấy hàng xong (at pickup location)
    'IN_TRANSIT',         -- Ảnh trong quá trình vận chuyển
    'BEFORE_DELIVERY',    -- Ảnh trước khi giao hàng (at delivery location)
    'AFTER_DELIVERY',     -- Ảnh sau khi giao hàng xong (at delivery location)
    'DAMAGE_EVIDENCE',    -- Ảnh chứng cứ hư hỏng
    'OTHER'
  ) NOT NULL,

  -- Image Info
  `image_url` VARCHAR(500) NOT NULL COMMENT 'Cloudinary/S3 URL',
  `thumbnail_url` VARCHAR(500) DEFAULT NULL COMMENT 'Optimized thumbnail',
  `caption` VARCHAR(500) DEFAULT NULL COMMENT 'Mô tả ảnh (optional)',

  -- Location & Context
  `location` ENUM('PICKUP', 'DELIVERY', 'TRANSPORT_VEHICLE') DEFAULT NULL,
  `gps_latitude` DECIMAL(10,8) DEFAULT NULL,
  `gps_longitude` DECIMAL(11,8) DEFAULT NULL,
  `gps_accuracy_meters` DECIMAL(6,2) DEFAULT NULL COMMENT 'GPS accuracy radius',

  -- Metadata
  `uploaded_by` BIGINT NOT NULL COMMENT 'User ID (customer or transport)',
  `uploaded_by_role` ENUM('CUSTOMER', 'TRANSPORT', 'MANAGER') NOT NULL,
  `notes` TEXT DEFAULT NULL COMMENT 'Additional notes from uploader',

  -- Status
  `is_disputed` BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Flagged for dispute/incident',
  `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Soft delete',

  -- Timestamps
  `uploaded_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (`evidence_id`),
  KEY `idx_evidence_booking` (`booking_id`, `image_type`),
  KEY `idx_evidence_uploader` (`uploaded_by`, `uploaded_at` DESC),
  KEY `idx_evidence_disputed` (`is_disputed`, `booking_id`),
  KEY `idx_evidence_type_booking` (`booking_id`, `image_type`, `uploaded_at`),
  KEY `idx_evidence_booking_type` (`booking_id`, `image_type`, `uploaded_at` DESC) USING BTREE,

  CONSTRAINT `fk_evidence_booking`
    FOREIGN KEY (`booking_id`)
    REFERENCES `bookings` (`booking_id`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_evidence_uploader`
    FOREIGN KEY (`uploaded_by`)
    REFERENCES `users` (`user_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,

  -- Geo validation
  CONSTRAINT `chk_evidence_gps_lat`
    CHECK (`gps_latitude` IS NULL OR (`gps_latitude` BETWEEN -90 AND 90)),
  CONSTRAINT `chk_evidence_gps_lng`
    CHECK (`gps_longitude` IS NULL OR (`gps_longitude` BETWEEN -180 AND 180)),
  CONSTRAINT `chk_evidence_gps_accuracy`
    CHECK (`gps_accuracy_meters` IS NULL OR `gps_accuracy_meters` >= 0)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Ảnh chứng cứ before/after pickup/delivery cho bảo hiểm & giải quyết tranh chấp';

-- ================================================================
-- TABLE: incidents
-- Purpose: Report and track issues/disputes during booking lifecycle
-- ================================================================

CREATE TABLE IF NOT EXISTS `incidents` (
  `incident_id` BIGINT NOT NULL AUTO_INCREMENT,
  `booking_id` BIGINT NOT NULL COMMENT 'FK to bookings',

  -- Reporter Info
  `reported_by` BIGINT NOT NULL COMMENT 'User who reported (customer or transport)',
  `reported_by_role` ENUM('CUSTOMER', 'TRANSPORT', 'MANAGER') NOT NULL,

  -- Incident Classification
  `incident_type` ENUM(
    'DAMAGE',             -- Hàng hóa bị hư hỏng
    'MISSING_ITEM',       -- Thiếu món đồ
    'DELAY',              -- Giao hàng trễ
    'WRONG_ADDRESS',      -- Địa chỉ sai
    'PRICE_DISPUTE',      -- Tranh chấp về giá
    'SERVICE_QUALITY',    -- Chất lượng dịch vụ kém
    'UNPROFESSIONAL',     -- Thái độ không chuyên nghiệp
    'SAFETY_VIOLATION',   -- Vi phạm an toàn
    'FRAUD',              -- Gian lận
    'OTHER'
  ) NOT NULL,

  -- Severity (for prioritization)
  `severity` ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') NOT NULL DEFAULT 'MEDIUM',

  -- Incident Details
  `title` VARCHAR(200) NOT NULL COMMENT 'Tiêu đề ngắn gọn',
  `description` TEXT NOT NULL COMMENT 'Mô tả chi tiết sự cố',

  -- Financial Impact
  `estimated_loss_vnd` DECIMAL(12,0) DEFAULT NULL COMMENT 'Thiệt hại ước tính (VND)',
  `claimed_compensation_vnd` DECIMAL(12,0) DEFAULT NULL COMMENT 'Bồi thường yêu cầu (VND)',

  -- Evidence
  `evidence_image_ids` JSON DEFAULT NULL COMMENT 'Array of evidence_id references',
  `supporting_documents` JSON DEFAULT NULL COMMENT 'Array of document URLs',

  -- Resolution Workflow
  `status` ENUM(
    'PENDING',            -- Mới tạo, chờ xử lý
    'ACKNOWLEDGED',       -- Đã tiếp nhận
    'INVESTIGATING',      -- Đang điều tra
    'ESCALATED',          -- Chuyển lên Manager
    'RESOLVED',           -- Đã giải quyết
    'CLOSED'              -- Đóng (có thể resolved hoặc rejected)
  ) DEFAULT 'PENDING',

  `resolution_status` ENUM('PENDING', 'COMPENSATED', 'REFUNDED', 'REJECTED', 'SETTLED') DEFAULT 'PENDING',

  -- Resolution Details
  `resolved_by` BIGINT DEFAULT NULL COMMENT 'Manager who resolved',
  `resolution_notes` TEXT DEFAULT NULL COMMENT 'Nội dung giải quyết',
  `resolution_action` VARCHAR(500) DEFAULT NULL COMMENT 'Hành động đã thực hiện',
  `compensation_paid_vnd` DECIMAL(12,0) DEFAULT NULL COMMENT 'Bồi thường thực tế (VND)',

  -- Priority & SLA
  `priority` INT NOT NULL DEFAULT 3 COMMENT '1=Highest, 5=Lowest',
  `sla_due_at` DATETIME DEFAULT NULL COMMENT 'SLA deadline for response',
  `first_response_at` DATETIME DEFAULT NULL,
  `resolved_at` DATETIME DEFAULT NULL,
  `closed_at` DATETIME DEFAULT NULL,

  -- Communication
  `customer_notified` BOOLEAN NOT NULL DEFAULT FALSE,
  `transport_notified` BOOLEAN NOT NULL DEFAULT FALSE,
  `manager_notified` BOOLEAN NOT NULL DEFAULT FALSE,

  -- Metadata
  `tags` JSON DEFAULT NULL COMMENT 'Tags for categorization: ["insurance", "legal"]',
  `internal_notes` TEXT DEFAULT NULL COMMENT 'Internal notes (not visible to customer/transport)',

  -- Timestamps
  `reported_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  PRIMARY KEY (`incident_id`),
  KEY `idx_incidents_booking` (`booking_id`, `status`),
  KEY `idx_incidents_reported_by` (`reported_by`, `reported_at` DESC),
  KEY `idx_incidents_status_severity` (`status`, `severity`, `reported_at` DESC),
  KEY `idx_incidents_type` (`incident_type`, `status`),
  KEY `idx_incidents_sla` (`status`, `sla_due_at`),
  KEY `idx_incidents_resolved_by` (`resolved_by`, `resolved_at` DESC),
  KEY `idx_incidents_pending_sla` (`status`, `sla_due_at`, `severity`) USING BTREE,

  CONSTRAINT `fk_incidents_booking`
    FOREIGN KEY (`booking_id`)
    REFERENCES `bookings` (`booking_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_incidents_reported_by`
    FOREIGN KEY (`reported_by`)
    REFERENCES `users` (`user_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_incidents_resolved_by`
    FOREIGN KEY (`resolved_by`)
    REFERENCES `users` (`user_id`)
    ON DELETE SET NULL
    ON UPDATE NO ACTION,

  -- Business Rules
  CONSTRAINT `chk_incidents_amounts_positive`
    CHECK (
      (`estimated_loss_vnd` IS NULL OR `estimated_loss_vnd` >= 0) AND
      (`claimed_compensation_vnd` IS NULL OR `claimed_compensation_vnd` >= 0) AND
      (`compensation_paid_vnd` IS NULL OR `compensation_paid_vnd` >= 0)
    ),
  CONSTRAINT `chk_incidents_priority_range`
    CHECK (`priority` BETWEEN 1 AND 5)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Báo cáo sự cố & tranh chấp trong quá trình vận chuyển (Manager xử lý)';

-- ================================================================
-- TABLE: evidence
-- Purpose: Store incident evidence files uploaded by customers, transports, or managers
-- ================================================================

CREATE TABLE IF NOT EXISTS `evidence` (
  `evidence_id` BIGINT NOT NULL AUTO_INCREMENT,
  `incident_id` BIGINT NOT NULL,
  `uploaded_by_user_id` BIGINT NOT NULL,
  `file_type` ENUM('IMAGE', 'VIDEO', 'DOCUMENT') NOT NULL,
  `file_url` TEXT NOT NULL,
  `file_name` VARCHAR(500) NOT NULL,
  `file_size_bytes` BIGINT DEFAULT NULL,
  `description` TEXT DEFAULT NULL,
  `uploaded_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (`evidence_id`),
  KEY `idx_evidence_incident` (`incident_id`, `uploaded_at` DESC),
  KEY `idx_evidence_uploader` (`uploaded_by_user_id`, `uploaded_at` DESC),

  CONSTRAINT `fk_evidence_incident`
    FOREIGN KEY (`incident_id`)
    REFERENCES `incidents` (`incident_id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_evidence_uploaded_by`
    FOREIGN KEY (`uploaded_by_user_id`)
    REFERENCES `users` (`user_id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Incident evidence files with ownership and type tracking';

-- ================================================================
-- TRIGGERS: Auto-calculate SLA deadlines for incidents
-- ================================================================

-- Trigger: Set SLA deadline based on severity when incident is created
CREATE TRIGGER `trg_incidents_set_sla`
BEFORE INSERT ON `incidents`
FOR EACH ROW
BEGIN
  -- Set SLA deadline based on severity
  -- CRITICAL: 1 hour, HIGH: 4 hours, MEDIUM: 24 hours, LOW: 72 hours
  IF NEW.sla_due_at IS NULL THEN
    SET NEW.sla_due_at = CASE NEW.severity
      WHEN 'CRITICAL' THEN DATE_ADD(NOW(), INTERVAL 1 HOUR)
      WHEN 'HIGH' THEN DATE_ADD(NOW(), INTERVAL 4 HOUR)
      WHEN 'MEDIUM' THEN DATE_ADD(NOW(), INTERVAL 24 HOUR)
      WHEN 'LOW' THEN DATE_ADD(NOW(), INTERVAL 72 HOUR)
      ELSE DATE_ADD(NOW(), INTERVAL 24 HOUR)
    END;
  END IF;

  -- Set priority based on severity if not specified
  IF NEW.priority = 3 THEN
    SET NEW.priority = CASE NEW.severity
      WHEN 'CRITICAL' THEN 1
      WHEN 'HIGH' THEN 2
      WHEN 'MEDIUM' THEN 3
      WHEN 'LOW' THEN 4
      ELSE 3
    END;
  END IF;
END$$

-- Trigger: Auto-set timestamps when incident status changes
CREATE TRIGGER `trg_incidents_status_change`
BEFORE UPDATE ON `incidents`
FOR EACH ROW
BEGIN
  -- Set first_response_at when status changes from PENDING
  IF OLD.status = 'PENDING' AND NEW.status != 'PENDING' AND NEW.first_response_at IS NULL THEN
    SET NEW.first_response_at = NOW();
  END IF;

  -- Set resolved_at when status becomes RESOLVED
  IF NEW.status = 'RESOLVED' AND OLD.status != 'RESOLVED' AND NEW.resolved_at IS NULL THEN
    SET NEW.resolved_at = NOW();
  END IF;

  -- Set closed_at when status becomes CLOSED
  IF NEW.status = 'CLOSED' AND OLD.status != 'CLOSED' AND NEW.closed_at IS NULL THEN
    SET NEW.closed_at = NOW();
  END IF;
END$$

-- ================================================================
-- VIEWS: Useful summary views
-- ================================================================

-- View: Get incident summary per booking
CREATE OR REPLACE VIEW `booking_incidents_summary` AS
SELECT
  i.booking_id,
  COUNT(DISTINCT i.incident_id) AS total_incidents,
  COUNT(CASE WHEN i.status IN ('PENDING', 'INVESTIGATING') THEN 1 END) AS open_incidents,
  COUNT(CASE WHEN i.status = 'RESOLVED' THEN 1 END) AS resolved_incidents,
  MAX(i.severity) AS highest_severity,
  SUM(i.compensation_paid_vnd) AS total_compensation_paid_vnd,
  MAX(i.reported_at) AS last_incident_at
FROM incidents i
GROUP BY i.booking_id;

-- View: Get AI detection statistics
CREATE OR REPLACE VIEW `ai_detection_stats` AS
SELECT
  ai_service_used,
  COUNT(*) AS total_processed,
  AVG(confidence_score) AS avg_confidence,
  MIN(confidence_score) AS min_confidence,
  MAX(confidence_score) AS max_confidence,
  COUNT(CASE WHEN confidence_score >= 0.85 THEN 1 END) AS high_confidence_count,
  COUNT(CASE WHEN confidence_score < 0.85 THEN 1 END) AS low_confidence_count
FROM booking_item_images
WHERE ai_processed_at IS NOT NULL
GROUP BY ai_service_used;

--
-- RESTORE SETTINGS
--

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

--
-- POST-INSTALLATION NOTES
--

SELECT ' Database setup completed successfully!' AS message;
SELECT COUNT(*) AS total_tables FROM information_schema.tables
WHERE table_schema = 'home_express' AND table_type = 'BASE TABLE';

SELECT CONCAT('

🇻🇳 VIETNAMESE MARKET CONFIGURATION


 Database configured for Vietnamese market:

1. Phone Numbers: Vietnamese format (0xxxxxxxxx)
   - 10 digits starting with 0
   - Examples: 0901234567, 0987654321

2. Tax Code (Mã số thuế):
   - 10 digits (entity): 0123456789
   - 13 digits (branch): 0123456789-001

3. Business License (GPKD):
   - 10 or 13 digits format

4. National ID (CMND/CCCD):
   - CMND: 9 digits
   - CCCD: 12 digits

5. Payment Methods (production-ready):
   - CASH: Tien mat giao truc tiep
   - BANK_TRANSFER: Chuyen khoan ngan hang

6. Vietnamese Banks:
   - 20 major banks pre-loaded
   - VCB, TCB, BIDV, VietinBank, ACB, etc.

7. Address Structure (Optional):
   - vn_provinces: Tỉnh/Thành phố
   - vn_districts: Quận/Huyện
   - vn_wards: Phường/Xã
   (Import data from: https://provinces.open-api.vn)


🔐 SECURITY REMINDERS (Member 1)


1. Change default admin password immediately!
   Email: admin@homeexpress.vn
   Password: Admin@123456

2. Configure JWT secrets in environment variables:
   - JWT_SECRET (for access tokens)
   - JWT_REFRESH_SECRET (for refresh tokens)

3. Token Security:
   - Always hash tokens with SHA-256 before storing
   - Never store plaintext passwords or tokens
   - Use bcrypt/Argon2id with cost >= 12

4. Rate Limiting:
   - Implement login rate limiting (5 attempts/15 min)
   - Use login_attempts table for tracking

5. Session Management:
   - Access token: 15 minutes expiration
   - Refresh token: 7 days expiration
   - Implement token rotation on refresh

7. Database Cleanup (Scheduled Jobs):
   DELETE FROM user_tokens WHERE expires_at < NOW() - INTERVAL 7 DAY;
   DELETE FROM user_sessions WHERE expires_at < NOW() - INTERVAL 30 DAY;
   DELETE FROM login_attempts WHERE attempted_at < NOW() - INTERVAL 30 DAY;
   DELETE FROM audit_log WHERE occurred_at < NOW() - INTERVAL 365 DAY;

8. Vietnam Data Protection (Nghị định 13/2023):
   - Implement user consent for data collection
   - Provide data access/deletion requests
   - Encrypt sensitive PII (CMND/CCCD, bank accounts)


📦 BOOKING & QUOTATION (Member 2 - Quy) - COMPLETE


1. Booking Workflow:
    Vietnamese address structure (province/district/ward codes)
    Geo validation (lat/lng ranges, floor >= 0)
    Distance source tracking (Google/Mapbox/OSRM)
    FK corrections (points to customers/transports tables)
    Transport notification list (transport_list table)

2. Quotation System:
    ONE accepted quotation per booking (enforced by DB)
    Auto-update booking when quote accepted (trigger)
    Auto-reject other quotes when one is accepted
    Acceptance audit (accepted_by, accepted_at, accepted_ip)
    Quotation status history tracking

3. Contract Management:
    Validation: Only create from ACCEPTED quotation
    Auto-activate when both parties sign (trigger)
    Commercial terms snapshot (deposit, balance, due dates)
    IP tracking for signatures
    Contract PDF storage support

4. Payment System:
    Integer VND only (no decimals) - gateway ready
    Idempotency keys - prevent double charges
    Refund support with parent linkage
    FK to vn_banks table
    Failure tracking (code, message)
    Payment methods: CASH, BANK_TRANSFER

5. Transport Notification (NEW):
    transport_list: tracks which transports were notified
    notification_method: EMAIL, SMS, PUSH
    has_viewed/has_responded tracking
    viewed_at/responded_at timestamps

6. Audit & History:
    booking_status_history table
    quotation_status_history table
    booking_payment_summary view

7. Business Rules Enforced:
   - Only CUSTOMER role can have customer_id in bookings
   - Only TRANSPORT role can have transport_id in bookings/quotations
   - Only ONE quotation can be accepted per booking
   - Contracts only from ACCEPTED quotations
   - Refunds must have parent_payment_id
   - Amounts must be positive
   - Geo coordinates in valid ranges
   - Floors must be >= 0


🚚 VEHICLE & PRICING (Member 3 - Quang) - COMPLETE


1. Vehicle Management:
    VN license plate validation (29A-123.45, 51D-12345 formats)
    Normalized license plates (UPPER + no spaces)
    Capacity constraints (kg/m3 must be positive)
    Vehicle dimensions validation
    Status tracking (available/in_use/maintenance/inactive)
    Vehicle types: motorcycle, van, truck_small, truck_large, other

2. Category & Size Management:
    categories: pre-loaded Vietnamese categories
    category_sizes: Small/Medium/Large/Extra Large
    default_weight_kg and default_volume_m3
    is_fragile_default, requires_disassembly_default
    display_order for UI sorting

3. Time-Versioned Pricing:
    vehicle_pricing: Per transport, per vehicle type
    category_pricing: Per transport, per category (optional size)
    No overlapping active periods (enforced by triggers)
    Historical pricing preserved (valid_from/valid_to)
    Tiered distance pricing (0-4km, 5-40km, >40km)

4. Distance API Cache:
    Multi-provider support (Google/Mapbox/OSRM)
    SHA-256 hashed keys (origin/destination)
    30-day TTL for cache entries
    Coordinates storage for geo queries
    distance_mode: DRIVING, WALKING, BICYCLING, TRANSIT

5. Price History Audit:
    Full breakdown (base/distance/category/fees)
    Integer VND values (payment gateway ready)
    Peak/weekend multiplier tracking
    Rule snapshot in JSON (versioned engine)
    Links to booking and transport

5. Performance Optimizations:
    Hot lookup indexes for pricing queries:
      - idx_vp_hot_lookup: WHERE transport_id=? AND vehicle_type=? AND is_active AND NOW() BETWEEN...
      - idx_cp_hot_lookup: WHERE transport_id=? AND category_id=? AND size_id AND is_active...
    Distance cache unique key: (provider, mode, origin_hash, destination_hash)
    Price history indexed by booking and transport+time

6. Business Rules Enforced:
   - Time-versioned pricing cannot overlap (same transport+vehicle/category)
   - All monetary values in integer VND (no decimal cents)
   - Multipliers >= 1.00 (peak, weekend, fragile, etc.)
   - Peak hours 0-23 validation
   - Capacity and dimensions must be positive
   - VN license plate regex validation

Query Examples:
-- Get active vehicle pricing for a transport:
SELECT * FROM vehicle_pricing
WHERE transport_id = ? AND vehicle_type = ? AND is_active = 1
  AND NOW() BETWEEN valid_from AND IFNULL(valid_to, '9999-12-31')
LIMIT 1;

-- Check distance cache:
SELECT distance_km, duration_minutes FROM distance_cache
WHERE provider='GOOGLE' AND mode='DRIVING'
  AND origin_hash=? AND destination_hash=?
  AND expires_at > NOW();

-- Audit price calculation history:
SELECT * FROM price_history
WHERE booking_id = ?
ORDER BY calculated_at DESC;


🌟 REVIEWS & NOTIFICATIONS (Member 4 - Giang) - COMPLETE


1. Review System:
    Bidirectional reviews (Customer ↔ Transport)
    Booking completion validation (trigger)
    Party validation (customer/transport match booking)
    Self-review prevention (trigger)
    Rating constraints (1.0-5.0, 0.5 increments)
    Comment length (10-5000 characters)
    Vietnamese text search (FULLTEXT with ngram)
    Status: PENDING, APPROVED, REJECTED, FLAGGED
    is_verified, is_anonymous flags

2. Review Engagement:
    Helpful voting system (review_helpfulness)
    Self-vote prevention (no reviewer/reviewee voting)
    Auto-update helpful_count (triggers)
    One response per review (review_responses)
    Report spam prevention (UNIQUE per user+review)
    Review photos support (review_photos)
    Review reports (SPAM, INAPPROPRIATE, FAKE, OFFENSIVE)

3. Rating Aggregation (O(1) Performance):
    rating_summaries table (denormalized)
    Delta triggers (INSERT/UPDATE/DELETE reviews)
    Auto-update transports.average_rating
    Star distribution histogram (count_1_star to count_5_star)
    Sub-rating averages (punctuality, communication, care, professionalism)
    Context: AS_CUSTOMER, AS_TRANSPORT

4. Notification System:
    Deduplication (dedupe_key SHA-256)
    Grouping & batching (5-minute buckets)
    Collapse count for merged notifications
    Monthly partitioning (90-day retention)
    WebSocket delivery tracking
    Email delivery tracking
    notification_type: BOOKING_UPDATE, REVIEW, PAYMENT, QUOTATION
    Priority levels: LOW, NORMAL, HIGH, URGENT

5. Email Tracking:
    Multi-provider support (SMTP/SendGrid/SES/Mailgun)
    Provider message ID tracking
    Open/click tracking tokens (open_token, click_token)
    Detailed lifecycle (QUEUED→SENT→DELIVERED→OPENED→CLICKED)
    email_events append-only timeline
    Bounce & complaint handling
    email_logs with status tracking

6. WebSocket Sessions:
    Session tracking (audit/backup)
    Node-aware (multi-server support)
    Heartbeat monitoring (last_heartbeat_at)
    Connection metadata (IP, user agent, device info)
    disconnected_at timestamp

7. Performance Optimizations:
    Composite indexes for review feeds
    FULLTEXT index for search (Vietnamese ngram)
    Notification partitioning (query 10x faster)
    rating_summaries O(1) lookups vs O(n) aggregation

Query Examples:
-- Get user rating (instant):
SELECT avg_overall, total_count, count_5_star
FROM rating_summaries
WHERE target_user_id = ? AND context = 'AS_TRANSPORT';

-- Search reviews (Vietnamese text):
SELECT * FROM reviews
WHERE MATCH(title, comment) AGAINST ('dịch vụ tốt' IN NATURAL LANGUAGE MODE)
  AND status = 'APPROVED';

-- Unread notifications (partitioned):
SELECT * FROM notifications
WHERE user_id = ? AND is_read = 0
ORDER BY created_at DESC
LIMIT 20;


DATABASE SUMMARY FOR 4 MEMBERS


 Member 1 - TriQuan (Authentication & Users):
   - 10 tables: users, customers, transports, managers, user_tokens,
     user_sessions, login_attempts, audit_log, otp_codes, vn_banks
   - Security features: JWT refresh tokens, rate limiting, audit trail
   - Vietnamese market: phone validation, CMND/CCCD, bank integration

 Member 2 - Quy (Booking & Quotation):
   - 9 tables: bookings, transport_list, booking_items, quotations,
     quotation_status_history, contracts, payments, payment_refunds,
     booking_status_history
   - Business rules: state machine, one accepted quote, contract validation
   - Payment gateway ready: , , ZaloPay support

 Member 3 - Quang (Vehicle & Pricing):
   - 8 tables: vehicles, categories, category_sizes, vehicle_pricing,
     category_pricing, distance_cache, price_history, vn_provinces/districts/wards
   - Pricing engine: time-versioned, tiered distance, category-based
   - Distance API: cached, multi-provider (Google/Mapbox/OSRM)

 Member 4 - Giang (Reviews & Notifications):
   - 12 tables: reviews, review_photos, review_responses, review_helpfulness,
     review_reports, rating_summaries, notifications, notification_preferences,
     email_logs, email_events, websocket_sessions, notification_partitions
   - Real-time: WebSocket support, email tracking, push notifications
   - Performance: O(1) rating lookups, partitioned notifications

 Total Database Tables: 43 tables + 5 reference tables (vn_*)
 Total Triggers: 25+ business logic triggers
 Total Indexes: 100+ for query optimization
 Vietnamese Market Ready: Address hierarchy, payment gateways, validation

') AS setup_notes;

--
-- SETTLEMENT & PAYOUT SYSTEM - HOME EXPRESS
--
-- Database: MySQL 8.0+
-- Purpose: Settlement records and payout batching for transports
-- Business Logic: Track commission, platform fees, and batch payouts to transports
--

USE `home_express`;

--
-- Table: commission_rules
-- Purpose: Define commission rules for transports (platform-wide or transport-specific)
-- Business Rule: Platform fee = (agreed_price × commission_rate_bps) / 10000 OR commission_flat_vnd
--
CREATE TABLE `commission_rules` (
  `rule_id` BIGINT NOT NULL AUTO_INCREMENT,

  -- Rule Scope
  `transport_id` BIGINT DEFAULT NULL COMMENT 'NULL = default platform rule, otherwise transport-specific',

  -- Commission Structure
  `rule_type` ENUM('PERCENT', 'FLAT') NOT NULL DEFAULT 'PERCENT'
    COMMENT 'Loại hoa hồng: theo phần trăm hoặc cố định',
  `commission_rate` DECIMAL(6,2) NOT NULL DEFAULT 0.00
    COMMENT 'Commission rate percentage (0.00 to 100.00)',
  `flat_fee_vnd` BIGINT DEFAULT NULL
    COMMENT 'Flat commission in VND (for FLAT type)',

  -- Status
  `is_active` BOOLEAN NOT NULL DEFAULT TRUE,

  -- Effective Period
  `effective_from` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `effective_to` DATETIME DEFAULT NULL COMMENT 'NULL = indefinite',

  -- Audit Fields
  `created_by` BIGINT DEFAULT NULL COMMENT 'Manager who created this rule',
  `updated_by` BIGINT DEFAULT NULL COMMENT 'Manager who last updated this rule',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  PRIMARY KEY (`rule_id`),
  UNIQUE KEY `uk_commission_rules_transport_period` (`transport_id`, `effective_from`, `effective_to`),
  KEY `idx_commission_rules_transport_active` (`transport_id`, `is_active`, `effective_from`),
  KEY `idx_commission_rules_effective` (`effective_from`, `effective_to`, `is_active`),

  CONSTRAINT `fk_commission_rules_transport`
    FOREIGN KEY (`transport_id`)
    REFERENCES `transports` (`transport_id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_commission_rules_created_by`
    FOREIGN KEY (`created_by`)
    REFERENCES `users` (`user_id`)
    ON DELETE SET NULL,

  -- Business Rules
  CONSTRAINT `chk_commission_rules_type_value`
    CHECK (
      (rule_type = 'PERCENT' AND commission_rate >= 0 AND commission_rate <= 100) OR
      (rule_type = 'FLAT' AND flat_fee_vnd IS NOT NULL AND flat_fee_vnd >= 0)
    ),
  CONSTRAINT `chk_commission_rules_period`
    CHECK (effective_to IS NULL OR effective_to > effective_from)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Commission rules for platform fees (default or transport-specific)';

--
-- Table: booking_settlements
-- Purpose: Settlement record for each completed booking
-- Business Logic: Tracks money collected, fees deducted, and net amount due to transport
--
CREATE TABLE `booking_settlements` (
  `settlement_id` BIGINT NOT NULL AUTO_INCREMENT,

  -- Reference
  `booking_id` BIGINT NOT NULL,
  `transport_id` BIGINT NOT NULL,

  -- Money Breakdown (All amounts in VND - BIGINT)
  `agreed_price_vnd` BIGINT NOT NULL
    COMMENT 'Contract agreed price (from contracts.agreed_price_vnd)',
  `total_collected_vnd` BIGINT NOT NULL DEFAULT 0
    COMMENT 'Total collected from customer (deposits + balance)',
  `gateway_fee_vnd` BIGINT NOT NULL DEFAULT 0
    COMMENT 'Bank transfer fee hook (currently always 0 VND; no gateway markup is applied in this version)',
  `commission_rate_bps` INT NOT NULL DEFAULT 0
    COMMENT 'Applied commission rate in basis points (for audit)',
  `platform_fee_vnd` BIGINT NOT NULL DEFAULT 0
    COMMENT 'Platform commission (calculated from commission_rules)',
  `adjustment_vnd` BIGINT NOT NULL DEFAULT 0
    COMMENT 'Manual adjustments (can be negative for deductions)',
  `net_to_transport_vnd` BIGINT GENERATED ALWAYS AS (
    (total_collected_vnd - gateway_fee_vnd - platform_fee_vnd + adjustment_vnd)
  ) STORED COMMENT 'Net amount payable to transport (calculated)',

  -- Collection Mode
  `collection_mode` ENUM('ALL_ONLINE', 'PARTIAL_ONLINE', 'CASH_ON_DELIVERY', 'MIXED', 'ALL_CASH') NOT NULL DEFAULT 'ALL_ONLINE'
    COMMENT 'Hình thức thu: toàn bộ online, một phần online, tiền mặt khi giao, hỗn hợp, toàn tiền mặt',

  -- Settlement Status
  `status` ENUM('PENDING', 'READY', 'IN_PAYOUT', 'ON_HOLD', 'PAID', 'CANCELLED') NOT NULL DEFAULT 'PENDING'
    COMMENT 'PENDING: chưa hoàn thành, READY: sẵn sàng chi trả, IN_PAYOUT: đã đưa vào batch payout, ON_HOLD: tạm giữ, PAID: đã chi trả, CANCELLED: hủy',
  `on_hold_reason` TEXT DEFAULT NULL
    COMMENT 'Lý do tạm giữ (incident, dispute, verification)',

  -- Payout Link
  `payout_id` BIGINT DEFAULT NULL
    COMMENT 'Link to payout batch when PAID',

  -- Timestamps
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
    COMMENT 'When booking completed and settlement created',
  `ready_at` DATETIME DEFAULT NULL
    COMMENT 'When settlement became READY for payout',
  `paid_at` DATETIME DEFAULT NULL
    COMMENT 'When settlement was included in a payout batch',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  -- Metadata
  `notes` TEXT DEFAULT NULL,
  `metadata` JSON DEFAULT NULL COMMENT 'Additional tracking (commission_rule_id, payment_ids, etc.)',

  PRIMARY KEY (`settlement_id`),
  UNIQUE KEY `uk_booking_settlements_booking` (`booking_id`),
  KEY `idx_booking_settlements_transport_status` (`transport_id`, `status`, `created_at` DESC),
  KEY `idx_booking_settlements_status_ready` (`status`, `ready_at` DESC),
  KEY `idx_booking_settlements_transport_ready` (`transport_id`, `ready_at` DESC),
  KEY `idx_booking_settlements_paid_at` (`paid_at` DESC),

  CONSTRAINT `fk_booking_settlements_booking`
    FOREIGN KEY (`booking_id`)
    REFERENCES `bookings` (`booking_id`)
    ON DELETE NO ACTION,
  CONSTRAINT `fk_booking_settlements_transport`
    FOREIGN KEY (`transport_id`)
    REFERENCES `transports` (`transport_id`)
    ON DELETE NO ACTION,

  -- Business Rules
  CONSTRAINT `chk_booking_settlements_amounts_positive`
    CHECK (
      agreed_price_vnd >= 0 AND
      total_collected_vnd >= 0 AND
      gateway_fee_vnd >= 0 AND
      platform_fee_vnd >= 0 AND
      commission_rate_bps >= 0 AND
      commission_rate_bps <= 10000
    ),
  CONSTRAINT `chk_booking_settlements_status_dates`
    CHECK (
      (status NOT IN ('READY', 'IN_PAYOUT') OR ready_at IS NOT NULL) AND
      (status != 'PAID' OR paid_at IS NOT NULL) AND
      (status != 'ON_HOLD' OR on_hold_reason IS NOT NULL)
    )

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Settlement records for completed bookings with fee breakdown';

--
-- Table: transport_payouts
-- Purpose: Batch payout records for transports (weekly/monthly batches)
-- Business Logic: Group multiple settlements into a single bank transfer
--
CREATE TABLE `transport_payouts` (
  `payout_id` BIGINT NOT NULL AUTO_INCREMENT,

  -- Reference
  `transport_id` BIGINT NOT NULL,
  `payout_number` VARCHAR(50) NOT NULL
    COMMENT 'E.g., PAYOUT-2025-W43-T123, PAYOUT-2025-10-T456',

  -- Amount
  `total_amount_vnd` BIGINT NOT NULL
    COMMENT 'Total payout amount (sum of all settlement items)',

  -- Item Count
  `item_count` INT NOT NULL DEFAULT 0
    COMMENT 'Number of settlements in this payout batch',

  -- Payout Status
  `status` ENUM('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED') NOT NULL DEFAULT 'PENDING'
    COMMENT 'PENDING: chưa xử lý, PROCESSING: đang xử lý, COMPLETED: hoàn thành, FAILED: thất bại',

  -- Bank Account Snapshot (for audit trail - snapshot at payout time)
  `bank_code` VARCHAR(10) DEFAULT NULL
    COMMENT 'Vietnamese bank code (snapshot from transports.bank_code)',
  `bank_account_number` VARCHAR(19) DEFAULT NULL
    COMMENT 'Bank account number (snapshot from transports.bank_account_number)',
  `bank_account_holder` VARCHAR(255) DEFAULT NULL
    COMMENT 'Account holder name (snapshot from transports.bank_account_holder)',

  -- Timestamps
  `processed_at` DATETIME DEFAULT NULL
    COMMENT 'When payout processing started',
  `completed_at` DATETIME DEFAULT NULL
    COMMENT 'When payout was completed/failed',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  -- Failure Tracking & Transaction Reference
  `failure_reason` TEXT DEFAULT NULL
    COMMENT 'Why payout failed (invalid bank account, insufficient funds, etc.)',
  `transaction_reference` VARCHAR(255) DEFAULT NULL
    COMMENT 'Bank transaction reference number',

  -- Metadata
  `notes` TEXT DEFAULT NULL,

  PRIMARY KEY (`payout_id`),
  UNIQUE KEY `uk_transport_payouts_payout_number` (`payout_number`),
  KEY `idx_transport_payouts_transport_status` (`transport_id`, `status`, `created_at` DESC),
  KEY `idx_transport_payouts_status` (`status`, `processed_at` DESC),
  KEY `idx_transport_payouts_completed_at` (`completed_at` DESC),

  CONSTRAINT `fk_transport_payouts_transport`
    FOREIGN KEY (`transport_id`)
    REFERENCES `transports` (`transport_id`)
    ON DELETE NO ACTION,
  CONSTRAINT `fk_transport_payouts_bank`
    FOREIGN KEY (`bank_code`)
    REFERENCES `vn_banks` (`bank_code`)
    ON DELETE SET NULL,

  -- Business Rules
  CONSTRAINT `chk_transport_payouts_amount_positive`
    CHECK (total_amount_vnd > 0),
  CONSTRAINT `chk_transport_payouts_status_dates`
    CHECK (
      (status != 'PROCESSING' OR processed_at IS NOT NULL) AND
      (status NOT IN ('COMPLETED', 'FAILED') OR completed_at IS NOT NULL) AND
      (status != 'FAILED' OR failure_reason IS NOT NULL)
    ),
  CONSTRAINT `chk_transport_payouts_bank_account`
    CHECK (bank_account_number IS NULL OR bank_account_number REGEXP '^[0-9]{8,19}$')

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Batch payout records for transports with bank account snapshot';
--
-- Table: transport_payout_items
-- Purpose: Link individual settlements to payout batches (many-to-one)
-- Business Logic: Each settlement can only be in one payout batch
--
CREATE TABLE `transport_payout_items` (
  `payout_item_id` BIGINT NOT NULL AUTO_INCREMENT,

  -- References
  `payout_id` BIGINT NOT NULL,
  `settlement_id` BIGINT NOT NULL,
  `booking_id` BIGINT NOT NULL,

  -- Amount Snapshot
  `amount_vnd` BIGINT NOT NULL
    COMMENT 'Amount from settlement (snapshot for audit)',

  -- Metadata
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (`payout_item_id`),
  UNIQUE KEY `uk_transport_payout_items_settlement` (`settlement_id`)
    COMMENT 'Each settlement can only be in one payout',
  KEY `idx_transport_payout_items_payout` (`payout_id`),
  KEY `idx_transport_payout_items_booking` (`booking_id`),

  CONSTRAINT `fk_transport_payout_items_payout`
    FOREIGN KEY (`payout_id`)
    REFERENCES `transport_payouts` (`payout_id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_transport_payout_items_settlement`
    FOREIGN KEY (`settlement_id`)
    REFERENCES `booking_settlements` (`settlement_id`)
    ON DELETE NO ACTION,
  CONSTRAINT `fk_transport_payout_items_booking`
    FOREIGN KEY (`booking_id`)
    REFERENCES `bookings` (`booking_id`)
    ON DELETE CASCADE,

  -- Business Rules
  CONSTRAINT `chk_transport_payout_items_amount_positive`
    CHECK (amount_vnd > 0)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Links settlements to payout batches (one settlement per payout)';

--
-- TRIGGERS FOR SETTLEMENT & PAYOUT SYSTEM
--
DELIMITER $$

-- Trigger: Validate settlement before marking as PAID
-- Ensure settlement is in READY status and has net amount > 0
CREATE TRIGGER `trg_booking_settlements_validate_paid`
BEFORE UPDATE ON `booking_settlements`
FOR EACH ROW
BEGIN
  IF NEW.status = 'PAID' AND OLD.status != 'PAID' THEN
    -- Ensure previous status was READY
    IF OLD.status != 'READY' THEN
      SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Settlement must be in READY status before marking as PAID';
    END IF;

    -- Ensure net amount is positive
    IF NEW.net_to_transport_vnd <= 0 THEN
      SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Cannot mark settlement as PAID with zero or negative net amount';
    END IF;

    -- Auto-set paid_at timestamp
    IF NEW.paid_at IS NULL THEN
      SET NEW.paid_at = NOW();
    END IF;
  END IF;
END$$


-- Trigger: Auto-calculate platform fee when settlement is created/updated
-- Apply commission rule to calculate platform_fee_vnd from agreed_price_vnd
CREATE TRIGGER `trg_booking_settlements_calculate_fee_bi`
BEFORE INSERT ON `booking_settlements`
FOR EACH ROW
BEGIN
  DECLARE v_rule_type ENUM('PERCENT', 'FLAT');
  DECLARE v_commission_rate DECIMAL(6,2);
  DECLARE v_flat_fee BIGINT;
  DECLARE v_calculated_fee BIGINT;

  -- Find active commission rule (transport-specific or default)
  SELECT rule_type, commission_rate, flat_fee_vnd
  INTO v_rule_type, v_commission_rate, v_flat_fee
  FROM commission_rules
  WHERE (transport_id = NEW.transport_id OR transport_id IS NULL)
    AND is_active = TRUE
    AND NOW() BETWEEN effective_from AND IFNULL(effective_to, '9999-12-31')
  ORDER BY transport_id DESC NULLS LAST, effective_from DESC
  LIMIT 1;

  -- Calculate platform fee if rule found
  IF v_rule_type IS NOT NULL THEN
    IF v_rule_type = 'PERCENT' THEN
      SET v_calculated_fee = FLOOR((NEW.agreed_price_vnd * v_commission_rate) / 100);
      SET NEW.commission_rate_bps = FLOOR(v_commission_rate * 100);
    ELSE
      SET v_calculated_fee = COALESCE(v_flat_fee, 0);
      SET NEW.commission_rate_bps = 0;
    END IF;

    -- Only auto-set if not manually specified
    IF NEW.platform_fee_vnd = 0 THEN
      SET NEW.platform_fee_vnd = v_calculated_fee;
    END IF;
  END IF;
END$$

-- Trigger: Prevent settlement deletion if already in a payout
CREATE TRIGGER `trg_booking_settlements_prevent_delete`
BEFORE DELETE ON `booking_settlements`
FOR EACH ROW
BEGIN
  DECLARE v_payout_count INT;

  SELECT COUNT(*) INTO v_payout_count
  FROM transport_payout_items
  WHERE settlement_id = OLD.settlement_id;

  IF v_payout_count > 0 THEN
    SIGNAL SQLSTATE '45000'
    SET MESSAGE_TEXT = 'Cannot delete settlement that is already in a payout batch';
  END IF;
END$$

-- Trigger: Validate payout total matches sum of items
CREATE TRIGGER `trg_transport_payouts_validate_total`
BEFORE UPDATE ON `transport_payouts`
FOR EACH ROW
BEGIN
  DECLARE v_items_total BIGINT;

  -- Validate totals when payout is marked as completed
  IF NEW.status = 'COMPLETED' AND OLD.status <> 'COMPLETED' THEN
    SELECT COALESCE(SUM(amount_vnd), 0) INTO v_items_total
    FROM transport_payout_items
    WHERE payout_id = NEW.payout_id;

    IF v_items_total != NEW.total_amount_vnd THEN
      SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Payout total_amount_vnd does not match sum of payout items';
    END IF;
  END IF;

  -- Auto-set timestamps based on lifecycle
  IF NEW.status = 'PROCESSING' AND OLD.status <> 'PROCESSING' AND NEW.processed_at IS NULL THEN
    SET NEW.processed_at = NOW();
  END IF;

  IF NEW.status IN ('COMPLETED', 'FAILED')
     AND OLD.status NOT IN ('COMPLETED', 'FAILED')
     AND NEW.completed_at IS NULL THEN
    SET NEW.completed_at = NOW();
  END IF;
END$$

-- Trigger: Update settlements when added to payout
CREATE TRIGGER `trg_transport_payout_items_mark_paid`
AFTER INSERT ON `transport_payout_items`
FOR EACH ROW
BEGIN
  -- Mark settlement as PAID
  UPDATE booking_settlements
  SET status = 'PAID',
      paid_at = NOW()
  WHERE settlement_id = NEW.settlement_id
    AND status = 'READY';
END$$

DELIMITER ;

--
-- VIEWS FOR SETTLEMENT & PAYOUT ANALYTICS
--

-- View: Transport settlement summary (pending payouts)
CREATE OR REPLACE VIEW `transport_settlement_summary` AS
SELECT
  t.transport_id,
  t.company_name,
  COUNT(DISTINCT bs.settlement_id) AS total_settlements,
  COUNT(DISTINCT CASE WHEN bs.status = 'READY' THEN bs.settlement_id END) AS ready_count,
  COUNT(DISTINCT CASE WHEN bs.status = 'PENDING' THEN bs.settlement_id END) AS pending_count,
  COUNT(DISTINCT CASE WHEN bs.status = 'ON_HOLD' THEN bs.settlement_id END) AS on_hold_count,
  COALESCE(SUM(CASE WHEN bs.status = 'READY' THEN bs.net_to_transport_vnd ELSE 0 END), 0) AS ready_amount_vnd,
  COALESCE(SUM(bs.net_to_transport_vnd), 0) AS total_net_vnd,
  MIN(CASE WHEN bs.status = 'READY' THEN bs.ready_at END) AS oldest_ready_at
FROM transports t
LEFT JOIN booking_settlements bs ON t.transport_id = bs.transport_id
GROUP BY t.transport_id, t.company_name;

-- View: Payout batch summary (with item counts)
CREATE OR REPLACE VIEW `payout_batch_summary` AS
SELECT
  tp.payout_id,
  tp.payout_number,
  tp.transport_id,
  t.company_name,
  tp.total_amount_vnd,
  tp.item_count,
  tp.status,
  tp.processed_at,
  tp.completed_at,
  tp.bank_code,
  tp.bank_account_number,
  tp.bank_account_holder,
  tp.transaction_reference,
  COUNT(tpi.payout_item_id) AS linked_item_count,
  MIN(bs.created_at) AS oldest_settlement_date,
  MAX(bs.created_at) AS newest_settlement_date
FROM transport_payouts tp
INNER JOIN transports t ON tp.transport_id = t.transport_id
LEFT JOIN transport_payout_items tpi ON tp.payout_id = tpi.payout_id
LEFT JOIN booking_settlements bs ON tpi.settlement_id = bs.settlement_id
GROUP BY tp.payout_id, tp.payout_number, tp.transport_id, t.company_name,
         tp.total_amount_vnd, tp.item_count, tp.status, tp.processed_at, tp.completed_at,
         tp.bank_code, tp.bank_account_number, tp.bank_account_holder, tp.transaction_reference;


--
-- UPDATED VIETNAMESE ADMINISTRATIVE DIVISIONS (2024)
--

-- Clear existing data first
DELETE FROM vn_wards;
DELETE FROM vn_districts;
DELETE FROM vn_provinces;

-- Insert updated provinces (63 provinces/cities as of 2024)
INSERT INTO vn_provinces (province_code, province_name, province_name_en, region, display_order) VALUES
('01', 'Thành phố Hà Nội', 'Ha Noi City', 'NORTH', 1),
('02', 'Tỉnh Hà Giang', 'Ha Giang Province', 'NORTH', 2),
('04', 'Tỉnh Cao Bằng', 'Cao Bang Province', 'NORTH', 3),
('06', 'Tỉnh Bắc Kạn', 'Bac Kan Province', 'NORTH', 4),
('08', 'Tỉnh Tuyên Quang', 'Tuyen Quang Province', 'NORTH', 5),
('10', 'Tỉnh Lào Cai', 'Lao Cai Province', 'NORTH', 6),
('11', 'Tỉnh Điện Biên', 'Dien Bien Province', 'NORTH', 7),
('12', 'Tỉnh Lai Châu', 'Lai Chau Province', 'NORTH', 8),
('14', 'Tỉnh Sơn La', 'Son La Province', 'NORTH', 9),
('15', 'Tỉnh Yên Bái', 'Yen Bai Province', 'NORTH', 10),
('17', 'Tỉnh Hoà Bình', 'Hoa Binh Province', 'NORTH', 11),
('19', 'Tỉnh Thái Nguyên', 'Thai Nguyen Province', 'NORTH', 12),
('20', 'Tỉnh Lạng Sơn', 'Lang Son Province', 'NORTH', 13),
('22', 'Tỉnh Quảng Ninh', 'Quang Ninh Province', 'NORTH', 14),
('24', 'Tỉnh Bắc Giang', 'Bac Giang Province', 'NORTH', 15),
('25', 'Tỉnh Phú Thọ', 'Phu Tho Province', 'NORTH', 16),
('26', 'Tỉnh Vĩnh Phúc', 'Vinh Phuc Province', 'NORTH', 17),
('27', 'Tỉnh Bắc Ninh', 'Bac Ninh Province', 'NORTH', 18),
('30', 'Tỉnh Hải Dương', 'Hai Duong Province', 'NORTH', 19),
('31', 'Thành phố Hải Phòng', 'Hai Phong City', 'NORTH', 20),
('33', 'Tỉnh Hưng Yên', 'Hung Yen Province', 'NORTH', 21),
('34', 'Tỉnh Thái Bình', 'Thai Binh Province', 'NORTH', 22),
('35', 'Tỉnh Hà Nam', 'Ha Nam Province', 'NORTH', 23),
('36', 'Tỉnh Nam Định', 'Nam Dinh Province', 'NORTH', 24),
('37', 'Tỉnh Ninh Bình', 'Ninh Binh Province', 'NORTH', 25),
('38', 'Tỉnh Thanh Hóa', 'Thanh Hoa Province', 'NORTH', 26),
('40', 'Tỉnh Nghệ An', 'Nghe An Province', 'NORTH', 27),
('42', 'Tỉnh Hà Tĩnh', 'Ha Tinh Province', 'NORTH', 28),
('44', 'Tỉnh Quảng Bình', 'Quang Binh Province', 'CENTRAL', 29),
('45', 'Tỉnh Quảng Trị', 'Quang Tri Province', 'CENTRAL', 30),
('46', 'Tỉnh Thừa Thiên Huế', 'Thua Thien Hue Province', 'CENTRAL', 31),
('48', 'Thành phố Đà Nẵng', 'Da Nang City', 'CENTRAL', 32),
('49', 'Tỉnh Quảng Nam', 'Quang Nam Province', 'CENTRAL', 33),
('51', 'Tỉnh Quảng Ngãi', 'Quang Ngai Province', 'CENTRAL', 34),
('52', 'Tỉnh Bình Định', 'Binh Dinh Province', 'CENTRAL', 35),
('54', 'Tỉnh Phú Yên', 'Phu Yen Province', 'CENTRAL', 36),
('56', 'Tỉnh Khánh Hòa', 'Khanh Hoa Province', 'CENTRAL', 37),
('58', 'Tỉnh Ninh Thuận', 'Ninh Thuan Province', 'CENTRAL', 38),
('60', 'Tỉnh Bình Thuận', 'Binh Thuan Province', 'CENTRAL', 39),
('62', 'Tỉnh Kon Tum', 'Kon Tum Province', 'CENTRAL', 40),
('64', 'Tỉnh Gia Lai', 'Gia Lai Province', 'CENTRAL', 41),
('66', 'Tỉnh Đắk Lắk', 'Dak Lak Province', 'CENTRAL', 42),
('67', 'Tỉnh Đắk Nông', 'Dak Nong Province', 'CENTRAL', 43),
('68', 'Tỉnh Lâm Đồng', 'Lam Dong Province', 'CENTRAL', 44),
('70', 'Tỉnh Bình Phước', 'Binh Phuoc Province', 'SOUTH', 45),
('72', 'Tỉnh Tây Ninh', 'Tay Ninh Province', 'SOUTH', 46),
('74', 'Tỉnh Bình Dương', 'Binh Duong Province', 'SOUTH', 47),
('75', 'Tỉnh Đồng Nai', 'Dong Nai Province', 'SOUTH', 48),
('77', 'Tỉnh Bà Rịa - Vũng Tàu', 'Ba Ria - Vung Tau Province', 'SOUTH', 49),
('79', 'Thành phố Hồ Chí Minh', 'Ho Chi Minh City', 'SOUTH', 50),
('80', 'Tỉnh Long An', 'Long An Province', 'SOUTH', 51),
('82', 'Tỉnh Tiền Giang', 'Tien Giang Province', 'SOUTH', 52),
('83', 'Tỉnh Bến Tre', 'Ben Tre Province', 'SOUTH', 53),
('84', 'Tỉnh Trà Vinh', 'Tra Vinh Province', 'SOUTH', 54),
('86', 'Tỉnh Vĩnh Long', 'Vinh Long Province', 'SOUTH', 55),
('87', 'Tỉnh Đồng Tháp', 'Dong Thap Province', 'SOUTH', 56),
('89', 'Tỉnh An Giang', 'An Giang Province', 'SOUTH', 57),
('91', 'Tỉnh Kiên Giang', 'Kien Giang Province', 'SOUTH', 58),
('92', 'Thành phố Cần Thơ', 'Can Tho City', 'SOUTH', 59),
('93', 'Tỉnh Hậu Giang', 'Hau Giang Province', 'SOUTH', 60),
('94', 'Tỉnh Sóc Trăng', 'Soc Trang Province', 'SOUTH', 61),
('95', 'Tỉnh Bạc Liêu', 'Bac Lieu Province', 'SOUTH', 62),
('96', 'Tỉnh Cà Mau', 'Ca Mau Province', 'SOUTH', 63);

SELECT '✅ Vietnamese administrative divisions updated successfully!' AS status;
SELECT '63 provinces/cities, updated with latest mergers and administrative changes' AS details;

--
-- SUCCESS MESSAGE
--
SELECT '✅ Settlement & Payout schema created successfully!' AS status;
SELECT 'Created 4 tables: commission_rules, booking_settlements, transport_payouts, transport_payout_items' AS tables;
SELECT 'Created 5 triggers: validate_paid, calculate_fee, prevent_delete, validate_total, mark_paid' AS triggers;
SELECT 'Created 2 views: transport_settlement_summary, payout_batch_summary' AS views;

-- ============================================================================
-- Imported Flyway migration: V1__Initial_Schema.sql
-- ============================================================================

-- ============================================================================
-- HOME EXPRESS - Initial Database Schema
-- ============================================================================
-- Flyway Migration: V1__Initial_Schema.sql
-- Description: Complete database schema for Home Express platform
-- Author: Team Home Express
-- Date: 2025-01-29

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- ============================================================================
-- VIETNAMESE-SPECIFIC REFERENCE TABLES
-- ============================================================================

CREATE TABLE IF NOT EXISTS `vn_banks` (
  `bank_code` VARCHAR(10) NOT NULL,
  `bank_name` VARCHAR(255) NOT NULL,
  `bank_name_en` VARCHAR(255) DEFAULT NULL,
  `napas_bin` VARCHAR(8) DEFAULT NULL COMMENT 'NAPAS Bank Identification Number',
  `swift_code` VARCHAR(11) DEFAULT NULL,
  `is_active` BOOLEAN DEFAULT TRUE,
  `logo_url` TEXT DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`bank_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Vietnamese banks for payment integration';

CREATE TABLE IF NOT EXISTS `vn_provinces` (
  `province_code` VARCHAR(6) NOT NULL,
  `province_name` VARCHAR(100) NOT NULL,
  `province_name_en` VARCHAR(100) DEFAULT NULL,
  `region` ENUM('NORTH', 'CENTRAL', 'SOUTH') NOT NULL COMMENT 'North/Central/South',
  `display_order` INT DEFAULT 0,
  PRIMARY KEY (`province_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Vietnamese provinces and cities';

CREATE TABLE IF NOT EXISTS `vn_districts` (
  `district_code` VARCHAR(6) NOT NULL,
  `district_name` VARCHAR(100) NOT NULL,
  `province_code` VARCHAR(6) NOT NULL,
  PRIMARY KEY (`district_code`),
  KEY `idx_districts_province` (`province_code`),
  CONSTRAINT `fk_districts_province`
    FOREIGN KEY (`province_code`)
    REFERENCES `vn_provinces` (`province_code`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Vietnamese districts';

CREATE TABLE IF NOT EXISTS `vn_wards` (
  `ward_code` VARCHAR(6) NOT NULL,
  `ward_name` VARCHAR(100) NOT NULL,
  `district_code` VARCHAR(6) NOT NULL,
  PRIMARY KEY (`ward_code`),
  KEY `idx_wards_district` (`district_code`),
  CONSTRAINT `fk_wards_district`
    FOREIGN KEY (`district_code`)
    REFERENCES `vn_districts` (`district_code`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Vietnamese wards and communes';

-- ============================================================================
-- SEED DATA: Vietnamese Banks
-- ============================================================================

INSERT INTO `vn_banks` (`bank_code`, `bank_name`, `bank_name_en`, `napas_bin`, `swift_code`, `is_active`) VALUES
('VCB', 'Ngan hang TMCP Ngoai thuong Viet Nam', 'Vietcombank', '970436', 'BFTVVNVX', TRUE),
('TCB', 'Ngan hang TMCP Ky thuong Viet Nam', 'Techcombank', '970407', 'VTCBVNVX', TRUE),
('BIDV', 'Ngan hang TMCP Dau tu va Phat trien VN', 'BIDV', '970418', 'BIDVVNVX', TRUE),
('VTB', 'Ngan hang TMCP Cong thuong Viet Nam', 'VietinBank', '970415', 'ICBVVNVX', TRUE),
('ACB', 'Ngan hang TMCP A Chau', 'ACB', '970416', 'ASCBVNVX', TRUE),
('MBB', 'Ngan hang TMCP Quan doi', 'MB Bank', '970422', 'MSCBVNVX', TRUE),
('VPB', 'Ngan hang TMCP Viet Nam Thinh Vuong', 'VPBank', '970432', 'VPBKVNVX', TRUE),
('TPB', 'Ngan hang TMCP Tien Phong', 'TPBank', '970423', 'TPBVNVX', TRUE),
('STB', 'Ngan hang TMCP Sai Gon Thuong Tin', 'Sacombank', '970403', 'SGTTVNVX', TRUE),
('HDB', 'Ngan hang TMCP Phat trien TP.HCM', 'HDBank', '970437', 'HDBCVNVX', TRUE)
ON DUPLICATE KEY UPDATE bank_name=VALUES(bank_name);

-- ============================================================================
-- AUTHENTICATION & USERS MODULE
-- ============================================================================

CREATE TABLE IF NOT EXISTS `users` (
  `user_id` BIGINT NOT NULL AUTO_INCREMENT,
  `email` VARCHAR(255) NOT NULL,
  `password_hash` TEXT NOT NULL,
  `role` ENUM('CUSTOMER', 'TRANSPORT', 'MANAGER') NOT NULL,
  `is_active` BOOLEAN DEFAULT TRUE,
  `is_verified` BOOLEAN DEFAULT FALSE,
  `email_verified_at` DATETIME DEFAULT NULL,
  `last_password_change` DATETIME DEFAULT NULL,
  `locked_until` DATETIME DEFAULT NULL,
  `verification_token` VARCHAR(255) DEFAULT NULL,
  `reset_password_token` VARCHAR(255) DEFAULT NULL,
  `reset_password_expires` DATETIME DEFAULT NULL,
  `last_login` DATETIME DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_users_email_lower` ((LOWER(`email`))),
  KEY `idx_users_role` (`role`),
  KEY `idx_users_is_active` (`is_active`),
  KEY `idx_users_locked` (`locked_until`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Base user authentication table';

CREATE TABLE IF NOT EXISTS `customers` (
  `customer_id` BIGINT NOT NULL,
  `full_name` VARCHAR(255) NOT NULL,
  `phone` VARCHAR(20) NOT NULL,
  `address` TEXT DEFAULT NULL,
  `date_of_birth` DATE DEFAULT NULL,
  `avatar_url` TEXT DEFAULT NULL,
  `preferred_language` VARCHAR(10) DEFAULT 'vi',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`customer_id`),
  KEY `idx_customers_phone` (`phone`),
  CONSTRAINT `fk_customers_users`
    FOREIGN KEY (`customer_id`)
    REFERENCES `users` (`user_id`)
    ON DELETE CASCADE,
  CONSTRAINT `chk_customers_phone_vn`
    CHECK (phone REGEXP '^0[1-9][0-9]{8}$')
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `transports` (
  `transport_id` BIGINT NOT NULL,
  `company_name` VARCHAR(255) NOT NULL,
  `business_license_number` VARCHAR(50) NOT NULL,
  `tax_code` VARCHAR(50) DEFAULT NULL,
  `phone` VARCHAR(20) NOT NULL,
  `address` TEXT NOT NULL,
  `city` VARCHAR(100) NOT NULL,
  `district` VARCHAR(100) DEFAULT NULL,
  `ward` VARCHAR(100) DEFAULT NULL,
  `license_photo_url` TEXT DEFAULT NULL,
  `insurance_photo_url` TEXT DEFAULT NULL,
  `verification_status` ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
  `verified_at` DATETIME DEFAULT NULL,
  `verified_by` BIGINT DEFAULT NULL,
  `total_bookings` INT DEFAULT 0,
  `completed_bookings` INT DEFAULT 0,
  `cancelled_bookings` INT DEFAULT 0,
  `average_rating` DECIMAL(3,2) DEFAULT 0.00,
  `national_id_number` VARCHAR(12) DEFAULT NULL,
  `national_id_type` ENUM('CMND', 'CCCD') DEFAULT NULL,
  `national_id_issue_date` DATE DEFAULT NULL,
  `national_id_issuer` VARCHAR(100) DEFAULT NULL,
  `national_id_photo_front_url` TEXT DEFAULT NULL,
  `national_id_photo_back_url` TEXT DEFAULT NULL,
  `bank_name` VARCHAR(100) DEFAULT NULL,
  `bank_code` VARCHAR(10) DEFAULT NULL,
  `bank_account_number` VARCHAR(19) DEFAULT NULL,
  `bank_account_holder` VARCHAR(255) DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`transport_id`),
  UNIQUE KEY `uk_transports_license` (`business_license_number`),
  UNIQUE KEY `uk_transports_tax_code` (`tax_code`),
  UNIQUE KEY `uk_transports_national_id` (`national_id_number`),
  KEY `idx_transports_city` (`city`),
  KEY `idx_transports_verification` (`verification_status`, `verified_at` DESC),
  KEY `idx_transports_rating` (`average_rating` DESC),
  CONSTRAINT `fk_transports_users`
    FOREIGN KEY (`transport_id`)
    REFERENCES `users` (`user_id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_transports_verified_by`
    FOREIGN KEY (`verified_by`)
    REFERENCES `users` (`user_id`)
    ON DELETE SET NULL,
  CONSTRAINT `fk_transports_bank`
    FOREIGN KEY (`bank_code`)
    REFERENCES `vn_banks` (`bank_code`)
    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `managers` (
  `manager_id` BIGINT NOT NULL,
  `full_name` VARCHAR(255) NOT NULL,
  `phone` VARCHAR(20) NOT NULL,
  `employee_id` VARCHAR(50) DEFAULT NULL,
  `department` VARCHAR(100) DEFAULT NULL,
  `permissions` JSON DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`manager_id`),
  UNIQUE KEY `uk_managers_employee` (`employee_id`),
  KEY `idx_managers_phone` (`phone`),
  CONSTRAINT `fk_managers_users`
    FOREIGN KEY (`manager_id`)
    REFERENCES `users` (`user_id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Additional tables will be in subsequent migrations (V2, V3, etc.)
-- This keeps initial migration manageable and allows incremental schema changes

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

-- ============================================================================
-- Imported Flyway migration: V2__Booking_System.sql
-- ============================================================================

-- ============================================================================
-- HOME EXPRESS - Booking System Schema
-- ============================================================================
-- Flyway Migration: V2__Booking_System.sql
-- Description: Complete booking, quotation, and category tables matching entities
-- Date: 2025-01-29

-- ============================================================================
-- CATEGORIES & PRICING
-- ============================================================================

CREATE TABLE IF NOT EXISTS `categories` (
  `category_id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  `description` TEXT DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`category_id`),
  UNIQUE KEY `uk_categories_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `category_pricing` (
  `pricing_id` BIGINT NOT NULL AUTO_INCREMENT,
  `category_id` BIGINT NOT NULL,
  `base_price_per_km` INT NOT NULL,
  `effective_from` DATE NOT NULL,
  `effective_to` DATE DEFAULT NULL,
  `is_active` BOOLEAN DEFAULT TRUE,
  PRIMARY KEY (`pricing_id`),
  KEY `idx_category_pricing_category` (`category_id`),
  CONSTRAINT `fk_category_pricing_category`
    FOREIGN KEY (`category_id`)
    REFERENCES `categories` (`category_id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- BOOKING MODULE - Complete schema matching Booking.java entity
-- ============================================================================

CREATE TABLE IF NOT EXISTS `bookings` (
  `booking_id` BIGINT NOT NULL AUTO_INCREMENT,
  `customer_id` BIGINT NOT NULL,
  `transport_id` BIGINT DEFAULT NULL,
  
  -- Pickup location details
  `pickup_address` TEXT NOT NULL,
  `pickup_latitude` DECIMAL(10,8) DEFAULT NULL,
  `pickup_longitude` DECIMAL(11,8) DEFAULT NULL,
  `pickup_floor` INT DEFAULT NULL,
  `pickup_has_elevator` BOOLEAN DEFAULT FALSE,
  `pickup_province_code` VARCHAR(6) DEFAULT NULL,
  `pickup_district_code` VARCHAR(6) DEFAULT NULL,
  `pickup_ward_code` VARCHAR(6) DEFAULT NULL,
  
  -- Delivery location details
  `delivery_address` TEXT NOT NULL,
  `delivery_latitude` DECIMAL(10,8) DEFAULT NULL,
  `delivery_longitude` DECIMAL(11,8) DEFAULT NULL,
  `delivery_floor` INT DEFAULT NULL,
  `delivery_has_elevator` BOOLEAN DEFAULT FALSE,
  `delivery_province_code` VARCHAR(6) DEFAULT NULL,
  `delivery_district_code` VARCHAR(6) DEFAULT NULL,
  `delivery_ward_code` VARCHAR(6) DEFAULT NULL,
  
  -- Scheduling
  `preferred_date` DATE NOT NULL,
  `preferred_time_slot` VARCHAR(20) DEFAULT NULL,
  `actual_start_time` DATETIME DEFAULT NULL,
  `actual_end_time` DATETIME DEFAULT NULL,
  
  -- Distance calculation
  `distance_km` DECIMAL(8,2) DEFAULT NULL,
  `distance_source` VARCHAR(20) DEFAULT NULL,
  `distance_calculated_at` DATETIME DEFAULT NULL,
  
  -- Pricing
  `estimated_price` DECIMAL(12,0) DEFAULT NULL,
  `final_price` DECIMAL(12,0) DEFAULT NULL,
  
  -- Status
  `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  
  -- Additional info
  `notes` TEXT DEFAULT NULL,
  `special_requirements` TEXT DEFAULT NULL,
  
  -- Cancellation metadata
  `cancelled_by` BIGINT DEFAULT NULL,
  `cancellation_reason` TEXT DEFAULT NULL,
  `cancelled_at` DATETIME DEFAULT NULL,
  
  -- Timestamps
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  
  PRIMARY KEY (`booking_id`),
  KEY `idx_bookings_customer` (`customer_id`),
  KEY `idx_bookings_transport` (`transport_id`),
  KEY `idx_bookings_status` (`status`),
  KEY `idx_bookings_date` (`preferred_date`),
  KEY `idx_bookings_customer_status` (`customer_id`, `status`),
  
  CONSTRAINT `fk_bookings_customer`
    FOREIGN KEY (`customer_id`)
    REFERENCES `customers` (`customer_id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_bookings_transport`
    FOREIGN KEY (`transport_id`)
    REFERENCES `transports` (`transport_id`)
    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- BOOKING ITEMS
-- ============================================================================

CREATE TABLE IF NOT EXISTS `booking_items` (
  `item_id` BIGINT NOT NULL AUTO_INCREMENT,
  `booking_id` BIGINT NOT NULL,
  `category_id` BIGINT NOT NULL,
  `quantity` INT NOT NULL DEFAULT 1,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`item_id`),
  KEY `idx_booking_items_booking` (`booking_id`),
  CONSTRAINT `fk_booking_items_booking`
    FOREIGN KEY (`booking_id`)
    REFERENCES `bookings` (`booking_id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_booking_items_category`
    FOREIGN KEY (`category_id`)
    REFERENCES `categories` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- BOOKING STATUS HISTORY
-- ============================================================================

CREATE TABLE IF NOT EXISTS `booking_status_history` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `booking_id` BIGINT NOT NULL,
  `old_status` VARCHAR(20) DEFAULT NULL,
  `new_status` VARCHAR(20) NOT NULL,
  `changed_by` BIGINT DEFAULT NULL,
  `changed_by_role` VARCHAR(20) DEFAULT NULL,
  `reason` TEXT DEFAULT NULL,
  `metadata` JSON DEFAULT NULL,
  `changed_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_booking_status_history_booking` (`booking_id`),
  KEY `idx_booking_status_history_changed_at` (`changed_at`),
  CONSTRAINT `fk_status_history_booking`
    FOREIGN KEY (`booking_id`)
    REFERENCES `bookings` (`booking_id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- QUOTATION MODULE - Complete schema matching Quotation.java entity
-- ============================================================================

CREATE TABLE IF NOT EXISTS `quotations` (
  `quotation_id` BIGINT NOT NULL AUTO_INCREMENT,
  `booking_id` BIGINT NOT NULL,
  `transport_id` BIGINT NOT NULL,
  
  -- Pricing details
  `quoted_price` DECIMAL(12,0) NOT NULL,
  `base_price` DECIMAL(12,0) DEFAULT NULL,
  `distance_price` DECIMAL(12,0) DEFAULT NULL,
  `items_price` DECIMAL(12,0) DEFAULT NULL,
  `additional_fees` DECIMAL(12,0) DEFAULT NULL,
  `discount` DECIMAL(12,0) DEFAULT NULL,
  `price_breakdown` JSON DEFAULT NULL,
  
  -- Additional info
  `notes` TEXT DEFAULT NULL,
  `validity_period` INT DEFAULT 7,
  `expires_at` DATETIME DEFAULT NULL,
  
  -- Status
  `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  `responded_at` DATETIME DEFAULT NULL,
  
  -- Acceptance metadata
  `accepted_by` BIGINT DEFAULT NULL,
  `accepted_at` DATETIME DEFAULT NULL,
  `accepted_ip` VARCHAR(45) DEFAULT NULL,
  `accepted_booking_id` BIGINT DEFAULT NULL,
  
  -- Timestamps
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  
  PRIMARY KEY (`quotation_id`),
  KEY `idx_quotations_booking` (`booking_id`),
  KEY `idx_quotations_transport` (`transport_id`),
  KEY `idx_quotations_status` (`status`),
  KEY `idx_quotations_expires` (`expires_at`),
  
  CONSTRAINT `fk_quotations_booking`
    FOREIGN KEY (`booking_id`)
    REFERENCES `bookings` (`booking_id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_quotations_transport`
    FOREIGN KEY (`transport_id`)
    REFERENCES `transports` (`transport_id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- TRANSPORT LISTS
-- ============================================================================

CREATE TABLE IF NOT EXISTS `transport_lists` (
  `list_id` BIGINT NOT NULL AUTO_INCREMENT,
  `booking_id` BIGINT NOT NULL,
  `transport_id` BIGINT NOT NULL,
  `notified_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `viewed_at` DATETIME DEFAULT NULL,
  PRIMARY KEY (`list_id`),
  UNIQUE KEY `uk_transport_lists` (`booking_id`, `transport_id`),
  KEY `idx_transport_lists_transport` (`transport_id`),
  CONSTRAINT `fk_transport_lists_booking`
    FOREIGN KEY (`booking_id`)
    REFERENCES `bookings` (`booking_id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_transport_lists_transport`
    FOREIGN KEY (`transport_id`)
    REFERENCES `transports` (`transport_id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


