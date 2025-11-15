-- V20251113__add_declared_value_to_saved_items.sql
-- Add declared value field for insurance and liability purposes

ALTER TABLE customer_saved_items
ADD COLUMN declared_value_vnd DECIMAL(15,2) COMMENT 'Giá trị khai báo (VND) cho mục đích bảo hiểm';

-- Add index for filtering high-value items
CREATE INDEX idx_declared_value ON customer_saved_items(declared_value_vnd DESC);
