-- Drop unused tables to simplify codebase for demo
-- These tables were removed from code but migrations must not be deleted (Flyway best practice)
-- Author: Code Simplification
-- Date: 2025-12-15

-- Drop Review system tables
DROP TABLE IF EXISTS review_helpfulness;
DROP TABLE IF EXISTS review_photos;
DROP TABLE IF EXISTS rating_summaries;
DROP TABLE IF EXISTS review_reports;
DROP TABLE IF EXISTS review_responses;
DROP TABLE IF EXISTS reviews;

-- Drop Customer features tables
DROP TABLE IF EXISTS customer_favorite_transports;
DROP TABLE IF EXISTS customer_saved_items;

-- Drop Audit and tracking tables
DROP TABLE IF EXISTS audit_logs;
DROP TABLE IF EXISTS login_attempts;
DROP TABLE IF EXISTS outbox_messages;

-- Drop Analytics/Export tables
DROP TABLE IF EXISTS transport_data_export_jobs;

-- Drop Price history table
DROP TABLE IF EXISTS price_history;

-- Note: Transport settings and admin settings tables are kept as they may be used in dashboard
