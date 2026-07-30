ALTER TABLE products ADD COLUMN IF NOT EXISTS category VARCHAR(255);
CREATE INDEX IF NOT EXISTS idx_product_category ON products(category);

ALTER TABLE promotions ALTER COLUMN discount_percent TYPE NUMERIC(5,2);

ALTER TABLE promotion_products ADD CONSTRAINT uq_promotion_product UNIQUE (promotion_id, product_id);

CREATE TABLE IF NOT EXISTS payments (
    id BIGSERIAL PRIMARY KEY,
    stripe_session_id VARCHAR(500) NOT NULL UNIQUE,
    order_id BIGINT NOT NULL,
    amount NUMERIC(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT fk_payment_order FOREIGN KEY (order_id) REFERENCES orders(id)
    );

CREATE INDEX IF NOT EXISTS idx_payment_order_id ON payments(order_id);

CREATE INDEX IF NOT EXISTS idx_order_user_id ON orders(user_id);
CREATE INDEX IF NOT EXISTS idx_order_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_order_created_at ON orders(created_at);

ALTER TABLE orders DROP COLUMN IF EXISTS payment_status;

ALTER TABLE order_items RENAME COLUMN price TO subtotal;
ALTER TABLE order_items ADD COLUMN IF NOT EXISTS unit_price NUMERIC(10,2);
UPDATE order_items SET unit_price = subtotal / quantity WHERE unit_price IS NULL;
ALTER TABLE order_items ALTER COLUMN unit_price SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_review_product_id ON reviews(product_id);

ALTER TABLE review_replies ALTER COLUMN message TYPE VARCHAR(250);

CREATE INDEX IF NOT EXISTS idx_token_user_id ON tokens(user_id);

ALTER TABLE users ALTER COLUMN password TYPE VARCHAR(100);
ALTER TABLE users ALTER COLUMN email_verified SET NOT NULL;