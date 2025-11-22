CREATE TABLE IF NOT EXISTS rate_cards (
    rate_card_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transport_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    base_price DECIMAL(12, 0) NULL,
    price_per_km DECIMAL(12, 0) NULL,
    price_per_hour DECIMAL(12, 0) NULL,
    minimum_charge DECIMAL(12, 0) NULL,
    valid_from DATETIME NULL,
    valid_until DATETIME NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    additional_rules JSON NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_rate_card_transport FOREIGN KEY (transport_id) REFERENCES transports(transport_id),
    CONSTRAINT fk_rate_card_category FOREIGN KEY (category_id) REFERENCES categories(category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE transports
    ADD COLUMN ready_to_quote BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN rate_card_expires_at DATETIME NULL;

CREATE TABLE IF NOT EXISTS rate_card_snapshots (
    snapshot_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    quotation_id BIGINT NOT NULL,
    transport_id BIGINT NOT NULL,
    rate_card_id BIGINT NULL,
    category_id BIGINT NULL,
    pricing_snapshot JSON NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_snapshot_quotation FOREIGN KEY (quotation_id) REFERENCES quotations(quotation_id),
    CONSTRAINT fk_snapshot_transport FOREIGN KEY (transport_id) REFERENCES transports(transport_id),
    CONSTRAINT fk_snapshot_rate_card FOREIGN KEY (rate_card_id) REFERENCES rate_cards(rate_card_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
