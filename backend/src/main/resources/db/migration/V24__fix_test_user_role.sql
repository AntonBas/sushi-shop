UPDATE users
SET user_role = 'CUSTOMER'
WHERE email = 'user@test.com'
  AND user_role = 'USER';
