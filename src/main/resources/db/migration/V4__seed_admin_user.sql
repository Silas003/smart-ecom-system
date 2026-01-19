-- V4: Seed admin user with hashed password using pgcrypto
-- Requires the pgcrypto extension (CREATE EXTENSION IF NOT EXISTS pgcrypto)

CREATE EXTENSION IF NOT EXISTS pgcrypto;

INSERT INTO users (username, phone, email, password, userrole)
SELECT 'admin', '+10000000000', 'admin@example.com', crypt('adminpw', gen_salt('bf', 10)), 'admin'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@example.com');

ANALYZE users;
