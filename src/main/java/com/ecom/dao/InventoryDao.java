package com.ecom.dao;

import com.ecom.exceptions.InsufficientInventoryException;
import com.ecom.utils.DatabaseUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Inventory DAO that operates on inventory.quantity_in_stock as the source of truth.
 */
public class InventoryDao {

    public InventoryDao() {
        // stateless, uses DatabaseUtils per-call
    }

    /**
     * Get current stock quantity for a product. Prefer inventory.quantity_in_stock, fall back to products.stock_quantity.
     */
    public int getStock(int productId) throws SQLException {
        String sql = "SELECT COALESCE(i.quantity_in_stock, p.stock_quantity, 0) AS available FROM products p LEFT JOIN inventory i ON p.id = i.product_id WHERE p.id = ?";
        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("available");
                return 0;
            }
        }
    }

    /**
     * Atomically decrease inventory.quantity_in_stock by qty if available; otherwise throws InsufficientInventoryException.
     */
    public void decreaseStock(int productId, int qty) throws SQLException, InsufficientInventoryException {
        if (qty <= 0) return;
        // Try to update inventory row first
        String sql = "UPDATE inventory SET quantity_in_stock = quantity_in_stock - ? WHERE id = ? AND quantity_in_stock >= ?";
        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, qty);
            ps.setInt(2, productId);
            ps.setInt(3, qty);
            int updated = ps.executeUpdate();
            if (updated > 0) return; // success
        }
        // Fallback: try to decrease products.stock_quantity if inventory row missing or didn't have enough
        String sql2 = "UPDATE products SET stock_quantity = stock_quantity - ? WHERE id = ? AND stock_quantity >= ?";
        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql2)) {
            ps.setInt(1, qty);
            ps.setInt(2, productId);
            ps.setInt(3, qty);
            int updated = ps.executeUpdate();
            if (updated > 0) return;
        }
        int available = getStock(productId);
        throw new InsufficientInventoryException(productId, qty, available);
    }

    /**
     * Increase inventory.quantity_in_stock by qty (create inventory row if needed).
     */
    public void increaseStock(int productId, int qty) throws SQLException {
        if (qty <= 0) return;
        // Use Postgres upsert to insert or update inventory row
        String upsert = "INSERT INTO inventory (product_id, quantity_in_stock) VALUES (?, ?) ON CONFLICT (product_id) DO UPDATE SET quantity_in_stock = inventory.quantity_in_stock + EXCLUDED.quantity_in_stock";
        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(upsert)) {
            ps.setInt(1, productId);
            ps.setInt(2, qty);
            ps.executeUpdate();
        }
    }

    /**
     * Set absolute inventory quantity for a product (admin operation).
     */
    public void setStock(int productId, int qty) throws SQLException {
        String upsert = "INSERT INTO inventory (product_id, quantity_in_stock) VALUES (?, ?) ON CONFLICT (product_id) DO UPDATE SET quantity_in_stock = EXCLUDED.quantity_in_stock";
        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(upsert)) {
            ps.setInt(1, productId);
            ps.setInt(2, qty);
            ps.executeUpdate();
        }
    }
}
