-- Migration: Create dispute management tables
-- Version: V20251202
-- Description: Create tables for dispute filing system including disputes, messages, and evidence junction

-- Create dispute_type enum
CREATE TABLE IF NOT EXISTS dispute_type_enum (
    type_value VARCHAR(50) PRIMARY KEY
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert dispute types only if they don't exist
INSERT IGNORE INTO dispute_type_enum (type_value) VALUES
    ('PRICING_DISPUTE'),
    ('DAMAGE_CLAIM'),
    ('SERVICE_QUALITY'),
    ('DELIVERY_ISSUE'),
    ('PAYMENT_ISSUE'),
    ('OTHER');

-- Create dispute_status enum
CREATE TABLE IF NOT EXISTS dispute_status_enum (
    status_value VARCHAR(50) PRIMARY KEY
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert dispute statuses only if they don't exist
INSERT IGNORE INTO dispute_status_enum (status_value) VALUES
    ('PENDING'),
    ('UNDER_REVIEW'),
    ('RESOLVED'),
    ('REJECTED'),
    ('ESCALATED');

-- Create disputes table
CREATE TABLE IF NOT EXISTS disputes (
    dispute_id BIGINT NOT NULL AUTO_INCREMENT,
    booking_id BIGINT NOT NULL,
    filed_by_user_id BIGINT NOT NULL,
    dispute_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    requested_resolution TEXT DEFAULT NULL,
    resolution_notes TEXT DEFAULT NULL,
    resolved_by_user_id BIGINT DEFAULT NULL,
    resolved_at DATETIME DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (dispute_id),
    KEY idx_disputes_booking (booking_id),
    KEY idx_disputes_filed_by (filed_by_user_id),
    KEY idx_disputes_status (status),
    KEY idx_disputes_type (dispute_type),
    KEY idx_disputes_resolved_by (resolved_by_user_id),
    KEY idx_disputes_created_at (created_at),
    CONSTRAINT fk_disputes_booking
        FOREIGN KEY (booking_id) REFERENCES bookings(booking_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_disputes_filed_by_user
        FOREIGN KEY (filed_by_user_id) REFERENCES users(user_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_disputes_resolved_by_user
        FOREIGN KEY (resolved_by_user_id) REFERENCES users(user_id)
        ON DELETE SET NULL,
    CONSTRAINT fk_disputes_type
        FOREIGN KEY (dispute_type) REFERENCES dispute_type_enum(type_value),
    CONSTRAINT fk_disputes_status
        FOREIGN KEY (status) REFERENCES dispute_status_enum(status_value)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Disputes filed by customers for bookings';

-- Create dispute_messages table
CREATE TABLE IF NOT EXISTS dispute_messages (
    message_id BIGINT NOT NULL AUTO_INCREMENT,
    dispute_id BIGINT NOT NULL,
    sender_user_id BIGINT NOT NULL,
    message_text TEXT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (message_id),
    KEY idx_dispute_messages_dispute (dispute_id),
    KEY idx_dispute_messages_sender (sender_user_id),
    KEY idx_dispute_messages_created_at (created_at),
    CONSTRAINT fk_dispute_messages_dispute
        FOREIGN KEY (dispute_id) REFERENCES disputes(dispute_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_dispute_messages_sender
        FOREIGN KEY (sender_user_id) REFERENCES users(user_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create dispute_evidence junction table
CREATE TABLE IF NOT EXISTS dispute_evidence (
    dispute_id BIGINT NOT NULL,
    evidence_id BIGINT NOT NULL,
    added_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (dispute_id, evidence_id),
    KEY idx_dispute_evidence_dispute (dispute_id),
    KEY idx_dispute_evidence_evidence (evidence_id),
    CONSTRAINT fk_dispute_evidence_dispute
        FOREIGN KEY (dispute_id) REFERENCES disputes(dispute_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_dispute_evidence_evidence
        FOREIGN KEY (evidence_id) REFERENCES evidence(evidence_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add comments for documentation
ALTER TABLE disputes COMMENT = 'Disputes filed by customers or transport providers for bookings';
ALTER TABLE dispute_messages COMMENT = 'Message thread for dispute communication';
ALTER TABLE dispute_evidence COMMENT = 'Junction table linking disputes to evidence files';

