UPDATE users u
SET email = LOWER(u.email)
WHERE u.email <> LOWER(u.email)
  AND NOT EXISTS (SELECT 1 FROM users other WHERE other.id <> u.id AND LOWER(other.email) = LOWER(u.email));

UPDATE users
SET pending_email = LOWER(pending_email)
WHERE pending_email IS NOT NULL
  AND pending_email <> LOWER(pending_email);
