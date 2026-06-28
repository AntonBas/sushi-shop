CREATE TABLE audit_logs
(
    id           BIGSERIAL PRIMARY KEY,
    action       VARCHAR(50)  NOT NULL,
    entity_name  VARCHAR(100) NOT NULL,
    entity_id    BIGINT,
    details      TEXT,
    performed_by VARCHAR(100) NOT NULL,
    performed_at TIMESTAMP    NOT NULL
);