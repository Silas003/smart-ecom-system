-- V3: Seed sample data for development

INSERT INTO categories (name) VALUES ('Electronics') ON CONFLICT DO NOTHING;
INSERT INTO categories (name) VALUES ('Books') ON CONFLICT DO NOTHING;
INSERT INTO categories (name) VALUES ('Home') ON CONFLICT DO NOTHING;

INSERT INTO users (username, phone, email, password, userrole) VALUES ('admin', '+10000000000', 'admin@example.com', 'adminpw', 'admin') ON CONFLICT DO NOTHING;
INSERT INTO users (username, phone, email, password, userrole) VALUES ('jdoe', '+10000000001', 'jdoe@example.com', 'password', 'customer') ON CONFLICT DO NOTHING;

INSERT INTO products (category_id, name, description, price, stock_quantity)
SELECT c.category_id, 'Sample Laptop', 'A test laptop', 999.99, 10 FROM categories c WHERE c.name = 'Electronics' ON CONFLICT DO NOTHING;

INSERT INTO products (category_id, name, description, price, stock_quantity)
SELECT c.category_id, 'Sample Book', 'A test book', 19.99, 50 FROM categories c WHERE c.name = 'Books' ON CONFLICT DO NOTHING;

-- ensure inventory rows
INSERT INTO inventory (product_id, quantity_in_stock)
SELECT p.product_id, p.stock_quantity FROM products p
ON CONFLICT (product_id) DO UPDATE SET quantity_in_stock = EXCLUDED.quantity_in_stock;

ANALYZE;
