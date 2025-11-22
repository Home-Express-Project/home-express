-- ============================================================================
-- HOME EXPRESS - INITIAL DATABASE SCHEMA (V1)
-- ============================================================================
-- Description: Cleaned & Consolidated Schema
-- Includes: Auth, Users, Locations, Transport, Booking, Pricing, Reviews
-- ============================================================================

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- ============================================================================
-- 1. VIETNAMESE REFERENCE DATA (Locations & Banks)
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `vn_provinces` (
  `province_code` VARCHAR(6) NOT NULL,
  `province_name` VARCHAR(100) NOT NULL,
  `province_name_en` VARCHAR(100) DEFAULT NULL,
  `region` ENUM('NORTH', 'CENTRAL', 'SOUTH') NOT NULL,
  `display_order` INT DEFAULT 0,
  PRIMARY KEY (`province_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `vn_districts` (
  `district_code` VARCHAR(6) NOT NULL,
  `district_name` VARCHAR(100) NOT NULL,
  `province_code` VARCHAR(6) NOT NULL,
  PRIMARY KEY (`district_code`),
  KEY `idx_districts_province` (`province_code`),
  CONSTRAINT `fk_districts_province` FOREIGN KEY (`province_code`) REFERENCES `vn_provinces` (`province_code`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `vn_wards` (
  `ward_code` VARCHAR(6) NOT NULL,
  `ward_name` VARCHAR(100) NOT NULL,
  `district_code` VARCHAR(6) NOT NULL,
  PRIMARY KEY (`ward_code`),
  KEY `idx_wards_district` (`district_code`),
  CONSTRAINT `fk_wards_district` FOREIGN KEY (`district_code`) REFERENCES `vn_districts` (`district_code`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Seed Data: Top Banks
INSERT IGNORE INTO `vn_banks` (`bank_code`, `bank_name`, `napas_bin`, `is_active`) VALUES
('VCB', 'Vietcombank', '970436', TRUE),
('TCB', 'Techcombank', '970407', TRUE),
('BIDV', 'BIDV', '970418', TRUE),
('MBB', 'MB Bank', '970422', TRUE),
('ACB', 'ACB', '970416', TRUE),
('VPB', 'VPBank', '970432', TRUE),
('TPB', 'TPBank', '970423', TRUE);

-- ============================================================================
-- 2. AUTHENTICATION & USERS
-- ============================================================================

CREATE TABLE IF NOT EXISTS `users` (
  `user_id` BIGINT NOT NULL AUTO_INCREMENT,
  `email` VARCHAR(255) NOT NULL,
  `phone` VARCHAR(20) DEFAULT NULL,
  `password_hash` TEXT NOT NULL,
  `role` ENUM('CUSTOMER', 'TRANSPORT', 'MANAGER') NOT NULL,
  `is_active` BOOLEAN DEFAULT TRUE,
  `is_verified` BOOLEAN DEFAULT FALSE,
  `email_verified_at` DATETIME DEFAULT NULL,
  `last_password_change` DATETIME DEFAULT NULL,
  `locked_until` DATETIME DEFAULT NULL,
  `last_login` DATETIME DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_users_email_lower` ((LOWER(`email`))),
  UNIQUE KEY `uk_users_phone` (`phone`),
  KEY `idx_users_role` (`role`),
  KEY `idx_users_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
  CONSTRAINT `fk_customers_users` FOREIGN KEY (`customer_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE,
  CONSTRAINT `chk_customers_phone` CHECK (phone REGEXP '^0[1-9][0-9]{8}$')
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
  CONSTRAINT `fk_managers_users` FOREIGN KEY (`manager_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Secure Token Management (Replaces old verify/reset columns)
CREATE TABLE IF NOT EXISTS `user_tokens` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `token_type` ENUM('VERIFY_EMAIL', 'RESET_PASSWORD', 'INVITE', 'MFA_RECOVERY') NOT NULL,
  `token_hash` VARCHAR(64) NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `expires_at` DATETIME NOT NULL,
  `consumed_at` DATETIME DEFAULT NULL,
  `metadata` JSON DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_tokens` (`user_id`, `token_type`, `token_hash`),
  KEY `idx_user_tokens_lookup` (`user_id`, `token_type`, `expires_at`),
  CONSTRAINT `fk_user_tokens_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- JWT Refresh Tokens
CREATE TABLE IF NOT EXISTS `user_sessions` (
  `session_id` CHAR(36) NOT NULL DEFAULT (UUID()),
  `user_id` BIGINT NOT NULL,
  `refresh_token_hash` VARCHAR(64) NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `expires_at` DATETIME NOT NULL,
  `last_seen_at` DATETIME DEFAULT NULL,
  `revoked_at` DATETIME DEFAULT NULL,
  `revoked_reason` TEXT DEFAULT NULL,
  `ip_address` VARCHAR(45) DEFAULT NULL,
  `user_agent` TEXT DEFAULT NULL,
  `device_id` VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (`session_id`),
  KEY `idx_user_sessions_refresh` (`refresh_token_hash`),
  CONSTRAINT `fk_user_sessions_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Login Audit
CREATE TABLE IF NOT EXISTS `login_attempts` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT DEFAULT NULL,
  `email` VARCHAR(255) NOT NULL,
  `ip_address` VARCHAR(45) DEFAULT NULL,
  `success` BOOLEAN NOT NULL,
  `failure_reason` TEXT DEFAULT NULL,
  `attempted_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_login_attempts_email` (`email`, `attempted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 3. TRANSPORT & VEHICLES
-- ============================================================================

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
  
  -- Verification
  `verification_status` ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
  `verified_at` DATETIME DEFAULT NULL,
  `verified_by` BIGINT DEFAULT NULL,
  
  -- Stats
  `total_bookings` INT DEFAULT 0,
  `completed_bookings` INT DEFAULT 0,
  `cancelled_bookings` INT DEFAULT 0,
  `average_rating` DECIMAL(3,2) DEFAULT 0.00,
  
  -- KYC (National ID)
  `national_id_number` VARCHAR(12) DEFAULT NULL,
  `national_id_type` ENUM('CMND', 'CCCD') DEFAULT NULL,
  `national_id_issue_date` DATE DEFAULT NULL,
  `national_id_issuer` VARCHAR(100) DEFAULT NULL,
  `national_id_photo_front_url` TEXT DEFAULT NULL,
  `national_id_photo_back_url` TEXT DEFAULT NULL,
  
  -- Banking
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
  KEY `idx_transports_rating` (`average_rating` DESC),
  
  CONSTRAINT `fk_transports_users` FOREIGN KEY (`transport_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_transports_verifier` FOREIGN KEY (`verified_by`) REFERENCES `users` (`user_id`) ON DELETE SET NULL,
  CONSTRAINT `fk_transports_bank` FOREIGN KEY (`bank_code`) REFERENCES `vn_banks` (`bank_code`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `transport_settings` (
  `transport_id` BIGINT NOT NULL,
  `search_radius_km` DECIMAL(5,2) NOT NULL DEFAULT 10.00,
  `min_job_value_vnd` DECIMAL(12,0) NOT NULL DEFAULT 0,
  `auto_accept_jobs` TINYINT(1) NOT NULL DEFAULT 0,
  `response_time_hours` DECIMAL(4,1) DEFAULT 2.0,
  `new_job_alerts` TINYINT(1) NOT NULL DEFAULT 1,
  `quotation_updates` TINYINT(1) NOT NULL DEFAULT 1,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`transport_id`),
  CONSTRAINT `fk_settings_transport` FOREIGN KEY (`transport_id`) REFERENCES `transports` (`transport_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
  KEY `idx_category_pricing` (`category_id`),
  CONSTRAINT `fk_category_pricing` FOREIGN KEY (`category_id`) REFERENCES `categories` (`category_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 4. BOOKING & QUOTATION
-- ============================================================================

CREATE TABLE IF NOT EXISTS `bookings` (
  `booking_id` BIGINT NOT NULL AUTO_INCREMENT,
  `customer_id` BIGINT NOT NULL,
  `transport_id` BIGINT DEFAULT NULL,
  
  -- Pickup
  `pickup_address` TEXT NOT NULL,
  `pickup_latitude` DECIMAL(10,8) DEFAULT NULL,
  `pickup_longitude` DECIMAL(11,8) DEFAULT NULL,
  `pickup_floor` INT DEFAULT NULL,
  `pickup_has_elevator` BOOLEAN DEFAULT FALSE,
  `pickup_province_code` VARCHAR(6) DEFAULT NULL,
  `pickup_district_code` VARCHAR(6) DEFAULT NULL,
  `pickup_ward_code` VARCHAR(6) DEFAULT NULL,
  
  -- Delivery
  `delivery_address` TEXT NOT NULL,
  `delivery_latitude` DECIMAL(10,8) DEFAULT NULL,
  `delivery_longitude` DECIMAL(11,8) DEFAULT NULL,
  `delivery_floor` INT DEFAULT NULL,
  `delivery_has_elevator` BOOLEAN DEFAULT FALSE,
  `delivery_province_code` VARCHAR(6) DEFAULT NULL,
  `delivery_district_code` VARCHAR(6) DEFAULT NULL,
  `delivery_ward_code` VARCHAR(6) DEFAULT NULL,
  
  -- Details
  `preferred_date` DATE NOT NULL,
  `preferred_time_slot` VARCHAR(20) DEFAULT NULL,
  `actual_start_time` DATETIME DEFAULT NULL,
  `actual_end_time` DATETIME DEFAULT NULL,
  `distance_km` DECIMAL(8,2) DEFAULT NULL,
  `distance_source` VARCHAR(20) DEFAULT NULL,
  `distance_calculated_at` DATETIME DEFAULT NULL,
  
  -- Money
  `estimated_price` DECIMAL(12,0) DEFAULT NULL,
  `final_price` DECIMAL(12,0) DEFAULT NULL,
  
  -- Lifecycle
  `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  `notes` TEXT DEFAULT NULL,
  `special_requirements` TEXT DEFAULT NULL,
  `cancelled_by` BIGINT DEFAULT NULL,
  `cancellation_reason` TEXT DEFAULT NULL,
  `cancelled_at` DATETIME DEFAULT NULL,
  
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  
  PRIMARY KEY (`booking_id`),
  KEY `idx_bookings_customer` (`customer_id`),
  KEY `idx_bookings_transport` (`transport_id`),
  KEY `idx_bookings_status` (`status`),
  
  CONSTRAINT `fk_bookings_customer` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`customer_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_bookings_transport` FOREIGN KEY (`transport_id`) REFERENCES `transports` (`transport_id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `booking_items` (
  `item_id` BIGINT NOT NULL AUTO_INCREMENT,
  `booking_id` BIGINT NOT NULL,
  `category_id` BIGINT NOT NULL,
  `quantity` INT NOT NULL DEFAULT 1,
  PRIMARY KEY (`item_id`),
  KEY `idx_booking_items` (`booking_id`),
  CONSTRAINT `fk_booking_items_booking` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`booking_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_booking_items_category` FOREIGN KEY (`category_id`) REFERENCES `categories` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `quotations` (
  `quotation_id` BIGINT NOT NULL AUTO_INCREMENT,
  `booking_id` BIGINT NOT NULL,
  `transport_id` BIGINT NOT NULL,
  
  `quoted_price` DECIMAL(12,0) NOT NULL,
  `base_price` DECIMAL(12,0) DEFAULT NULL,
  `distance_price` DECIMAL(12,0) DEFAULT NULL,
  `items_price` DECIMAL(12,0) DEFAULT NULL,
  `additional_fees` DECIMAL(12,0) DEFAULT NULL,
  `notes` TEXT DEFAULT NULL,
  
  `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  `validity_period` INT DEFAULT 7,
  `expires_at` DATETIME DEFAULT NULL,
  `accepted_at` DATETIME DEFAULT NULL,
  
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  
  PRIMARY KEY (`quotation_id`),
  KEY `idx_quotations_booking` (`booking_id`),
  KEY `idx_quotations_transport` (`transport_id`),
  CONSTRAINT `fk_quotations_booking` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`booking_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_quotations_transport` FOREIGN KEY (`transport_id`) REFERENCES `transports` (`transport_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `transport_lists` (
  `list_id` BIGINT NOT NULL AUTO_INCREMENT,
  `booking_id` BIGINT NOT NULL,
  `transport_id` BIGINT NOT NULL,
  `notified_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `viewed_at` DATETIME DEFAULT NULL,
  PRIMARY KEY (`list_id`),
  UNIQUE KEY `uk_transport_lists` (`booking_id`, `transport_id`),
  CONSTRAINT `fk_transport_lists_booking` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`booking_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_transport_lists_transport` FOREIGN KEY (`transport_id`) REFERENCES `transports` (`transport_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 5. SETTLEMENT & PAYMENTS
-- ============================================================================

CREATE TABLE IF NOT EXISTS `commission_rules` (
  `rule_id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  `percentage` DECIMAL(5,2) NOT NULL,
  `min_fee` DECIMAL(12,0) DEFAULT 0,
  `max_fee` DECIMAL(12,0) DEFAULT NULL,
  `apply_to_transport_type` VARCHAR(50) DEFAULT NULL,
  `is_active` BOOLEAN DEFAULT TRUE,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`rule_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `booking_settlements` (
  `settlement_id` BIGINT NOT NULL AUTO_INCREMENT,
  `booking_id` BIGINT NOT NULL,
  `transport_id` BIGINT NOT NULL,
  
  `total_booking_value` DECIMAL(12,0) NOT NULL,
  `commission_rate` DECIMAL(5,2) NOT NULL,
  `commission_amount` DECIMAL(12,0) NOT NULL,
  `transport_earnings` DECIMAL(12,0) NOT NULL,
  
  `status` ENUM('PENDING', 'PROCESSED', 'PAID', 'DISPUTED') DEFAULT 'PENDING',
  `processed_at` DATETIME DEFAULT NULL,
  `payout_id` BIGINT DEFAULT NULL,
  
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`settlement_id`),
  UNIQUE KEY `uk_settlements_booking` (`booking_id`),
  KEY `idx_settlements_transport` (`transport_id`),
  CONSTRAINT `fk_settlements_booking` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`booking_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `transport_payouts` (
  `payout_id` BIGINT NOT NULL AUTO_INCREMENT,
  `transport_id` BIGINT NOT NULL,
  `amount` DECIMAL(12,0) NOT NULL,
  `period_start` DATE NOT NULL,
  `period_end` DATE NOT NULL,
  `status` ENUM('DRAFT', 'PROCESSING', 'COMPLETED', 'FAILED') DEFAULT 'DRAFT',
  `bank_reference` VARCHAR(100) DEFAULT NULL,
  `paid_at` DATETIME DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`payout_id`),
  KEY `idx_payouts_transport` (`transport_id`),
  CONSTRAINT `fk_payouts_transport` FOREIGN KEY (`transport_id`) REFERENCES `transports` (`transport_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 6. REVIEWS & INCIDENTS
-- ============================================================================

CREATE TABLE IF NOT EXISTS `reviews` (
  `review_id` BIGINT NOT NULL AUTO_INCREMENT,
  `booking_id` BIGINT NOT NULL,
  `customer_id` BIGINT NOT NULL,
  `transport_id` BIGINT NOT NULL,
  `rating` INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
  `comment` TEXT DEFAULT NULL,
  `is_public` BOOLEAN DEFAULT TRUE,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`review_id`),
  UNIQUE KEY `uk_reviews_booking` (`booking_id`),
  KEY `idx_reviews_transport` (`transport_id`),
  CONSTRAINT `fk_reviews_booking` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`booking_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_reviews_customer` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`customer_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_reviews_transport` FOREIGN KEY (`transport_id`) REFERENCES `transports` (`transport_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `notifications` (
  `notification_id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `type` VARCHAR(50) NOT NULL,
  `title` VARCHAR(255) NOT NULL,
  `message` TEXT NOT NULL,
  `reference_type` VARCHAR(50) DEFAULT NULL,
  `reference_id` BIGINT DEFAULT NULL,
  `is_read` BOOLEAN NOT NULL DEFAULT FALSE,
  `read_at` DATETIME DEFAULT NULL,
  `priority` VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`notification_id`),
  KEY `idx_notifications_user` (`user_id`),
  CONSTRAINT `fk_notifications_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `incidents` (
  `incident_id` BIGINT NOT NULL AUTO_INCREMENT,
  `booking_id` BIGINT NOT NULL,
  `reported_by` BIGINT NOT NULL,
  `type` VARCHAR(50) NOT NULL,
  `description` TEXT NOT NULL,
  `status` ENUM('OPEN', 'INVESTIGATING', 'RESOLVED', 'CLOSED') DEFAULT 'OPEN',
  `resolution_notes` TEXT DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`incident_id`),
  KEY `idx_incidents_booking` (`booking_id`),
  CONSTRAINT `fk_incidents_booking` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`booking_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_incidents_reporter` FOREIGN KEY (`reported_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 7. SEED DATA (ADMIN USER)
-- ============================================================================

-- Default Admin: admin@homeexpress.com / Admin123@
INSERT IGNORE INTO `users` (`email`, `password_hash`, `role`, `is_active`, `is_verified`, `created_at`)
VALUES ('admin@homeexpress.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'MANAGER', TRUE, TRUE, NOW());

-- Get the ID of the admin user we just inserted (in case auto_increment is not 1)
SET @admin_id = (SELECT `user_id` FROM `users` WHERE `email` = 'admin@homeexpress.com');

-- Create Manager profile
INSERT IGNORE INTO `managers` (`manager_id`, `full_name`, `phone`, `department`, `employee_id`, `permissions`)
VALUES (@admin_id, 'System Administrator', '0900000000', 'IT', 'ADMIN001', '["ALL"]');

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
