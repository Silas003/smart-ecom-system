-- Minimal test schema for H2
CREATE TABLE users (
  id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(255),
  email VARCHAR(255)
);

CREATE TABLE categories (
  category_id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255)
);

CREATE TABLE products (
  product_id INT AUTO_INCREMENT PRIMARY KEY,
  category_id INT,
  name VARCHAR(255),
  price DECIMAL(12,2),
  stock_quantity INT
);

CREATE TABLE inventory (
  id INT AUTO_INCREMENT PRIMARY KEY,
  product_id INT UNIQUE,
  quantity_in_stock INT
);

CREATE TABLE orders (
  order_id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  status VARCHAR(50),
  total_amount DECIMAL(14,2)
);

CREATE TABLE order_items (
  id INT AUTO_INCREMENT PRIMARY KEY,
  order_id INT,
  product_id INT,
  quantity INT,
  unit_price DECIMAL(12,2)
);

CREATE TABLE order_address (
  id INT AUTO_INCREMENT PRIMARY KEY,
  order_id INT,
  city VARCHAR(255),
  region VARCHAR(255),
  zip_code VARCHAR(50),
  user_id INT
);

INSERT INTO users (username, email) VALUES ('jdoe','jdoe@example.com');
INSERT INTO products (category_id, name, price, stock_quantity) VALUES (1,'Sample Book',19.99,50);
INSERT INTO inventory (product_id, quantity_in_stock) VALUES (1,50);
