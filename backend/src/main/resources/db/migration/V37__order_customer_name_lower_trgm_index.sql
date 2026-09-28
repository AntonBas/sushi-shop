DROP INDEX IF EXISTS idx_order_customer_name_trgm;
CREATE INDEX IF NOT EXISTS idx_order_customer_name_lower_trgm ON orders USING GIN (lower(customer_name) gin_trgm_ops);
