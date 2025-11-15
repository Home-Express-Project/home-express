-- V20251114__add_brand_model_value_to_booking_items.sql
-- Add brand, model, declared value to booking items for better tracking

ALTER TABLE booking_items
ADD COLUMN brand VARCHAR(100) COMMENT 'Thương hiệu vật phẩm (Samsung, IKEA...)',
ADD COLUMN model VARCHAR(200) COMMENT 'Model vật phẩm (UN55TU7000, KALLAX...)',
ADD COLUMN declared_value_vnd DECIMAL(15,2) COMMENT 'Giá trị khai báo (VND) cho bảo hiểm';

-- Index for filtering by brand
CREATE INDEX idx_booking_items_brand ON booking_items(brand);

-- Index for high-value items
CREATE INDEX idx_booking_items_value ON booking_items(declared_value_vnd DESC);
