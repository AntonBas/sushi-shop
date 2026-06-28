CREATE TABLE promotions
(
    id               BIGSERIAL PRIMARY KEY,
    title            VARCHAR(50)  NOT NULL,
    description      VARCHAR(250),
    discount_percent DECIMAL(5, 2) NOT NULL,
    start_date       TIMESTAMP     NOT NULL,
    end_date         TIMESTAMP     NOT NULL,
    active           BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP,
    created_by       VARCHAR(255),
    updated_by       VARCHAR(255)
);

CREATE TABLE promotion_products
(
    promotion_id BIGINT NOT NULL,
    product_id   BIGINT NOT NULL,
    PRIMARY KEY (promotion_id, product_id),
    CONSTRAINT fk_promotion FOREIGN KEY (promotion_id) REFERENCES promotions (id),
    CONSTRAINT fk_product FOREIGN KEY (product_id) REFERENCES products (id)
);