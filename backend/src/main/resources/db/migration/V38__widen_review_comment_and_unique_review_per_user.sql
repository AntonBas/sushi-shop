ALTER TABLE reviews ALTER COLUMN comment TYPE VARCHAR(250);

CREATE UNIQUE INDEX IF NOT EXISTS uk_reviews_user_product ON reviews (user_id, product_id);
