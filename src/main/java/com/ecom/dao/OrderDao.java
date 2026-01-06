package com.ecom.dao;
import java.sql.*;
import java.util.Map;

public class OrderDao {
    
  /**
   * Performs a transactional order placement. 1. Inserts Order 2. Inserts OrderItems 3. Updates
   * Product Stock Rolls back if any step fails (e.g., insufficient stock).
   */
  public boolean placeOrder(int userId, Map<Product, Integer> cartItems) throws SQLException {
    Connection conn = null;
    PreparedStatement orderStmt = null;
    PreparedStatement itemStmt = null;
    PreparedStatement stockStmt = null;
    ResultSet generatedKeys = null;

    try {
      conn = DatabaseConnection.getInstance().getConnection();
      // 1. Start Transaction
      conn.setAutoCommit(false);

      // Calculate Total
      double totalAmount = 0;
      for (Map.Entry<Product, Integer> entry : cartItems.entrySet()) {
        totalAmount += entry.getKey().getPrice() * entry.getValue();
      }

      // 2. Insert Order
      String insertOrderSQL =
          "INSERT INTO orders (user_id, order_date, total_amount) VALUES (?, NOW(), ?)";
      orderStmt = conn.prepareStatement(insertOrderSQL, Statement.RETURN_GENERATED_KEYS);
      orderStmt.setInt(1, userId);
      orderStmt.setDouble(2, totalAmount);
      int affectedRows = orderStmt.executeUpdate();

      if (affectedRows == 0) {
        throw new SQLException("Creating order failed, no rows affected.");
      }

      generatedKeys = orderStmt.getGeneratedKeys();
      int orderId;
      if (generatedKeys.next()) {
        orderId = generatedKeys.getInt(1);
      } else {
        throw new SQLException("Creating order failed, no ID obtained.");
      }

      // 3. Insert Items & Update Stock
      String insertItemSQL =
          "INSERT INTO order_items (order_id, product_id, quantity, price_at_purchase) VALUES (?,"
              + " ?, ?, ?)";
      // This query ensures we don't sell more than we have
      String updateStockSQL =
          "UPDATE products SET stock_quantity = stock_quantity - ? WHERE product_id = ? AND"
              + " stock_quantity >= ?";

      itemStmt = conn.prepareStatement(insertItemSQL);
      stockStmt = conn.prepareStatement(updateStockSQL);

      for (Map.Entry<Product, Integer> entry : cartItems.entrySet()) {
        Product product = entry.getKey();
        int quantity = entry.getValue();

        // Add to Batch: Insert Item
        itemStmt.setInt(1, orderId);
        itemStmt.setInt(2, product.getProductId());
        itemStmt.setInt(3, quantity);
        itemStmt.setDouble(4, product.getPrice());
        itemStmt.addBatch();

        // Update Stock (Immediate execution to check constraints)
        stockStmt.setInt(1, quantity);
        stockStmt.setInt(2, product.getProductId());
        stockStmt.setInt(3, quantity); // Check if stock >= quantity
        int stockRows = stockStmt.executeUpdate();

        if (stockRows == 0) {
          throw new SQLException("Insufficient stock for product: " + product.getName());
        }
      }

      itemStmt.executeBatch();

      // 4. Commit Transaction
      conn.commit();
      System.out.println("Transaction Committed Successfully. Order ID: " + orderId);
      return true;

    } catch (SQLException e) {
      if (conn != null) {
        try {
          System.err.println("Transaction failed. Rolling back.");
          conn.rollback();
        } catch (SQLException ex) {
          ex.printStackTrace();
        }
      }
      throw e; // Re-throw to notify caller
    } finally {
      // 5. Reset AutoCommit and Close Resources
      if (conn != null) conn.setAutoCommit(true);
      if (generatedKeys != null) generatedKeys.close();
      if (orderStmt != null) orderStmt.close();
      if (itemStmt != null) itemStmt.close();
      if (stockStmt != null) stockStmt.close();
    }
  }
}
