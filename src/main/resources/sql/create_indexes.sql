-- ============================================================================
-- Performance Optimization: Database Indexes
-- ============================================================================
-- This script creates indexes on frequently queried columns to improve
-- query performance. Indexes should be created after the initial schema
-- is set up and sample data is loaded.
--
-- Execution Instructions:
-- 1. Connect to your PostgreSQL database: psql -U postgres -d smartEcom
-- 2. Run this script: \i src/main/resources/sql/create_indexes.sql
--    OR copy and paste the contents into your database client
-- ============================================================================

-- Index on products.name for search operations (case-insensitive search)
-- This significantly improves performance of LIKE queries on product names
CREATE INDEX IF NOT EXISTS idx_products_name_lower 
ON products (LOWER(name));

-- Unique index on lower(name) to prevent duplicate product names (case-insensitive)
CREATE UNIQUE INDEX IF NOT EXISTS ux_products_name_lower ON products (LOWER(name));

-- Index on products.category_id for filtering products by category
-- Improves JOIN and WHERE clause performance when filtering by category
CREATE INDEX IF NOT EXISTS idx_products_category_id 
ON products (category_id);

-- Index on products.price for sorting operations
-- Improves ORDER BY price queries
CREATE INDEX IF NOT EXISTS idx_products_price 
ON products (price);

-- Composite index on products for common search patterns
-- Optimizes queries that filter by category and sort by price
CREATE INDEX IF NOT EXISTS idx_products_category_price 
ON products (category_id, price);

-- Index on orders.user_id for retrieving user orders
-- Critical for ORDER BY created_at DESC queries per user
CREATE INDEX IF NOT EXISTS idx_orders_user_id 
ON orders (user_id);

-- Index on orders.created_at for date-based queries and sorting
-- Improves performance of time-series queries and recent orders
CREATE INDEX IF NOT EXISTS idx_orders_created_at 
ON orders (created_at DESC);

-- Composite index on orders for user-specific date queries
-- Optimizes queries that filter by user and sort by date
CREATE INDEX IF NOT EXISTS idx_orders_user_created 
ON orders (user_id, created_at DESC);

-- Index on order_items.order_id for retrieving order details
-- Improves JOIN performance when loading order items
CREATE INDEX IF NOT EXISTS idx_order_items_order_id 
ON order_items (order_id);

-- Index on order_items.product_id for product order history
-- Useful for queries showing which products are in which orders
CREATE INDEX IF NOT EXISTS idx_order_items_product_id 
ON order_items (product_id);

-- Index on reviews.product_id for product review queries
-- Improves performance when loading reviews for a product
CREATE INDEX IF NOT EXISTS idx_reviews_product_id 
ON reviews (product_id);

-- Index on reviews.user_id for user review queries
-- Improves performance when loading reviews by a specific user
CREATE INDEX IF NOT EXISTS idx_reviews_user_id 
ON reviews (user_id);

-- Index on users.email for login and user lookup operations
-- Critical for authentication queries
CREATE INDEX IF NOT EXISTS idx_users_email 
ON users (email);

-- Index on categories.name for category search operations
CREATE INDEX IF NOT EXISTS idx_categories_name 
ON categories (name);

-- ============================================================================
-- Performance Analysis Queries
-- ============================================================================
-- Use these queries to analyze index usage and performance:

-- Check index usage statistics
-- SELECT schemaname, tablename, indexname, idx_scan, idx_tup_read, idx_tup_fetch
-- FROM pg_stat_user_indexes
-- WHERE schemaname = 'public'
-- ORDER BY idx_scan DESC;

-- Check table sizes and index sizes
-- SELECT 
--     schemaname,
--     tablename,
--     pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS total_size,
--     pg_size_pretty(pg_indexes_size(schemaname||'.'||tablename)) AS index_size
-- FROM pg_tables
-- WHERE schemaname = 'public'
-- ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;

-- Explain plan for a search query (use this to verify index usage)
-- EXPLAIN ANALYZE SELECT * FROM products WHERE LOWER(name) LIKE '%laptop%' ORDER BY price;

-- ============================================================================
-- Notes:
-- ============================================================================
-- 1. Indexes improve SELECT query performance but slightly slow down
--    INSERT, UPDATE, and DELETE operations. This is usually acceptable
--    for read-heavy e-commerce applications.
--
-- 2. The LOWER(name) index uses a functional index to support
--    case-insensitive searches efficiently.
--
-- 3. Composite indexes are useful when queries filter by multiple columns.
--
-- 4. Monitor index usage with pg_stat_user_indexes to identify unused
--    indexes that can be dropped.
--
-- 5. After creating indexes, run ANALYZE on tables to update statistics:
--    ANALYZE products;
--    ANALYZE orders;
--    ANALYZE order_items;
--    ANALYZE reviews;
-- ============================================================================
