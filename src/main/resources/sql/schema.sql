CREATE TABLE `users` (
  `id` BIGSERIAL,
  `email` VARCHAR(300) NOT NULL UNIQUE,
  `username` VARCHAR(255) NOT NULL,
  `phone` VARCHAR(14),
  `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
  `created_at` TIMESTAMP NOT NULL DEFAULT NOW(),
  `updated_at` TIMESTAMP NOT NULL DEFAULT NOW(),
  `password` VARCHAR(500) NOT NULL,
  `userrole` VARCHAR(50) NOT NULL DEFAULT 'customer',
  PRIMARY KEY (`id`)
);

CREATE TABLE `orders` (
  `order_id` BIGSERIAL NOT NULL,
  `user_id` BIGINT NOT NULL,
  `total_amount` DECIMAL(12,2) NOT NULL DEFAULT 0 CHECK(discount_amount >=0),
  `tax_amount` DECIMAL(12,2) NOT NULL DEFAULT 0 CHECK(tax_amount >=0),
  `created_at` TIMESTAMP NOT NULL DEFAULT NOW(),
  `updated_at` TIMESTAMP NOT NULL DEFAULT NOW(),
  `status ` VARCHAR(50) NOT NULL CHCECK(status in ('processing','delivered','cancelled')) DEFAULT 'processing',
  PRIMARY KEY (`order_id`),
  FOREIGN KEY (`user_id`)
      REFERENCES `users`(`id`)
);

CREATE TABLE `order_items` (
  `id` BIGSERIAL ,
  `order_id` BIGINT NOT NULL ON DELETE CASCADE,
  `quantity` INTEGER NOT NULL CHECK(quantity > 0),
  `unit_price` DECIMAL(12,2) NOT NULL CHECK(unit_price >=0),
  `discount_amount` DECIMAL(12,2) NOT NULL CHECK(discount_amount >=0),
  `tax_amount` DECIMAL(12,2) NOT NULL CHECK(tax_amount >=0),
  PRIMARY KEY (`id`),
  FOREIGN KEY (`order_id`)
      REFERENCES `orders`(`order_id`)
);

CREATE TABLE `products` (
  `id` BIGSERIAL,
  `name` VARCHAR(200) NOT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT NOW(),
  `updated_at` TIMESTAMP NOT NULL DEFAULT NOW(),
  `price` DECIMAL(12,2) NOT NULL DEFAULT 0.0 ,
  `category_id` BIGINT NOT NULL,
  `stock_quantity` BIGINT NOT NULL CHECK(stock_quantity >=0) DEFAULT 0,
  PRIMARY KEY (`id`)
);

CREATE TABLE `inventory` (
  id` BIGSERIAL,
  `product_id` BIGINT NOT NULL,
  `quantity_in_stock` INT NOT NULL CHECK(quantity_in_stock >=0) DEFAULT 0,
  `quantity_in_reserve` INT NOT NULL CHECK(quantity_in_reserve >=0)  DEFAULT 0,
  `stock_status` VARCHAR(255) NOT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT NOW(),
  `updated_at` TIMESTAMP NOT NULL DEFAULT NOW(),
  PRIMARY KEY (`id`)
);

CREATE TABLE `categories` (
  `id` BIGSERIAL,
  `name` VARCHAR(255) NOT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT NOW(),
    `updated_at` TIMESTAMP NOT NULL DEFAULT NOW(),
  PRIMARY KEY (`id`)
);

CREATE TABLE `reviews` (
  `id` BIGSERIAL,
  `user_id` INT NOT NULL,
  `product_id` INT NOT NULL,
  `stars` INT NOT NULL DEFAULT 0 CHECK(stars <=10),
  `description` TEXT,
  PRIMARY KEY (`id`)
);

CREATE TABLE `order_address` (
  `id` BIGSERIAL,
  `order_id` BIGINT NOT NULL,
  `city` VARCHAR(255),
  `region` VARCHAR(255),
  `zip_code` VARCHAR(50),
  `user_id` BIGINT,
  PRIMARY KEY (`id`)
);
