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
  userId INT NOT NULL AUTO_INCREMENT,
  username VARCHAR(45) NOT NULL,
  password VARCHAR(255) NOT NULL,  -- Tăng độ dài để hỗ trợ hash bảo mật (e.g., bcrypt)
  phone VARCHAR(20) NOT NULL,      -- Thay đổi từ INT sang VARCHAR để hỗ trợ định dạng quốc tế
  email VARCHAR(45) NOT NULL,
  role ENUM('customer', 'transport', 'manager') NOT NULL,  -- Sử dụng ENUM để hạn chế giá trị
  avatar VARCHAR(255),             -- URL ảnh đại diện
  isActive BOOLEAN DEFAULT TRUE,
  isVerified BOOLEAN DEFAULT FALSE, -- Trạng thái xác thực email/phone
  lastActive DATETIME,
  refreshToken VARCHAR(255),       -- Hỗ trợ JWT refresh token cho auth
  createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,
  updatedAt DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (userId),
  UNIQUE KEY unique_email (email), -- Đảm bảo email duy nhất
  UNIQUE KEY unique_phone (phone)  -- Đảm bảo phone duy nhất
) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table home_express.customers
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS home_express.customers (
  customerId INT NOT NULL,
  landCertificate VARCHAR(45) NOT NULL,
  address VARCHAR(100) NOT NULL,   -- Tăng độ dài cho địa chỉ đầy đủ
  createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,
  updatedAt DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (customerId),
  CONSTRAINT customer
    FOREIGN KEY (customerId)
    REFERENCES home_express.users (userId)
    ON DELETE CASCADE  -- Xóa cascade để đồng bộ
    ON UPDATE NO ACTION
) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table home_express.transports
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS home_express.transports (
  transportId INT NOT NULL,
  license VARCHAR(45) NOT NULL,
  bankQr VARCHAR(255) NOT NULL,    -- Tăng độ dài cho URL hoặc mã QR
  income DECIMAL(15,2) NOT NULL DEFAULT 0.00,
  rating DECIMAL(3,2) NOT NULL DEFAULT 0.00,
  completedJobs INT DEFAULT 0,     -- Số công việc hoàn thành
  createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,
  updatedAt DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (transportId),
  CONSTRAINT transport
    FOREIGN KEY (transportId)
    REFERENCES home_express.users (userId)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table home_express.managers
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS home_express.managers (
  managerId INT NOT NULL,
  createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,
  updatedAt DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (managerId),
  CONSTRAINT manager
    FOREIGN KEY (managerId)
    REFERENCES home_express.users (userId)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table home_express.categories
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS home_express.categories (
  categoryId INT NOT NULL AUTO_INCREMENT,
  name VARCHAR(45) NOT NULL,
  createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (categoryId)
) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table home_express.size
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS home_express.size (
  categoryId INT NOT NULL,
  weight DECIMAL(10,2) NOT NULL,
  height DECIMAL(10,2) NOT NULL,
  createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX categorySizes_idx (categoryId ASC) VISIBLE,
  CONSTRAINT categorySizes
    FOREIGN KEY (categoryId)
    REFERENCES home_express.categories (categoryId)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table home_express.bookings
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS home_express.bookings (
  bookingId INT NOT NULL AUTO_INCREMENT,
  customerId INT NOT NULL,
  finalTransportID INT NOT NULL,
  departure VARCHAR(100) NOT NULL,  -- Tăng độ dài cho địa chỉ
  destination VARCHAR(100) NOT NULL,
  vehicleId INT NOT NULL,           -- Thay đổi sang INT để liên kết với bảng vehicles
  weight DECIMAL(10,2),             -- Khối lượng hàng hóa
  estimatedPrice DECIMAL(15,2),
  finalPrice DECIMAL(15,2),
  status ENUM('pending', 'confirmed', 'inProgress', 'completed', 'cancelled') DEFAULT 'pending',
  scheduledDate DATETIME,
  completedDate DATETIME,
  notes TEXT,
  metadata JSON,
  createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,
  updatedAt DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (bookingId),
  INDEX customerBooking_idx (customerId ASC) VISIBLE,
  INDEX finalTransportOption_idx (finalTransportID ASC) VISIBLE,
  CONSTRAINT customerBooking
    FOREIGN KEY (customerId)
    REFERENCES home_express.customers (customerId)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT finalTransportOption
    FOREIGN KEY (finalTransportID)
    REFERENCES home_express.transports (transportId)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table home_express.vehicles (Bảng mới để hỗ trợ vehicleId)
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS home_express.vehicles (
  vehicleId INT NOT NULL AUTO_INCREMENT,
  transportId INT NOT NULL,
  type ENUM('truck', 'van', 'motorcycle', 'other') NOT NULL,
  model VARCHAR(100) NOT NULL,
  capacity DECIMAL(10,2) NOT NULL,  -- Dung lượng (kg)
  status ENUM('available', 'inUse', 'maintenance') DEFAULT 'available',
  createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,
  updatedAt DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (vehicleId),
  INDEX transport_vehicle_idx (transportId ASC),
  CONSTRAINT transport_vehicle
    FOREIGN KEY (transportId)
    REFERENCES home_express.transports (transportId)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
) ENGINE = InnoDB;

-- Cập nhật foreign key cho vehicleId trong bookings
ALTER TABLE home_express.bookings
  ADD CONSTRAINT booking_vehicle
    FOREIGN KEY (vehicleId)
    REFERENCES home_express.vehicles (vehicleId)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION;

-- -----------------------------------------------------
-- Table home_express.transportList
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS home_express.transportList (
  bookingId INT NOT NULL,
  transportId INT NOT NULL,
  INDEX transportChoice_idx (transportId ASC) VISIBLE,
  INDEX bookingDetail_idx (bookingId ASC) VISIBLE,
  CONSTRAINT transportChoice
    FOREIGN KEY (transportId)
    REFERENCES home_express.transports (transportId)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT bookingDetail
    FOREIGN KEY (bookingId)
    REFERENCES home_express.bookings (bookingId)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table home_express.quotations
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS home_express.quotations (
  quotationId INT NOT NULL AUTO_INCREMENT,
  transportId INT NOT NULL,
  bookingId INT NOT NULL,
  price DECIMAL(15,2) NOT NULL,
  status ENUM('pending', 'accepted', 'rejected') NOT NULL DEFAULT 'pending',
  transporterName VARCHAR(100),
  transporterAvatar VARCHAR(255),
  estimatedTime VARCHAR(50),
  transporterRating DECIMAL(3,2) DEFAULT 4.5,
  transporterCompletedJobs INT DEFAULT 0,
  expiresAt DATETIME,
  isSelected BOOLEAN DEFAULT FALSE,
  notes TEXT,
  metadata JSON,
  createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,
  updatedAt DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (quotationId),
  INDEX transportQuotation_idx (transportId ASC) VISIBLE,
  INDEX bookingQuotation_idx (bookingId ASC) VISIBLE,
  CONSTRAINT transportQuotation
    FOREIGN KEY (transportId)
    REFERENCES home_express.transports (transportId)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT bookingQuotation
    FOREIGN KEY (bookingId)
    REFERENCES home_express.bookings (bookingId)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table home_express.conversations (Bảng mới)
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS home_express.conversations (
  conversationId VARCHAR(100) NOT NULL,
  customerId INT NOT NULL,
  transportId INT NOT NULL,
  bookingId INT,                            -- Có thể null cho chat tổng quát
  lastMessageId INT,
  lastMessageAt DATETIME,
  isActive BOOLEAN DEFAULT TRUE,
  createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,
  updatedAt DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (conversationId),
  INDEX customer_conversation_idx (customerId ASC),
  INDEX transport_conversation_idx (transportId ASC),
  INDEX booking_conversation_idx (bookingId ASC),
  CONSTRAINT conversation_customer
    FOREIGN KEY (customerId) REFERENCES home_express.customers (customerId) ON DELETE CASCADE,
  CONSTRAINT conversation_transport
    FOREIGN KEY (transportId) REFERENCES home_express.transports (transportId) ON DELETE CASCADE,
  CONSTRAINT conversation_booking
    FOREIGN KEY (bookingId) REFERENCES home_express.bookings (bookingId) ON DELETE SET NULL
) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table home_express.messages
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS home_express.messages (
  messageId INT NOT NULL AUTO_INCREMENT,
  message TEXT NOT NULL,              -- Cho phép tin nhắn dài
  customerId INT NOT NULL,
  transportId INT NOT NULL,
  conversationId VARCHAR(100) NOT NULL,  -- Liên kết với conversations
  isRead BOOLEAN DEFAULT FALSE,
  attachmentUrl VARCHAR(255),
  attachmentType VARCHAR(50),
  timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
  metadata JSON,
  PRIMARY KEY (messageId),
  INDEX customerMessage_idx (customerId ASC) VISIBLE,
  INDEX transportMessage_idx (transportId ASC) VISIBLE,
  INDEX messages_timestamp (timestamp ASC),
  CONSTRAINT customerMessage
    FOREIGN KEY (customerId)
    REFERENCES home_express.customers (customerId)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT transportMessage
    FOREIGN KEY (transportId)
    REFERENCES home_express.transports (transportId)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT message_conversation
    FOREIGN KEY (conversationId)
    REFERENCES home_express.conversations (conversationId)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table home_express.contracts
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS home_express.contracts (
  contractId INT NOT NULL AUTO_INCREMENT,
  price DECIMAL(15,2) NOT NULL,
  customerId INT NOT NULL,
  transportId INT NOT NULL,
  bookingId INT,                    -- Thêm liên kết với booking
  status ENUM('active', 'expired', 'cancelled') DEFAULT 'active',
  createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,
  updatedAt DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (contractId),
  INDEX customerContract_idx (customerId ASC) VISIBLE,
  INDEX transportContract_idx (transportId ASC) VISIBLE,
  CONSTRAINT customerContract
    FOREIGN KEY (customerId)
    REFERENCES home_express.customers (customerId)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT transportContract
    FOREIGN KEY (transportId)
    REFERENCES home_express.transports (transportId)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT contract_booking
    FOREIGN KEY (bookingId)
    REFERENCES home_express.bookings (bookingId)
    ON DELETE SET NULL
    ON UPDATE NO ACTION
) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table home_express.feedbacks
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS home_express.feedbacks (
  feedbackId INT NOT NULL AUTO_INCREMENT,
  bookingId INT NOT NULL,
  description TEXT NOT NULL,
  rating DECIMAL(3,2) NOT NULL,
  createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (feedbackId),
  INDEX bookingFeedback_idx (bookingId ASC) VISIBLE,
  CONSTRAINT bookingFeedback
    FOREIGN KEY (bookingId)
    REFERENCES home_express.bookings (bookingId)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table home_express.reports
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS home_express.reports (
  reportId INT NOT NULL AUTO_INCREMENT,
  bookingId INT NOT NULL,
  description TEXT NOT NULL,
  customerId INT NULL,
  createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (reportId),
  INDEX customerReport_idx (customerId ASC) VISIBLE,
  CONSTRAINT customerReport
    FOREIGN KEY (customerId)
    REFERENCES home_express.customers (customerId)
    ON DELETE SET NULL
    ON UPDATE NO ACTION,
  CONSTRAINT report_booking
    FOREIGN KEY (bookingId)
    REFERENCES home_express.bookings (bookingId)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table home_express.penalties
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS home_express.penalties (
  penaltyId INT NOT NULL AUTO_INCREMENT,
  transportId INT NULL,
  description TEXT NOT NULL,
  amount DECIMAL(15,2) DEFAULT 0.00,  -- Số tiền phạt
  createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (penaltyId),
  INDEX transportPenalty_idx (transportId ASC) VISIBLE,
  CONSTRAINT transportPenalty
    FOREIGN KEY (transportId)
    REFERENCES home_express.transports (transportId)
    ON DELETE SET NULL
    ON UPDATE NO ACTION
) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table home_express.issues
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS home_express.issues (
  issueId INT NOT NULL AUTO_INCREMENT,
  bookingId INT NULL,
  description TEXT NOT NULL,          -- Thay đổi từ INT sang TEXT
  penaltyId INT NULL,
  createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (issueId),
  INDEX bookingIssue_idx (bookingId ASC) VISIBLE,
  CONSTRAINT bookingIssue
    FOREIGN KEY (bookingId)
    REFERENCES home_express.bookings (bookingId)
    ON DELETE SET NULL
    ON UPDATE NO ACTION,
  CONSTRAINT issue_penalty
    FOREIGN KEY (penaltyId)
    REFERENCES home_express.penalties (penaltyId)
    ON DELETE SET NULL
    ON UPDATE NO ACTION
) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table home_express.tickets
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS home_express.tickets (
  ticketId INT NOT NULL AUTO_INCREMENT,
  transportId INT NOT NULL,
  penaltyId INT NOT NULL,
  description TEXT NOT NULL,
  createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (ticketId),
  INDEX penaltyTicket_idx (penaltyId ASC) VISIBLE,
  INDEX transportTicket_idx (transportId ASC) VISIBLE,
  CONSTRAINT penaltyTicket
    FOREIGN KEY (penaltyId)
    REFERENCES home_express.penalties (penaltyId)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT transportTicket
    FOREIGN KEY (transportId)
    REFERENCES home_express.transports (transportId)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table home_express.responses
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS home_express.responses (
  responseId INT NOT NULL AUTO_INCREMENT,
  feedbackId INT NULL,
  ticketId INT NULL,
  reportId INT NULL,
  issueId INT NULL,
  managerId INT NOT NULL,
  description TEXT NOT NULL,          -- Thêm trường mô tả phản hồi
  createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (responseId),
  INDEX managerResponse_idx (managerId ASC) VISIBLE,
  INDEX feedbackResponse_idx (feedbackId ASC) VISIBLE,
  INDEX ticketResponse_idx (ticketId ASC) VISIBLE,
  INDEX reportResponse_idx (reportId ASC) VISIBLE,
  INDEX issueResponse_idx (issueId ASC) VISIBLE,
  CONSTRAINT managerResponse
    FOREIGN KEY (managerId)
    REFERENCES home_express.managers (managerId)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT feedbackResponse
    FOREIGN KEY (feedbackId)
    REFERENCES home_express.feedbacks (feedbackId)
    ON DELETE SET NULL
    ON UPDATE NO ACTION,
  CONSTRAINT ticketResponse
    FOREIGN KEY (ticketId)
    REFERENCES home_express.tickets (ticketId)
    ON DELETE SET NULL
    ON UPDATE NO ACTION,
  CONSTRAINT reportResponse
    FOREIGN KEY (reportId)
    REFERENCES home_express.reports (reportId)
    ON DELETE SET NULL
    ON UPDATE NO ACTION,
  CONSTRAINT issueResponse
    FOREIGN KEY (issueId)
    REFERENCES home_express.issues (issueId)
    ON DELETE SET NULL
    ON UPDATE NO ACTION
) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table home_express.images
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS home_express.images (
  imageId INT NOT NULL AUTO_INCREMENT,
  feedbackId INT NULL,
  reportId INT NULL,
  issueId INT NULL,
  url VARCHAR(255) NOT NULL,
  createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (imageId),
  CONSTRAINT image_feedback
    FOREIGN KEY (feedbackId)
    REFERENCES home_express.feedbacks (feedbackId)
    ON DELETE SET NULL
    ON UPDATE NO ACTION,
  CONSTRAINT image_report
    FOREIGN KEY (reportId)
    REFERENCES home_express.reports (reportId)
    ON DELETE SET NULL
    ON UPDATE NO ACTION,
  CONSTRAINT image_issue
    FOREIGN KEY (issueId)
    REFERENCES home_express.issues (issueId)
    ON DELETE SET NULL
    ON UPDATE NO ACTION
) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table home_express.bookingItems
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS home_express.bookingItems (
  itemId INT NOT NULL AUTO_INCREMENT,  -- Thêm primary key
  bookingId INT NOT NULL,
  quantity INT NOT NULL,
  name VARCHAR(100) NOT NULL,
  categoryId INT NOT NULL,
  image VARCHAR(255) NOT NULL,
  weight DECIMAL(10,2),               -- Thêm trọng lượng cho item
  createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (itemId),
  INDEX bookingItem_idx (bookingId ASC) VISIBLE,
  CONSTRAINT bookingItem
    FOREIGN KEY (bookingId)
    REFERENCES home_express.bookings (bookingId)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT item_category
    FOREIGN KEY (categoryId)
    REFERENCES home_express.categories (categoryId)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table home_express.categoryPricing
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS home_express.categoryPricing (
  pricingId INT NOT NULL AUTO_INCREMENT,  -- Thêm primary key
  transportId INT NOT NULL,
  categoryId INT NOT NULL,
  price DECIMAL(15,2) NOT NULL,
  createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,
  updatedAt DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (pricingId),
  INDEX itemPricing_idx (transportId ASC) VISIBLE,
  CONSTRAINT itemPricing
    FOREIGN KEY (transportId)
    REFERENCES home_express.transports (transportId)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT pricing_category
    FOREIGN KEY (categoryId)
    REFERENCES home_express.categories (categoryId)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table home_express.vehiclePricing
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS home_express.vehiclePricing (
  vehicleId INT NOT NULL,
  transportId INT NOT NULL,
  type ENUM('truck', 'van', 'motorcycle', 'other') NOT NULL,  -- Đồng bộ với vehicles
  price DECIMAL(15,2) NOT NULL,
  createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,
  updatedAt DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (vehicleId),
  INDEX vehilesPricing_idx (transportId ASC) VISIBLE,
  CONSTRAINT vehilesPricing
    FOREIGN KEY (transportId)
    REFERENCES home_express.transports (transportId)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT pricing_vehicle
    FOREIGN KEY (vehicleId)
    REFERENCES home_express.vehicles (vehicleId)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table home_express.distancePricing
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS home_express.distancePricing (
  pricingId INT NOT NULL AUTO_INCREMENT,  -- Thêm primary key
  transportId INT NOT NULL,
  vehicleId INT NOT NULL,
  priceFirst4km DECIMAL(15,2) NOT NULL,
  price5to40 DECIMAL(15,2) NOT NULL,
  priceAfter40 DECIMAL(15,2) NOT NULL,
  createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,
  updatedAt DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (pricingId),
  INDEX vehiclesBasePrice_idx (vehicleId ASC) VISIBLE,
  CONSTRAINT vehiclesBasePrice
    FOREIGN KEY (vehicleId)
    REFERENCES home_express.vehiclePricing (vehicleId)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT distance_transport
    FOREIGN KEY (transportId)
    REFERENCES home_express.transports (transportId)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
) ENGINE = InnoDB;
sys_configPRIMARYsys_config_insert_set_usersys_config_update_set_user
-- Tạo indexes bổ sung cho performance
CREATE INDEX idx_users_email ON home_express.users(email);
CREATE INDEX idx_users_role ON home_express.users(role);
CREATE INDEX idx_bookings_status ON home_express.bookings(status);
CREATE INDEX idx_bookings_customer ON home_express.bookings(customerId);
CREATE INDEX idx_bookings_scheduled_date ON home_express.bookings(scheduledDate);
CREATE INDEX idx_quotations_booking ON home_express.quotations(bookingId);
CREATE INDEX idx_quotations_transport ON home_express.quotations(transportId);
CREATE INDEX idx_messages_conversation ON home_express.messages(conversationId);
CREATE INDEX idx_messages_timestamp ON home_express.messages(timestamp);
CREATE INDEX idx_conversations_last_message ON home_express.conversations(lastMessageAt);

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
