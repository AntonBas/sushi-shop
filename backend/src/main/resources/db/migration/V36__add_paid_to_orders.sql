ALTER TABLE orders
    ADD COLUMN paid BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE orders o
SET paid = TRUE
WHERE EXISTS (SELECT 1 FROM payments p WHERE p.order_id = o.id AND p.status = 'PAID');
