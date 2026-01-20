-- V5: Create order_address table

CREATE TABLE IF NOT EXISTS order_address (
  id SERIAL PRIMARY KEY,
  order_id INT NOT NULL REFERENCES orders(order_id) ON DELETE CASCADE,
  city VARCHAR(255),
  region VARCHAR(255),
  zip_code VARCHAR(50),
  user_id INT REFERENCES users(id) ON DELETE SET NULL,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_order_address_order_id ON order_address(order_id);
CREATE INDEX IF NOT EXISTS idx_order_address_user_id ON order_address(user_id);
