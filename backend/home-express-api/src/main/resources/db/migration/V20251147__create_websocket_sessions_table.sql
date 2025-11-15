-- ============================================================================
-- Create websocket_sessions table for WebSocket connection tracking
-- ============================================================================
-- Migration: V20251147__create_websocket_sessions_table.sql
-- Description: Create websocket_sessions table for tracking active WebSocket connections
-- Date: 2025-02-16
-- Issue: Missing websocket_sessions table for real-time connection tracking

-- ============================================================================
-- WEBSOCKET_SESSIONS TABLE
-- ============================================================================
-- Purpose: Track active WebSocket connections for real-time features
--   - Live booking updates, chat, notifications
--   - Multi-node support with node_id for load balancing
--   - Heartbeat tracking for connection health
--   - Connection metadata (IP address, user agent)
--   - Audit trail for debugging
-- Note: Use Redis for active routing, this table is for audit/backup
-- ============================================================================

CREATE TABLE IF NOT EXISTS websocket_sessions (
    session_id CHAR(36) NOT NULL COMMENT 'UUID session identifier',
    
    -- References
    user_id BIGINT NOT NULL COMMENT 'User who owns this WebSocket connection',
    
    -- Connection Details
    node_id VARCHAR(64) DEFAULT NULL COMMENT 'Backend server node identifier',
    connected_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'When connection was established',
    last_heartbeat_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Last heartbeat timestamp',
    disconnected_at DATETIME DEFAULT NULL COMMENT 'When connection was closed',
    
    -- Client Information
    ip_address VARCHAR(45) DEFAULT NULL COMMENT 'Client IP address (IPv4 or IPv6)',
    user_agent TEXT DEFAULT NULL COMMENT 'Client user agent string',
    
    -- Metadata
    connection_metadata JSON DEFAULT NULL COMMENT 'Additional connection metadata',
    
    PRIMARY KEY (session_id),
    
    -- Indexes for performance
    KEY idx_ws_sessions_user_heartbeat (user_id, last_heartbeat_at DESC),
    KEY idx_ws_sessions_node (node_id, last_heartbeat_at DESC),
    KEY idx_ws_sessions_active (disconnected_at, last_heartbeat_at DESC),
    KEY idx_ws_sessions_connected (connected_at DESC),
    
    -- Foreign keys
    CONSTRAINT fk_ws_sessions_user
        FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON DELETE CASCADE
        ON UPDATE NO ACTION
        
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='WebSocket session tracking (audit/backup, use Redis for routing)';

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
  AND TABLE_NAME = 'websocket_sessions';

-- ============================================================================
-- Display table structure
-- ============================================================================
DESCRIBE websocket_sessions;

