ALTER TABLE users
    ADD COLUMN IF NOT EXISTS last_password_reset_sent_at TIMESTAMP;
