CREATE TABLE IF NOT EXISTS product_images (
    id BIGSERIAL PRIMARY KEY,
    url VARCHAR(500) NOT NULL,
    sort_order INTEGER NOT NULL DEFAULT 0,
    product_id BIGINT NOT NULL,
    CONSTRAINT fk_product_images_product FOREIGN KEY (product_id) REFERENCES products(id)
    );

ALTER TABLE products DROP COLUMN IF EXISTS image_url;
ALTER TABLE products RENAME COLUMN is_available TO available;