-- ============================================================================
-- Create audit_log table for comprehensive audit trail
-- ============================================================================
-- Migration: V20251124__create_audit_log_table.sql
-- Description: Create audit_log table for tracking all changes to critical tables
-- Date: 2025-01-24
-- Issue: Missing audit_log table for security and compliance

-- ============================================================================
-- AUDIT_LOG TABLE
-- ============================================================================
-- Purpose: Complete audit trail for all changes to Auth & Users tables
--   - Track INSERT, UPDATE, DELETE operations
--   - Store before/after data as JSON
--   - Track actor (who made the change)
--   - Support request correlation via request_id
-- ============================================================================

CREATE TABLE IF NOT EXISTS audit_log (
    audit_id BIGINT NOT NULL AUTO_INCREMENT,
    table_name VARCHAR(64) NOT NULL COMMENT 'Name of the table that was modified',
    action ENUM('INSERT', 'UPDATE', 'DELETE') NOT NULL COMMENT 'Type of operation',
    row_pk VARCHAR(255) DEFAULT NULL COMMENT 'Primary key value of the affected row',
    old_data JSON DEFAULT NULL COMMENT 'Row data before change (for UPDATE/DELETE)',
    new_data JSON DEFAULT NULL COMMENT 'Row data after change (for INSERT/UPDATE)',
    
    -- Actor tracking (set via application)
    actor_id BIGINT DEFAULT NULL COMMENT 'User who performed the action',
    actor_role VARCHAR(20) DEFAULT NULL COMMENT 'Role of the actor (CUSTOMER, TRANSPORT, MANAGER)',
    request_id VARCHAR(36) DEFAULT NULL COMMENT 'Request correlation ID for tracing',
    
    occurred_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'When the action occurred',
    
    PRIMARY KEY (audit_id),
    
    -- Indexes for querying audit logs
    KEY idx_audit_log_table_time (table_name, occurred_at DESC) COMMENT 'Find audits by table',
    KEY idx_audit_log_actor_time (actor_id, occurred_at DESC) COMMENT 'Find audits by actor',
    KEY idx_audit_log_time (occurred_at DESC) COMMENT 'Find recent audits',
    
    -- Foreign key
    CONSTRAINT fk_audit_log_actor
        FOREIGN KEY (actor_id) REFERENCES users(user_id)
        ON DELETE SET NULL
        ON UPDATE NO ACTION
        
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Complete audit trail for all changes to Auth & Users tables';

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
  AND TABLE_NAME = 'audit_log';

-- ============================================================================
-- Display table structure
-- ============================================================================
DESCRIBE audit_log;

