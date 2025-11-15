-- Create quotations table required by Quotation JPA entity

CREATE TABLE IF NOT EXISTS quotations (
    quotation_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    transport_id BIGINT NOT NULL,
    quoted_price DECIMAL(12, 0) NOT NULL,
    base_price DECIMAL(12, 0) DEFAULT NULL,
    distance_price DECIMAL(12, 0) DEFAULT NULL,
    items_price DECIMAL(12, 0) DEFAULT NULL,
    additional_fees DECIMAL(12, 0) DEFAULT NULL,
    discount DECIMAL(12, 0) DEFAULT NULL,
    price_breakdown JSON DEFAULT NULL,
    notes TEXT DEFAULT NULL,
    validity_period INT DEFAULT 7,
    expires_at DATETIME DEFAULT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    responded_at DATETIME DEFAULT NULL,
    accepted_by BIGINT DEFAULT NULL,
    accepted_at DATETIME DEFAULT NULL,
    accepted_ip VARCHAR(45) DEFAULT NULL,
    accepted_booking_id BIGINT DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_quotations_booking
        FOREIGN KEY (booking_id) REFERENCES bookings(booking_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_quotations_transport
        FOREIGN KEY (transport_id) REFERENCES transports(transport_id)
        ON DELETE CASCADE
);

CREATE INDEX idx_quotations_booking
    ON quotations (booking_id);

CREATE INDEX idx_quotations_transport_created
    ON quotations (transport_id, created_at);

CREATE INDEX idx_quotations_transport_status
    ON quotations (transport_id, status);

CREATE INDEX idx_quotations_status
    ON quotations (status);

CREATE INDEX idx_quotations_expires
    ON quotations (expires_at);
