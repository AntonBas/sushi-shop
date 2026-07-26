CREATE TABLE review_replies
(
    id         BIGSERIAL PRIMARY KEY,
    review_id  BIGINT       NOT NULL REFERENCES reviews (id),
    user_id    BIGINT       NOT NULL REFERENCES users (id),
    message    VARCHAR(100) NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_at TIMESTAMP,
    updated_by VARCHAR(100)
);