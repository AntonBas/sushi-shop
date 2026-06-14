CREATE TABLE users
(
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    email      VARCHAR(255) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    phone      VARCHAR(20),
    user_role  VARCHAR(20)  NOT NULL DEFAULT 'CUSTOMER',
    created_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE TABLE products
(
    id           BIGSERIAL PRIMARY KEY,
    name         VARCHAR(255)   NOT NULL,
    description  VARCHAR(500),
    price        DECIMAL(10, 2) NOT NULL,
    category     VARCHAR(20)    NOT NULL,
    image_url    VARCHAR(500),
    is_available BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP,
    created_by   VARCHAR(255),
    updated_by   VARCHAR(255)
);

CREATE TABLE orders
(
    id            BIGSERIAL PRIMARY KEY,
    customer_name VARCHAR(255)   NOT NULL,
    phone         VARCHAR(20)    NOT NULL,
    address       VARCHAR(255)   NOT NULL,
    status        VARCHAR(20)    NOT NULL DEFAULT 'NEW',
    total_amount  DECIMAL(10, 2) NOT NULL,
    created_at    TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP,
    created_by    VARCHAR(255),
    updated_by    VARCHAR(255)
);

CREATE TABLE order_items
(
    id         BIGSERIAL PRIMARY KEY,
    product_id BIGINT         NOT NULL,
    order_id   BIGINT         NOT NULL,
    quantity   INTEGER        NOT NULL,
    price      DECIMAL(10, 2) NOT NULL,
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders (id)
);