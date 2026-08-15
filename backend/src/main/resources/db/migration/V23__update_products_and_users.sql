UPDATE products
SET weight = 1
WHERE weight IS NULL
   OR weight = 0;

ALTER TABLE products
    ALTER COLUMN weight SET NOT NULL;

ALTER TABLE products
    ADD CONSTRAINT chk_products_weight_positive CHECK (weight > 0);

ALTER TABLE products
    ADD CONSTRAINT chk_products_pieces_positive CHECK (pieces IS NULL OR pieces > 0);

ALTER TABLE users
    ALTER COLUMN phone DROP NOT NULL;

CREATE INDEX IF NOT EXISTS idx_product_name ON products(name);

ALTER TABLE orders
    ALTER COLUMN customer_name SET NOT NULL;
ALTER TABLE orders
    ALTER COLUMN phone SET NOT NULL;
ALTER TABLE orders
    ALTER COLUMN payment_method SET NOT NULL;
ALTER TABLE orders
    ALTER COLUMN delivery_method SET NOT NULL;
ALTER TABLE orders
    ALTER COLUMN total_amount SET NOT NULL;

ALTER TABLE orders
    ADD CONSTRAINT chk_orders_total_amount_positive CHECK (total_amount > 0);

ALTER TABLE order_items
    ADD CONSTRAINT chk_order_items_unit_price_positive CHECK (unit_price > 0);
ALTER TABLE order_items
    ADD CONSTRAINT chk_order_items_quantity_positive CHECK (quantity > 0);
ALTER TABLE order_items
    ADD CONSTRAINT chk_order_items_subtotal_positive CHECK (subtotal > 0);