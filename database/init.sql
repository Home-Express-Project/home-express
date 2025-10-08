-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema home_express
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS home_express DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci ;
USE home_express ;

-- -----------------------------------------------------
-- Table home_express.users
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS home_express.users (
                                                  user_id INT NOT NULL AUTO_INCREMENT,
                                                  username VARCHAR(45) NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(45) NOT NULL,
    role ENUM('CUSTOMER', 'TRANSPORT', 'MANAGER') NOT NULL,
    avatar VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    is_verified BOOLEAN DEFAULT FALSE,
    last_active DATETIME,
    refresh_token VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id),
    UNIQUE KEY unique_email (email),
    UNIQUE KEY unique_phone (phone)
    ) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table home_express.customers
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS home_express.customers (
                                                      customer_id INT NOT NULL,
                                                      land_certificate VARCHAR(45) NOT NULL,
    address VARCHAR(100) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (customer_id),
    CONSTRAINT customer
    FOREIGN KEY (customer_id)
    REFERENCES home_express.users (user_id)
                                                  ON DELETE CASCADE
                                                  ON UPDATE NO ACTION
    ) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table home_express.transports
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS home_express.transports (
                                                       transport_id INT NOT NULL,
                                                       license VARCHAR(45) NOT NULL,
    bank_qr VARCHAR(255) NOT NULL,
    income DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    rating DECIMAL(3,2) NOT NULL DEFAULT 0.00,
    completed_jobs INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (transport_id),
    CONSTRAINT transport
    FOREIGN KEY (transport_id)
    REFERENCES home_express.users (user_id)
                                                  ON DELETE CASCADE
                                                  ON UPDATE NO ACTION
    ) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table home_express.managers
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS home_express.managers (
                                                     manager_id INT NOT NULL,
                                                     created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                                     updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                                     PRIMARY KEY (manager_id),
    CONSTRAINT manager
    FOREIGN KEY (manager_id)
    REFERENCES home_express.users (user_id)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
    ) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table home_express.categories
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS home_express.categories (
                                                       category_id INT NOT NULL AUTO_INCREMENT,
                                                       name VARCHAR(45) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (category_id)
    ) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table home_express.size
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS home_express.size (
                                                 category_id INT NOT NULL,
                                                 weight DECIMAL(10,2) NOT NULL,
    height DECIMAL(10,2) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX category_sizes_idx (category_id ASC) VISIBLE,
    CONSTRAINT category_sizes
    FOREIGN KEY (category_id)
    REFERENCES home_express.categories (category_id)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
    ) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table home_express.bookings
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS home_express.bookings (
                                                     booking_id INT NOT NULL AUTO_INCREMENT,
                                                     customer_id INT NOT NULL,
                                                     final_transport_id INT NOT NULL,
                                                     departure VARCHAR(100) NOT NULL,
    destination VARCHAR(100) NOT NULL,
    vehicle_id INT NOT NULL,
    weight DECIMAL(10,2),
    estimated_price DECIMAL(15,2),
    final_price DECIMAL(15,2),
    status ENUM('pending', 'confirmed', 'inProgress', 'completed', 'cancelled') DEFAULT 'pending',
    scheduled_date DATETIME,
    completed_date DATETIME,
    notes TEXT,
    metadata JSON,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (booking_id),
    INDEX customer_booking_idx (customer_id ASC) VISIBLE,
    INDEX final_transport_option_idx (final_transport_id ASC) VISIBLE,
    CONSTRAINT customer_booking
    FOREIGN KEY (customer_id)
    REFERENCES home_express.customers (customer_id)
                                                  ON DELETE NO ACTION
                                                  ON UPDATE NO ACTION,
    CONSTRAINT final_transport_option
    FOREIGN KEY (final_transport_id)
    REFERENCES home_express.transports (transport_id)
                                                  ON DELETE NO ACTION
                                                  ON UPDATE NO ACTION
    ) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table home_express.vehicles
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS home_express.vehicles (
                                                     vehicle_id INT NOT NULL AUTO_INCREMENT,
                                                     transport_id INT NOT NULL,
                                                     type ENUM('truck', 'van', 'motorcycle', 'other') NOT NULL,
    model VARCHAR(100) NOT NULL,
    capacity DECIMAL(10,2) NOT NULL,
    status ENUM('available', 'inUse', 'maintenance') DEFAULT 'available',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (vehicle_id),
    INDEX transport_vehicle_idx (transport_id ASC),
    CONSTRAINT transport_vehicle
    FOREIGN KEY (transport_id)
    REFERENCES home_express.transports (transport_id)
                                                  ON DELETE CASCADE
                                                  ON UPDATE NO ACTION
    ) ENGINE = InnoDB;

ALTER TABLE home_express.bookings
    ADD CONSTRAINT booking_vehicle
        FOREIGN KEY (vehicle_id)
            REFERENCES home_express.vehicles (vehicle_id)
            ON DELETE NO ACTION
            ON UPDATE NO ACTION;

-- -----------------------------------------------------
-- Table home_express.transportList
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS home_express.transportList (
                                                          booking_id INT NOT NULL,
                                                          transport_id INT NOT NULL,
                                                          INDEX transport_choice_idx (transport_id ASC) VISIBLE,
    INDEX booking_detail_idx (booking_id ASC) VISIBLE,
    CONSTRAINT transport_choice
    FOREIGN KEY (transport_id)
    REFERENCES home_express.transports (transport_id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
    CONSTRAINT booking_detail
    FOREIGN KEY (booking_id)
    REFERENCES home_express.bookings (booking_id)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
    ) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table home_express.quotations
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS home_express.quotations (
                                                       quotation_id INT NOT NULL AUTO_INCREMENT,
                                                       transport_id INT NOT NULL,
                                                       booking_id INT NOT NULL,
                                                       price DECIMAL(15,2) NOT NULL,
    status ENUM('pending', 'accepted', 'rejected') NOT NULL DEFAULT 'pending',
    transporter_name VARCHAR(100),
    transporter_avatar VARCHAR(255),
    estimated_time VARCHAR(50),
    transporter_rating DECIMAL(3,2) DEFAULT 4.5,
    transporter_completed_jobs INT DEFAULT 0,
    expires_at DATETIME,
    is_selected BOOLEAN DEFAULT FALSE,
    notes TEXT,
    metadata JSON,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (quotation_id),
    INDEX transport_quotation_idx (transport_id ASC) VISIBLE,
    INDEX booking_quotation_idx (booking_id ASC) VISIBLE,
    CONSTRAINT transport_quotation
    FOREIGN KEY (transport_id)
    REFERENCES home_express.transports (transport_id)
                                                  ON DELETE NO ACTION
                                                  ON UPDATE NO ACTION,
    CONSTRAINT booking_quotation
    FOREIGN KEY (booking_id)
    REFERENCES home_express.bookings (booking_id)
                                                  ON DELETE CASCADE
                                                  ON UPDATE NO ACTION
    ) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table home_express.conversations
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS home_express.conversations (
                                                          conversation_id VARCHAR(100) NOT NULL,
    customer_id INT NOT NULL,
    transport_id INT NOT NULL,
    booking_id INT,
    last_message_id INT,
    last_message_at DATETIME,
    is_active BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (conversation_id),
    INDEX customer_conversation_idx (customer_id ASC),
    INDEX transport_conversation_idx (transport_id ASC),
    INDEX booking_conversation_idx (booking_id ASC),
    CONSTRAINT conversation_customer
    FOREIGN KEY (customer_id) REFERENCES home_express.customers (customer_id) ON DELETE CASCADE,
    CONSTRAINT conversation_transport
    FOREIGN KEY (transport_id) REFERENCES home_express.transports (transport_id) ON DELETE CASCADE,
    CONSTRAINT conversation_booking
    FOREIGN KEY (booking_id) REFERENCES home_express.bookings (booking_id) ON DELETE SET NULL
    ) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table home_express.messages
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS home_express.messages (
                                                     message_id INT NOT NULL AUTO_INCREMENT,
                                                     message TEXT NOT NULL,
                                                     customer_id INT NOT NULL,
                                                     transport_id INT NOT NULL,
                                                     conversation_id VARCHAR(100) NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    attachment_url VARCHAR(255),
    attachment_type VARCHAR(50),
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    metadata JSON,
    PRIMARY KEY (message_id),
    INDEX customer_message_idx (customer_id ASC) VISIBLE,
    INDEX transport_message_idx (transport_id ASC) VISIBLE,
    INDEX messages_timestamp (timestamp ASC),
    CONSTRAINT customer_message
    FOREIGN KEY (customer_id)
    REFERENCES home_express.customers (customer_id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
    CONSTRAINT transport_message
    FOREIGN KEY (transport_id)
    REFERENCES home_express.transports (transport_id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
    CONSTRAINT message_conversation
    FOREIGN KEY (conversation_id)
    REFERENCES home_express.conversations (conversation_id)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
    ) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table home_express.contracts
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS home_express.contracts (
                                                      contract_id INT NOT NULL AUTO_INCREMENT,
                                                      price DECIMAL(15,2) NOT NULL,
    customer_id INT NOT NULL,
    transport_id INT NOT NULL,
    booking_id INT,
    status ENUM('active', 'expired', 'cancelled') DEFAULT 'active',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (contract_id),
    INDEX customer_contract_idx (customer_id ASC) VISIBLE,
    INDEX transport_contract_idx (transport_id ASC) VISIBLE,
    CONSTRAINT customer_contract
    FOREIGN KEY (customer_id)
    REFERENCES home_express.customers (customer_id)
                                                  ON DELETE NO ACTION
                                                  ON UPDATE NO ACTION,
    CONSTRAINT transport_contract
    FOREIGN KEY (transport_id)
    REFERENCES home_express.transports (transport_id)
                                                  ON DELETE NO ACTION
                                                  ON UPDATE NO ACTION,
    CONSTRAINT contract_booking
    FOREIGN KEY (booking_id)
    REFERENCES home_express.bookings (booking_id)
                                                  ON DELETE SET NULL
                                                  ON UPDATE NO ACTION
    ) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table home_express.feedbacks
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS home_express.feedbacks (
                                                      feedback_id INT NOT NULL AUTO_INCREMENT,
                                                      booking_id INT NOT NULL,
                                                      description TEXT NOT NULL,
                                                      rating DECIMAL(3,2) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (feedback_id),
    INDEX booking_feedback_idx (booking_id ASC) VISIBLE,
    CONSTRAINT booking_feedback
    FOREIGN KEY (booking_id)
    REFERENCES home_express.bookings (booking_id)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
    ) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table home_express.reports
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS home_express.reports (
                                                    report_id INT NOT NULL AUTO_INCREMENT,
                                                    booking_id INT NOT NULL,
                                                    description TEXT NOT NULL,
                                                    customer_id INT NULL,
                                                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                                    PRIMARY KEY (report_id),
    INDEX customer_report_idx (customer_id ASC) VISIBLE,
    CONSTRAINT customer_report
    FOREIGN KEY (customer_id)
    REFERENCES home_express.customers (customer_id)
    ON DELETE SET NULL
    ON UPDATE NO ACTION,
    CONSTRAINT report_booking
    FOREIGN KEY (booking_id)
    REFERENCES home_express.bookings (booking_id)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
    ) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table home_express.penalties
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS home_express.penalties (
                                                      penalty_id INT NOT NULL AUTO_INCREMENT,
                                                      transport_id INT NULL,
                                                      description TEXT NOT NULL,
                                                      amount DECIMAL(15,2) DEFAULT 0.00,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (penalty_id),
    INDEX transport_penalty_idx (transport_id ASC) VISIBLE,
    CONSTRAINT transport_penalty
    FOREIGN KEY (transport_id)
    REFERENCES home_express.transports (transport_id)
    ON DELETE SET NULL
    ON UPDATE NO ACTION
    ) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table home_express.issues
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS home_express.issues (
                                                   issue_id INT NOT NULL AUTO_INCREMENT,
                                                   booking_id INT NULL,
                                                   description TEXT NOT NULL,
                                                   penalty_id INT NULL,
                                                   created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                                   PRIMARY KEY (issue_id),
    INDEX booking_issue_idx (booking_id ASC) VISIBLE,
    CONSTRAINT booking_issue
    FOREIGN KEY (booking_id)
    REFERENCES home_express.bookings (booking_id)
    ON DELETE SET NULL
    ON UPDATE NO ACTION,
    CONSTRAINT issue_penalty
    FOREIGN KEY (penalty_id)
    REFERENCES home_express.penalties (penalty_id)
    ON DELETE SET NULL
    ON UPDATE NO ACTION
    ) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table home_express.tickets
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS home_express.tickets (
                                                    ticket_id INT NOT NULL AUTO_INCREMENT,
                                                    transport_id INT NOT NULL,
                                                    penalty_id INT NOT NULL,
                                                    description TEXT NOT NULL,
                                                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                                    PRIMARY KEY (ticket_id),
    INDEX penalty_ticket_idx (penalty_id ASC) VISIBLE,
    INDEX transport_ticket_idx (transport_id ASC) VISIBLE,
    CONSTRAINT penalty_ticket
    FOREIGN KEY (penalty_id)
    REFERENCES home_express.penalties (penalty_id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
    CONSTRAINT transport_ticket
    FOREIGN KEY (transport_id)
    REFERENCES home_express.transports (transport_id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
    ) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table home_express.responses
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS home_express.responses (
                                                      response_id INT NOT NULL AUTO_INCREMENT,
                                                      feedback_id INT NULL,
                                                      ticket_id INT NULL,
                                                      report_id INT NULL,
                                                      issue_id INT NULL,
                                                      manager_id INT NOT NULL,
                                                      description TEXT NOT NULL,
                                                      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                                      PRIMARY KEY (response_id),
    INDEX manager_response_idx (manager_id ASC) VISIBLE,
    INDEX feedback_response_idx (feedback_id ASC) VISIBLE,
    INDEX ticket_response_idx (ticket_id ASC) VISIBLE,
    INDEX report_response_idx (report_id ASC) VISIBLE,
    INDEX issue_response_idx (issue_id ASC) VISIBLE,
    CONSTRAINT manager_response
    FOREIGN KEY (manager_id)
    REFERENCES home_express.managers (manager_id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
    CONSTRAINT feedback_response
    FOREIGN KEY (feedback_id)
    REFERENCES home_express.feedbacks (feedback_id)
    ON DELETE SET NULL
    ON UPDATE NO ACTION,
    CONSTRAINT ticket_response
    FOREIGN KEY (ticket_id)
    REFERENCES home_express.tickets (ticket_id)
    ON DELETE SET NULL
    ON UPDATE NO ACTION,
    CONSTRAINT report_response
    FOREIGN KEY (report_id)
    REFERENCES home_express.reports (report_id)
    ON DELETE SET NULL
    ON UPDATE NO ACTION,
    CONSTRAINT issue_response
    FOREIGN KEY (issue_id)
    REFERENCES home_express.issues (issue_id)
    ON DELETE SET NULL
    ON UPDATE NO ACTION
    ) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table home_express.images
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS home_express.images (
                                                   image_id INT NOT NULL AUTO_INCREMENT,
                                                   feedback_id INT NULL,
                                                   report_id INT NULL,
                                                   issue_id INT NULL,
                                                   url VARCHAR(255) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (image_id),
    CONSTRAINT image_feedback
    FOREIGN KEY (feedback_id)
    REFERENCES home_express.feedbacks (feedback_id)
    ON DELETE SET NULL
    ON UPDATE NO ACTION,
    CONSTRAINT image_report
    FOREIGN KEY (report_id)
    REFERENCES home_express.reports (report_id)
    ON DELETE SET NULL
    ON UPDATE NO ACTION,
    CONSTRAINT image_issue
    FOREIGN KEY (issue_id)
    REFERENCES home_express.issues (issue_id)
    ON DELETE SET NULL
    ON UPDATE NO ACTION
    ) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table home_express.bookingItems
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS home_express.bookingItems (
                                                         item_id INT NOT NULL AUTO_INCREMENT,
                                                         booking_id INT NOT NULL,
                                                         quantity INT NOT NULL,
                                                         name VARCHAR(100) NOT NULL,
    category_id INT NOT NULL,
    image VARCHAR(255) NOT NULL,
    weight DECIMAL(10,2),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (item_id),
    INDEX booking_item_idx (booking_id ASC) VISIBLE,
    CONSTRAINT booking_item
    FOREIGN KEY (booking_id)
    REFERENCES home_express.bookings (booking_id)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
    CONSTRAINT item_category
    FOREIGN KEY (category_id)
    REFERENCES home_express.categories (category_id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
    ) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table home_express.categoryPricing
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS home_express.categoryPricing (
                                                            pricing_id INT NOT NULL AUTO_INCREMENT,
                                                            transport_id INT NOT NULL,
                                                            category_id INT NOT NULL,
                                                            price DECIMAL(15,2) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (pricing_id),
    INDEX item_pricing_idx (transport_id ASC) VISIBLE,
    CONSTRAINT item_pricing
    FOREIGN KEY (transport_id)
    REFERENCES home_express.transports (transport_id)
                                                  ON DELETE CASCADE
                                                  ON UPDATE NO ACTION,
    CONSTRAINT pricing_category
    FOREIGN KEY (category_id)
    REFERENCES home_express.categories (category_id)
                                                  ON DELETE NO ACTION
                                                  ON UPDATE NO ACTION
    ) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table home_express.vehiclePricing
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS home_express.vehiclePricing (
                                                           vehicle_id INT NOT NULL,
                                                           transport_id INT NOT NULL,
                                                           type ENUM('truck', 'van', 'motorcycle', 'other') NOT NULL,
    price DECIMAL(15,2) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (vehicle_id),
    INDEX vehicles_pricing_idx (transport_id ASC) VISIBLE,
    CONSTRAINT vehicles_pricing
    FOREIGN KEY (transport_id)
    REFERENCES home_express.transports (transport_id)
                                                  ON DELETE CASCADE
                                                  ON UPDATE NO ACTION,
    CONSTRAINT pricing_vehicle
    FOREIGN KEY (vehicle_id)
    REFERENCES home_express.vehicles (vehicle_id)
                                                  ON DELETE CASCADE
                                                  ON UPDATE NO ACTION
    ) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table home_express.distancePricing
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS home_express.distancePricing (
                                                            pricing_id INT NOT NULL AUTO_INCREMENT,
                                                            transport_id INT NOT NULL,
                                                            vehicle_id INT NOT NULL,
                                                            price_first_4km DECIMAL(15,2) NOT NULL,
    price_5_to_40 DECIMAL(15,2) NOT NULL,
    price_after_40 DECIMAL(15,2) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (pricing_id),
    INDEX vehicles_base_price_idx (vehicle_id ASC) VISIBLE,
    CONSTRAINT vehicles_base_price
    FOREIGN KEY (vehicle_id)
    REFERENCES home_express.vehiclePricing (vehicle_id)
                                                  ON DELETE CASCADE
                                                  ON UPDATE NO ACTION,
    CONSTRAINT distance_transport
    FOREIGN KEY (transport_id)
    REFERENCES home_express.transports (transport_id)
                                                  ON DELETE CASCADE
                                                  ON UPDATE NO ACTION
    ) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS home_express.otp_codes (
                                                      otp_id INT NOT NULL AUTO_INCREMENT,
                                                      email VARCHAR(45) NOT NULL,
    code VARCHAR(6) NOT NULL,
    expires_at DATETIME NOT NULL,
    is_used BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (otp_id),
    INDEX idx_email (email ASC),
    INDEX idx_expires (expires_at ASC)
    ) ENGINE = InnoDB;
-- Tạo indexes bổ sung cho performance
CREATE INDEX idx_users_email ON home_express.users(email);
CREATE INDEX idx_users_role ON home_express.users(role);
CREATE INDEX idx_bookings_status ON home_express.bookings(status);
CREATE INDEX idx_bookings_customer ON home_express.bookings(customer_id);
CREATE INDEX idx_bookings_scheduled_date ON home_express.bookings(scheduled_date);
CREATE INDEX idx_quotations_booking ON home_express.quotations(booking_id);
CREATE INDEX idx_quotations_transport ON home_express.quotations(transport_id);
CREATE INDEX idx_messages_conversation ON home_express.messages(conversation_id);
CREATE INDEX idx_messages_timestamp ON home_express.messages(timestamp);
CREATE INDEX idx_conversations_last_message ON home_express.conversations(last_message_at);

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
