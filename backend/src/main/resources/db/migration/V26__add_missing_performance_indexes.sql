CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items(order_id);
CREATE INDEX IF NOT EXISTS idx_order_items_product_id ON order_items(product_id);

CREATE INDEX IF NOT EXISTS idx_review_user_id ON reviews(user_id);

CREATE INDEX IF NOT EXISTS idx_review_replies_review_id ON review_replies(review_id);
CREATE INDEX IF NOT EXISTS idx_review_replies_user_id ON review_replies(user_id);

CREATE INDEX IF NOT EXISTS idx_promotion_products_product_id ON promotion_products(product_id);

CREATE INDEX IF NOT EXISTS idx_audit_log_entity ON audit_logs(entity_name, entity_id);
CREATE INDEX IF NOT EXISTS idx_audit_log_performed_at ON audit_logs(performed_at);
