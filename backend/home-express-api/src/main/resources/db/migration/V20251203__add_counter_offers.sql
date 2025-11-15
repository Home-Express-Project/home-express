-- Migration: Add Counter-offer System
-- Description: Creates tables and indexes for counter-offer negotiation between customers and transport providers
-- Author: Home Express Team
-- Date: 2025-12-03

-- ============================================================================
-- COUNTER OFFERS TABLE
-- ============================================================================

CREATE TABLE counter_offers (
    counter_offer_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    quotation_id BIGINT NOT NULL,
    booking_id BIGINT NOT NULL,
    
    -- Offer details
    offered_by_user_id BIGINT NOT NULL COMMENT 'User who made this counter-offer',
    offered_by_role ENUM('CUSTOMER', 'TRANSPORT') NOT NULL COMMENT 'Role of the user making the offer',
    
    -- Pricing
    offered_price DECIMAL(15, 2) NOT NULL COMMENT 'Counter-offered price in VND',
    original_price DECIMAL(15, 2) NOT NULL COMMENT 'Original price being countered',
    
    -- Status and lifecycle
    status ENUM('PENDING', 'ACCEPTED', 'REJECTED', 'EXPIRED', 'SUPERSEDED') NOT NULL DEFAULT 'PENDING',
    
    -- Negotiation details
    message TEXT COMMENT 'Optional message explaining the counter-offer',
    reason VARCHAR(500) COMMENT 'Reason for the counter-offer',
    
    -- Response tracking
    responded_by_user_id BIGINT COMMENT 'User who responded to this counter-offer',
    responded_at TIMESTAMP NULL COMMENT 'When the counter-offer was responded to',
    response_message TEXT COMMENT 'Response message from the other party',
    
    -- Expiration
    expires_at TIMESTAMP NOT NULL COMMENT 'When this counter-offer expires',
    
    -- Audit fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign keys
    CONSTRAINT fk_counter_offer_quotation FOREIGN KEY (quotation_id) 
        REFERENCES quotations(quotation_id) ON DELETE CASCADE,
    CONSTRAINT fk_counter_offer_booking FOREIGN KEY (booking_id) 
        REFERENCES bookings(booking_id) ON DELETE CASCADE,
    
    -- Indexes for performance
    INDEX idx_counter_offer_quotation (quotation_id),
    INDEX idx_counter_offer_booking (booking_id),
    INDEX idx_counter_offer_offered_by (offered_by_user_id),
    INDEX idx_counter_offer_status (status),
    INDEX idx_counter_offer_expires_at (expires_at),
    INDEX idx_counter_offer_created_at (created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Counter-offers for price negotiation between customers and transport providers';

-- ============================================================================
-- COUNTER OFFER HISTORY TABLE (Optional - for audit trail)
-- ============================================================================

CREATE TABLE counter_offer_history (
    history_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    counter_offer_id BIGINT NOT NULL,
    
    -- Change tracking
    action ENUM('CREATED', 'ACCEPTED', 'REJECTED', 'EXPIRED', 'SUPERSEDED') NOT NULL,
    actor_user_id BIGINT NOT NULL COMMENT 'User who performed the action',
    actor_role ENUM('CUSTOMER', 'TRANSPORT', 'MANAGER') NOT NULL,
    
    -- Details
    old_status ENUM('PENDING', 'ACCEPTED', 'REJECTED', 'EXPIRED', 'SUPERSEDED'),
    new_status ENUM('PENDING', 'ACCEPTED', 'REJECTED', 'EXPIRED', 'SUPERSEDED'),
    notes TEXT COMMENT 'Additional notes about the action',
    
    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key
    CONSTRAINT fk_counter_offer_history_offer FOREIGN KEY (counter_offer_id) 
        REFERENCES counter_offers(counter_offer_id) ON DELETE CASCADE,
    
    -- Indexes
    INDEX idx_counter_offer_history_offer (counter_offer_id),
    INDEX idx_counter_offer_history_actor (actor_user_id),
    INDEX idx_counter_offer_history_created (created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Audit trail for counter-offer status changes';

-- ============================================================================
-- INITIAL DATA / CONSTRAINTS
-- ============================================================================

-- Add constraint to ensure offered_price is positive
ALTER TABLE counter_offers 
ADD CONSTRAINT chk_counter_offer_price_positive 
CHECK (offered_price > 0);

-- Add constraint to ensure original_price is positive
ALTER TABLE counter_offers 
ADD CONSTRAINT chk_counter_offer_original_price_positive 
CHECK (original_price > 0);

-- Add constraint to ensure expires_at is in the future (at creation time)
-- Note: This is enforced in application logic, not at DB level

-- ============================================================================
-- COMMENTS
-- ============================================================================

-- Add table comments for documentation
ALTER TABLE counter_offers COMMENT = 
'Counter-offers allow customers and transport providers to negotiate prices. 
Each counter-offer references a quotation and can be accepted, rejected, or superseded by a new counter-offer.
Counter-offers expire after a configurable time period (default: 24 hours).';

ALTER TABLE counter_offer_history COMMENT = 
'Audit trail for all counter-offer status changes. 
Tracks who made changes, when, and what the old/new status was.';

