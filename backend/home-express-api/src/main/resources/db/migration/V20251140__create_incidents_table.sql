-- ============================================================================
-- Create incidents table for incident tracking
-- ============================================================================
-- Migration: V20251140__create_incidents_table.sql
-- Description: Create incidents table for tracking incidents, disputes, and issues
-- Date: 2025-02-09
-- Issue: Missing incidents table for incident management

-- ============================================================================
-- INCIDENTS TABLE
-- ============================================================================
-- Purpose: Report and track issues/disputes during booking lifecycle
--   - Damage reports, customer complaints, transport disputes
--   - Severity-based prioritization (LOW, MEDIUM, HIGH, CRITICAL)
--   - SLA tracking with automatic deadline calculation
--   - Resolution workflow (PENDING → ACKNOWLEDGED → INVESTIGATING → RESOLVED → CLOSED)
--   - Financial impact tracking (estimated loss, claimed compensation, actual compensation)
-- ============================================================================

CREATE TABLE IF NOT EXISTS incidents (
    incident_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    
    -- References
    booking_id BIGINT NOT NULL COMMENT 'Booking this incident relates to',
    
    -- Reporter Info
    reported_by BIGINT NOT NULL COMMENT 'User who reported (customer, transport, or manager)',
    reported_by_role ENUM('CUSTOMER', 'TRANSPORT', 'MANAGER') NOT NULL COMMENT 'Role of reporter',
    
    -- Incident Classification
    incident_type ENUM(
        'DAMAGE',             -- Damaged goods
        'MISSING_ITEM',       -- Missing items
        'DELAY',              -- Delivery delay
        'WRONG_ADDRESS',      -- Wrong address
        'PRICE_DISPUTE',      -- Price dispute
        'SERVICE_QUALITY',    -- Poor service quality
        'UNPROFESSIONAL',     -- Unprofessional behavior
        'SAFETY_VIOLATION',   -- Safety violation
        'FRAUD',              -- Fraud
        'OTHER'
    ) NOT NULL COMMENT 'Type of incident',
    
    -- Severity (for prioritization)
    severity ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') NOT NULL DEFAULT 'MEDIUM' COMMENT 'Incident severity',
    
    -- Incident Details
    title VARCHAR(200) NOT NULL COMMENT 'Short incident title',
    description TEXT NOT NULL COMMENT 'Detailed incident description',
    
    -- Financial Impact (All amounts in VND - DECIMAL(12,0) for integer values)
    estimated_loss_vnd DECIMAL(12,0) DEFAULT NULL COMMENT 'Estimated loss in VND',
    claimed_compensation_vnd DECIMAL(12,0) DEFAULT NULL COMMENT 'Claimed compensation in VND',
    
    -- Evidence
    evidence_image_ids JSON DEFAULT NULL COMMENT 'Array of evidence_id references',
    supporting_documents JSON DEFAULT NULL COMMENT 'Array of document URLs',
    
    -- Resolution Workflow
    status ENUM(
        'PENDING',            -- Newly created, awaiting processing
        'ACKNOWLEDGED',       -- Acknowledged by support
        'INVESTIGATING',      -- Under investigation
        'ESCALATED',          -- Escalated to manager
        'RESOLVED',           -- Resolved
        'CLOSED'              -- Closed (resolved or rejected)
    ) DEFAULT 'PENDING' COMMENT 'Incident status',
    
    resolution_status ENUM('PENDING', 'COMPENSATED', 'REFUNDED', 'REJECTED', 'SETTLED') DEFAULT 'PENDING' COMMENT 'Resolution outcome',
    
    -- Resolution Details
    resolved_by BIGINT DEFAULT NULL COMMENT 'Manager who resolved the incident',
    resolution_notes TEXT DEFAULT NULL COMMENT 'Resolution details',
    resolution_action VARCHAR(500) DEFAULT NULL COMMENT 'Actions taken',
    compensation_paid_vnd DECIMAL(12,0) DEFAULT NULL COMMENT 'Actual compensation paid in VND',
    
    -- Priority & SLA
    priority INT NOT NULL DEFAULT 3 COMMENT 'Priority level (1=Highest, 5=Lowest)',
    sla_due_at DATETIME DEFAULT NULL COMMENT 'SLA deadline for response',
    first_response_at DATETIME DEFAULT NULL COMMENT 'When first response was provided',
    resolved_at DATETIME DEFAULT NULL COMMENT 'When incident was resolved',
    closed_at DATETIME DEFAULT NULL COMMENT 'When incident was closed',
    
    -- Communication Tracking
    customer_notified BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Customer has been notified',
    transport_notified BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Transport has been notified',
    manager_notified BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Manager has been notified',
    
    -- Metadata
    tags JSON DEFAULT NULL COMMENT 'Tags for categorization (e.g., ["insurance", "legal"])',
    internal_notes TEXT DEFAULT NULL COMMENT 'Internal notes (not visible to customer/transport)',
    
    -- Timestamps
    reported_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'When incident was reported',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation timestamp',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
    
    PRIMARY KEY (incident_id),
    
    -- Indexes for performance
    KEY idx_incidents_booking (booking_id, status),
    KEY idx_incidents_reported_by (reported_by, reported_at DESC),
    KEY idx_incidents_status_severity (status, severity, reported_at DESC),
    KEY idx_incidents_type (incident_type, status),
    KEY idx_incidents_sla (status, sla_due_at),
    KEY idx_incidents_resolved_by (resolved_by, resolved_at DESC),
    KEY idx_incidents_pending_sla (status, sla_due_at, severity) USING BTREE,
    
    -- Constraints
    CONSTRAINT chk_incidents_amounts_positive
        CHECK (
            (estimated_loss_vnd IS NULL OR estimated_loss_vnd >= 0) AND
            (claimed_compensation_vnd IS NULL OR claimed_compensation_vnd >= 0) AND
            (compensation_paid_vnd IS NULL OR compensation_paid_vnd >= 0)
        ),
    
    CONSTRAINT chk_incidents_priority_range
        CHECK (priority BETWEEN 1 AND 5),
    
    -- Foreign keys
    CONSTRAINT fk_incidents_booking
        FOREIGN KEY (booking_id) REFERENCES bookings(booking_id)
        ON DELETE NO ACTION
        ON UPDATE NO ACTION,
    
    CONSTRAINT fk_incidents_reported_by
        FOREIGN KEY (reported_by) REFERENCES users(user_id)
        ON DELETE NO ACTION
        ON UPDATE NO ACTION,
    
    CONSTRAINT fk_incidents_resolved_by
        FOREIGN KEY (resolved_by) REFERENCES users(user_id)
        ON DELETE SET NULL
        ON UPDATE NO ACTION
        
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Incident reports and dispute tracking for bookings';

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
  AND TABLE_NAME = 'incidents';

-- ============================================================================
-- Display table structure
-- ============================================================================
DESCRIBE incidents;

