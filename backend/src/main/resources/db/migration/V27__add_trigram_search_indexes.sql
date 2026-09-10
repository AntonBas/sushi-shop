CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX IF NOT EXISTS idx_product_name_trgm ON products USING GIN (name gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_order_customer_name_trgm ON orders USING GIN (customer_name gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_order_phone_trgm ON orders USING GIN (phone gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_promotion_title_trgm ON promotions USING GIN (title gin_trgm_ops);
