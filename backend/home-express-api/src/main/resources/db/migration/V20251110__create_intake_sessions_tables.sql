-- ============================================================================
-- Intake Sessions Module
-- ============================================================================
-- Purpose: Store temporary intake sessions for item detection workflow
-- Tables: intake_sessions, intake_session_items
-- ============================================================================

-- Intake Sessions Table
CREATE TABLE IF NOT EXISTS intake_sessions (
    session_id VARCHAR(100) PRIMARY KEY,
    user_id BIGINT,
    status VARCHAR(20) NOT NULL DEFAULT 'active',
    total_items INT DEFAULT 0,
    estimated_volume DECIMAL(10, 2),
    ai_service_used VARCHAR(50),
    average_confidence DECIMAL(5, 4),
    metadata JSON,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    
    INDEX idx_intake_sessions_user_id (user_id),
    INDEX idx_intake_sessions_status (status),
    INDEX idx_intake_sessions_expires_at (expires_at),
    INDEX idx_intake_sessions_created_at (created_at),
    
    CONSTRAINT fk_intake_sessions_user
        FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Intake Session Items Table
CREATE TABLE IF NOT EXISTS intake_session_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(100) NOT NULL,
    item_id VARCHAR(100),
    name VARCHAR(255) NOT NULL,
    category VARCHAR(100),
    description TEXT,
    quantity INT NOT NULL DEFAULT 1,
    length_cm DECIMAL(10, 2),
    width_cm DECIMAL(10, 2),
    height_cm DECIMAL(10, 2),
    weight_kg DECIMAL(10, 2),
    volume_m3 DECIMAL(10, 4),
    is_fragile BOOLEAN DEFAULT FALSE,
    is_high_value BOOLEAN DEFAULT FALSE,
    requires_disassembly BOOLEAN DEFAULT FALSE,
    image_url TEXT,
    confidence DECIMAL(5, 4),
    ai_detected BOOLEAN DEFAULT FALSE,
    source VARCHAR(50),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_intake_items_session_id (session_id),
    INDEX idx_intake_items_category (category),
    INDEX idx_intake_items_created_at (created_at),
    
    CONSTRAINT fk_intake_items_session
        FOREIGN KEY (session_id) REFERENCES intake_sessions(session_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
