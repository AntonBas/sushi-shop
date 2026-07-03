CREATE TABLE payments
(
    id                BIGSERIAL PRIMARY KEY,
    stripe_session_id VARCHAR(255)   NOT NULL UNIQUE,
    order_id          BIGINT         NOT NULL,
    amount            DECIMAL(10, 2) NOT NULL,
    status            VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    created_at        TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP,
    created_by        VARCHAR(255),
    updated_by        VARCHAR(255),
    CONSTRAINT fk_payment_order FOREIGN KEY (order_id) REFERENCES orders (id)
);