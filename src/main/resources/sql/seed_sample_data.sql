-- Sample seed data for Smart E-Commerce System
-- Run after schema_postgres.sql has been applied

-- Create sample categories
INSERT INTO categories (name) VALUES ('Electronics') ON CONFLICT DO NOTHING;
INSERT INTO categories (name) VALUES ('Books') ON CONFLICT DO NOTHING;
INSERT INTO categories (name) VALUES ('Home') ON CONFLICT DO NOTHING;

-- Create sample users
INSERT INTO users (username, phone, email, password, userrole) VALUES ('admin', '+10000000000', 'admin@example.com', 'adminpw', 'admin') ON CONFLICT DO NOTHING;
INSERT INTO users (username, phone, email, password, userrole) VALUES ('jdoe', '+10000000001', 'jdoe@example.com', 'password', 'customer') ON CONFLICT DO NOTHING;

-- Create sample products
INSERT INTO products (category_id, name, description, price, stock_quantity) VALUES (
  (SELECT category_id FROM categories WHERE name='Electronics'),
  'Sample Laptop', 'A test laptop', 999.99, 10
) ON CONFLICT DO NOTHING;

INSERT INTO products (category_id, name, description, price, stock_quantity) VALUES (
  (SELECT category_id FROM categories WHERE name='Books'),
  'Sample Book', 'A test book', 19.99, 50
) ON CONFLICT DO NOTHING;

-- Sync inventory table for created products
INSERT INTO inventory (product_id, quantity_in_stock)
SELECT _id, stock_quantity FROM products
ON CONFLICT (product_id) DO UPDATE SET quantity_in_stock = EXCLUDED.quantity_in_stock;

-- Create a sample order for jdoe
WITH u AS (SELECT id AS user_id FROM users WHERE email='jdoe@example.com'),
p AS (SELECT product_id, price FROM products WHERE name='Sample Book')
INSERT INTO orders (user_id, status, total_amount)
SELECT u.user_id, 'completed', p.price FROM u, p
RETURNING order_id INTO TEMP TABLE temp_order;

-- Insert order_items (if the previous insert succeeded)
INSERT INTO order_items (order_id, product_id, quantity, unit_price)
SELECT to_char((SELECT order_id FROM orders WHERE user_id = (SELECT id FROM users WHERE email='jdoe@example.com') ORDER BY created_at DESC LIMIT 1),'999999')::int, (SELECT product_id FROM products WHERE name='Sample Book'), 1, (SELECT price FROM products WHERE name='Sample Book');

-- Note: simple seed; for robust test data consider a dedicated seeder script in Java or SQL with checks.
