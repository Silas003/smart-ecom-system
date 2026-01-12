package com.ecom.dao;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.ecom.models.Product;
import com.ecom.models.Order;
import com.ecom.models.OrderItem;
import com.ecom.utils.DatabaseUtils;
import com.ecom.exceptions.DaoException;
import com.ecom.exceptions.InsufficientInventoryException;

public class OrderDao {
    
  /**
   * Performs a transactional order placement. 1. Inserts Order 2. Inserts OrderItems 3. Updates
   * Product Stock Rolls back if any step fails (e.g., insufficient stock).
   */
  public boolean placeOrder(int userId, Map<Product, Integer> cartItems) throws DaoException, InsufficientInventoryException {
    Connection conn = null;
    PreparedStatement orderStmt = null;
    PreparedStatement itemStmt = null;
    PreparedStatement stockStmt = null;
    ResultSet generatedKeys = null;

    try {
      conn = DatabaseUtils.getConnection();

      conn.setAutoCommit(false);


      double totalAmount = 0;
      for (Map.Entry<Product, Integer> entry : cartItems.entrySet()) {
        totalAmount += entry.getKey().getPrice() * entry.getValue();
      }


      String insertOrderSQL =
          "INSERT INTO orders (user_id, total_amount) VALUES (?, NOW(), ?)";
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
          throw new InsufficientInventoryException(product.getProductId(), quantity, 0);
        }
      }

      itemStmt.executeBatch();

      // 4. Commit Transaction
      conn.commit();
      System.out.println("Transaction Committed Successfully. Order ID: " + orderId);
      return true;

    } catch (InsufficientInventoryException e) {
      if (conn != null) {
        try {
          System.err.println("Transaction failed due to insufficient inventory. Rolling back.");
          conn.rollback();
        } catch (SQLException ex) {
          ex.printStackTrace();
        }
      }
      throw e;
    } catch (SQLException e) {
      if (conn != null) {
        try {
          System.err.println("Transaction failed. Rolling back.");
          conn.rollback();
        } catch (SQLException ex) {
          ex.printStackTrace();
        }
      }
      throw new DaoException("Database error while placing order", e);
    } finally {
      // 5. Reset AutoCommit and Close Resources
      try {
        if (conn != null) conn.setAutoCommit(true);
        if (generatedKeys != null) generatedKeys.close();
        if (orderStmt != null) orderStmt.close();
        if (itemStmt != null) itemStmt.close();
        if (stockStmt != null) stockStmt.close();
      } catch (SQLException e) {
        throw new DaoException("Failed to clean up resources", e);
      }
    }
  }

  public List<Order> findByUserId(int userId) throws SQLException {
    List<Order> orders = new ArrayList<>();
    String sql = "SELECT order_id, user_id, created_at, total_amount FROM orders WHERE user_id = ? ORDER BY created_at DESC";
    try (Connection conn = DatabaseUtils.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, userId);
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          orders.add(mapResultSetToOrder(rs));
        }
      }
    }
      return orders;
  }

  public Order findById(int orderId) throws SQLException {
    String sql = "SELECT order_id, user_id, created_at, total_amount FROM orders WHERE order_id = ?";
    try (Connection conn = DatabaseUtils.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, orderId);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          return mapResultSetToOrder(rs);
        }
      }
    }
      return null;
  }

  public List<Order> findAll() throws SQLException {
    List<Order> orders = new ArrayList<>();
    String sql = "SELECT order_id, user_id, created_at, total_amount FROM orders ORDER BY created_at DESC";
    try (Connection conn = DatabaseUtils.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql)) {
      while (rs.next()) {
        orders.add(mapResultSetToOrder(rs));
      }
    }
      return orders;
  }

  public List<OrderItem> findOrderItemsByOrderId(int orderId) throws SQLException {
    List<OrderItem> items = new ArrayList<>();
    String sql = "SELECT id, order_id, product_id, quantity, price_at_purchase FROM order_items WHERE order_id = ?";
    try (Connection conn = DatabaseUtils.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, orderId);
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          items.add(mapResultSetToOrderItem(rs));
        }
      }
    }
      return items;
  }

  private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
    Order order = new Order();
    order.setOrderId(rs.getInt("order_id"));
    order.setUserId(rs.getInt("user_id"));
    order.setOrderDate(rs.getTimestamp("created_at").toLocalDateTime());
    order.setTotalAmount(rs.getDouble("total_amount"));
    return order;
  }

  private OrderItem mapResultSetToOrderItem(ResultSet rs) throws SQLException {
    OrderItem item = new OrderItem(
        rs.getInt("product_id"),
        rs.getInt("quantity"),
        rs.getDouble("price_at_purchase")
    );
    item.setId(rs.getInt("id"));
    item.setOrderId(rs.getInt("order_id"));
    return item;
  }
}
