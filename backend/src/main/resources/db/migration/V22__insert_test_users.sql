INSERT INTO users (email, password, name, phone, user_role, email_verified, token_version, created_at, updated_at)
VALUES ('user@test.com', '$2a$12$yyOs2pPT/SoBML2u99MKfewjp7Lhj1HrI/v5m7DFaC/SoMZetBJ0W', 'Test User', '+380991234567',
        'USER', true, 0, NOW(), NOW()),
       ('admin@test.com', '$2a$12$hPwjJfuUU7oG8KLBxWh23ulaVS6PyX2w7YvnJK8gIOyMYaHk24bfq', 'Admin', '+380997654321',
        'ADMIN', true, 0, NOW(), NOW()),
       ('courier@test.com', '$2a$12$03vzvEtpaBM3Un4yjj0nweW9qMYVZrd2axRIatRRmTk5dLiL95bgW', 'Courier', '+380995554433',
        'COURIER', true, 0, NOW(),
        NOW());