-- ============================================================================
-- Create email_logs table for email tracking
-- ============================================================================
-- Migration: V20251145__create_email_logs_table.sql
-- Description: Create email_logs table for tracking all emails sent by the system
-- Date: 2025-02-14
-- Issue: Missing email_logs table for email delivery tracking

-- ============================================================================
-- EMAIL_LOGS TABLE
-- ============================================================================
-- Purpose: Track all emails sent by the system with delivery status
--   - Verification emails, booking confirmations, notifications
--   - Provider integration (SMTP, SendGrid, SES, Mailgun)
--   - Detailed lifecycle tracking (queued, sent, delivered, opened, clicked, bounced, complained, failed)
--   - Event timestamps for each lifecycle stage
--   - Tracking tokens for open/click tracking
-- ============================================================================

CREATE TABLE IF NOT EXISTS email_logs (
    log_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    
    -- References
    user_id BIGINT DEFAULT NULL COMMENT 'User this email is for (NULL for non-user emails)',
    to_email VARCHAR(255) NOT NULL COMMENT 'Recipient email address',
    
    -- Email Details
    subject VARCHAR(500) NOT NULL COMMENT 'Email subject line',
    template_name VARCHAR(100) DEFAULT NULL COMMENT 'Email template used',
    
    -- Provider Integration
    provider ENUM('SMTP', 'SENDGRID', 'SES', 'MAILGUN') DEFAULT 'SMTP' COMMENT 'Email service provider',
    provider_message_id VARCHAR(128) DEFAULT NULL COMMENT 'External provider message ID',
    tracking_token CHAR(22) DEFAULT NULL COMMENT 'URL-safe token for open/click tracking',
    
    -- Status (Detailed lifecycle)
    status ENUM('QUEUED', 'SENT', 'DELIVERED', 'OPENED', 'CLICKED', 'BOUNCED', 'COMPLAINED', 'FAILED') DEFAULT 'QUEUED' COMMENT 'Email delivery status',
    error_code VARCHAR(64) DEFAULT NULL COMMENT 'Error code if failed',
    error_message VARCHAR(255) DEFAULT NULL COMMENT 'Error message if failed',
    
    -- Event Timestamps (Detailed tracking)
    queued_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'When email was queued',
    sent_at DATETIME DEFAULT NULL COMMENT 'When email was sent',
    delivered_at DATETIME DEFAULT NULL COMMENT 'When email was delivered',
    opened_at DATETIME DEFAULT NULL COMMENT 'When email was opened',
    clicked_at DATETIME DEFAULT NULL COMMENT 'When email link was clicked',
    bounced_at DATETIME DEFAULT NULL COMMENT 'When email bounced',
    complained_at DATETIME DEFAULT NULL COMMENT 'When spam complaint was received',
    failed_at DATETIME DEFAULT NULL COMMENT 'When email failed',
    
    -- Metadata
    metadata JSON DEFAULT NULL COMMENT 'Additional tracking data',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation timestamp',
    
    PRIMARY KEY (log_id),
    
    -- Unique constraints
    UNIQUE KEY uk_email_provider_id (provider_message_id),
    UNIQUE KEY uk_email_tracking (tracking_token),
    
    -- Indexes for performance
    KEY idx_email_logs_user_created (user_id, created_at DESC),
    KEY idx_email_logs_status (status, created_at DESC),
    KEY idx_email_logs_template (template_name, created_at DESC),
    KEY idx_email_logs_to_email (to_email, created_at DESC),
    KEY idx_email_logs_provider (provider, status, created_at DESC),
    
    -- Foreign keys
    CONSTRAINT fk_email_logs_user
        FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON DELETE SET NULL
        ON UPDATE NO ACTION
        
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Email delivery tracking with provider webhooks support';

-- ============================================================================
-- Verify table was created
-- ============================================================================
SELECT 
    TABLE_NAME,
    TABLE_ROWS,
    CREATE_TIME,
    TABLE_COMMENT
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'email_logs';

-- ============================================================================
-- Display table structure
-- ============================================================================
DESCRIBE email_logs;

