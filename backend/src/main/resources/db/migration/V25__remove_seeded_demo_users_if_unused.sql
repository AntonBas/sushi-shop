DELETE FROM users u
WHERE u.email IN ('user@test.com', 'admin@test.com', 'courier@test.com')
  AND NOT EXISTS (SELECT 1 FROM orders o WHERE o.user_id = u.id)
  AND NOT EXISTS (SELECT 1 FROM tokens t WHERE t.user_id = u.id)
  AND NOT EXISTS (SELECT 1 FROM reviews r WHERE r.user_id = u.id)
  AND NOT EXISTS (SELECT 1 FROM review_replies rr WHERE rr.user_id = u.id);
