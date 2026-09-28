DROP INDEX IF EXISTS idx_product_name_trgm;
CREATE INDEX IF NOT EXISTS idx_product_name_lower_trgm ON products USING GIN (lower(name) gin_trgm_ops);

DROP INDEX IF EXISTS idx_promotion_title_trgm;
CREATE INDEX IF NOT EXISTS idx_promotion_title_lower_trgm ON promotions USING GIN (lower(title) gin_trgm_ops);
