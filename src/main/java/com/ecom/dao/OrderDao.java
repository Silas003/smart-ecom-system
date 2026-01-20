package com.ecom.dao;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.ecom.models.Product;
import com.ecom.models.Order;
import com.ecom.models.OrderItem;
import com.ecom.utils.DatabaseUtils;
import com.ecom.utils.QueryTimer;
import com.ecom.exceptions.DaoException;
import com.ecom.exceptions.InsufficientInventoryException;

/**
 * Data Access Object (DAO) for orders and related entities.
 * <p>
 * Provides methods to create orders, read orders and order items, and update order status.
 * Critical write operations are performed transactionally to ensure consistency between
 * orders, order items and inventory updates.
 */
public class OrderDao {
    
  /**
   * Performs a transactional order placement. 1. Inserts Order 2. Inserts OrderItems 3. Updates
   * Product Stock Rolls back if any step fails (e.g., insufficient stock).
   *
   * @param userId    the ID of the purchasing user
   * @param cartItems map of Product -> quantity
   * @return true if the order was placed successfully
   * @throws DaoException when a database error occurs
   * @throws InsufficientInventoryException when inventory is insufficient for any item
   */
  public boolean placeOrder(int userId, Map<Product, Integer> cartItems) throws DaoException, InsufficientInventoryException {
    Connection conn = null;
    PreparedStatement orderStmt = null;
    PreparedStatement itemStmt = null;
    PreparedStatement stockStmt = null;
    PreparedStatement addrStmt = null;
    ResultSet generatedKeys = null;

    try {
      conn = DatabaseUtils.getConnection();

      conn.setAutoCommit(false);


      double totalAmount = 0;
      for (Map.Entry<Product, Integer> entry : cartItems.entrySet()) {
        totalAmount += entry.getKey().getPrice() * entry.getValue();
      }


      String insertOrderSQL =
          "INSERT INTO orders (user_id, total_amount) VALUES (?, ?)";
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

      // Insert order address if table exists; method caller must supply address info via overloaded method
      // addrStmt will be set by overloaded method variant; kept here for resource cleanup

      // 3. Insert Items & Update Stock
      String insertItemSQL =
          "INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES (?,"
              + " ?, ?, ?)";
      // Try to update inventory table first, then fallback to products table
      String updateInventorySQL =
          "UPDATE inventory SET quantity_in_stock = quantity_in_stock - ? WHERE product_id = ? AND quantity_in_stock >= ?";
      String updateProductsSQL =
          "UPDATE products SET stock_quantity = stock_quantity - ? WHERE id = ? AND stock_quantity >= ?";

      itemStmt = conn.prepareStatement(insertItemSQL);
      PreparedStatement invStmt = conn.prepareStatement(updateInventorySQL);
      PreparedStatement prodStmt = conn.prepareStatement(updateProductsSQL);

      for (Map.Entry<Product, Integer> entry : cartItems.entrySet()) {
        Product product = entry.getKey();
        int quantity = entry.getValue();

        // Add to Batch: Insert Item
        itemStmt.setInt(1, orderId);
        itemStmt.setInt(2, product.getProductId());
        itemStmt.setInt(3, quantity);
        itemStmt.setDouble(4, product.getPrice());
        itemStmt.addBatch();

        // Update Inventory (try inventory first)
        invStmt.setInt(1, quantity);
        invStmt.setInt(2, product.getProductId());
        invStmt.setInt(3, quantity);
        int invRows = invStmt.executeUpdate();
        if (invRows == 0) {
          // fallback to products table update
          prodStmt.setInt(1, quantity);
          prodStmt.setInt(2, product.getProductId());
          prodStmt.setInt(3, quantity);
          int prodRows = prodStmt.executeUpdate();
          if (prodRows == 0) {
            throw new InsufficientInventoryException(product.getProductId(), quantity, 0);
          }
        }
      }

      itemStmt.executeBatch();

      // Note: there's an overloaded variant below (inserted by service) that will execute address insert

      // 4. Commit Transaction
      conn.commit();
      System.out.println("Transaction Committed Successfully. Order ID: " + orderId);
      return true;

    } catch (InsufficientInventoryException e) {
        e.getMessage();
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
        e.getMessage();
      if (conn != null) {
        try {
            e.getMessage();
            e.printStackTrace();
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
        if (addrStmt != null) addrStmt.close();
      } catch (SQLException e) {
        throw new DaoException("Failed to clean up resources", e);
      }
    }
  }

  // New overloaded placeOrder to accept shipping address and insert into order_address table within the same transaction
  /**
   * Place an order and persist a shipping address for the order within the same transaction.
   *
   * @param userId    the ID of the purchasing user
   * @param cartItems map of Product -> quantity
   * @param city      shipping city
   * @param region    shipping region/state
   * @param zipCode   shipping postal code
   * @param address   shipping address line
   * @return true if the order and address were persisted successfully
   * @throws DaoException when a database error occurs
   * @throws InsufficientInventoryException when inventory is insufficient for any item
   */
  public boolean placeOrder(int userId, Map<Product, Integer> cartItems, String city, String region, String zipCode,String address) throws DaoException, InsufficientInventoryException {
    Connection conn = null;
    PreparedStatement orderStmt = null;
    PreparedStatement itemStmt = null;
    PreparedStatement stockStmt = null;
    PreparedStatement addrStmt = null;
    ResultSet generatedKeys = null;

    try {
      conn = DatabaseUtils.getConnection();
      conn.setAutoCommit(false);

      double totalAmount = 0;
      for (Map.Entry<Product, Integer> entry : cartItems.entrySet()) {
        totalAmount += entry.getKey().getPrice() * entry.getValue();
      }

      String insertOrderSQL = "INSERT INTO orders (user_id, total_amount) VALUES (?, ?)";
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

      // Insert address for this order
      String insertAddrSQL = "INSERT INTO order_address (order_id, city, region, zip_code, user_id,address) VALUES (?, ?, ?, ?, ?,?)";
      addrStmt = conn.prepareStatement(insertAddrSQL);
      addrStmt.setInt(1, orderId);
      addrStmt.setString(2, city);
      addrStmt.setString(3, region);
      addrStmt.setString(4, zipCode);
      addrStmt.setInt(5, userId);
      addrStmt.setString(6, address);
      addrStmt.executeUpdate();

      // 3. Insert Items & Update Stock (same logic as other placeOrder)
      String insertItemSQL = "INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)";
      String updateInventorySQL = "UPDATE inventory SET quantity_in_stock = quantity_in_stock - ? WHERE product_id = ? AND quantity_in_stock >= ?";
      String updateProductsSQL = "UPDATE products SET stock_quantity = stock_quantity - ? WHERE id = ? AND stock_quantity >= ?";

      itemStmt = conn.prepareStatement(insertItemSQL);
      PreparedStatement invStmt = conn.prepareStatement(updateInventorySQL);
      PreparedStatement prodStmt = conn.prepareStatement(updateProductsSQL);

      for (Map.Entry<Product, Integer> entry : cartItems.entrySet()) {
        Product product = entry.getKey();
        int quantity = entry.getValue();

        itemStmt.setInt(1, orderId);
        itemStmt.setInt(2, product.getProductId());
        itemStmt.setInt(3, quantity);
        itemStmt.setDouble(4, product.getPrice());
        itemStmt.addBatch();

        invStmt.setInt(1, quantity);
        invStmt.setInt(2, product.getProductId());
        invStmt.setInt(3, quantity);
        int invRows = invStmt.executeUpdate();
        if (invRows == 0) {
          prodStmt.setInt(1, quantity);
          prodStmt.setInt(2, product.getProductId());
          prodStmt.setInt(3, quantity);
          int prodRows = prodStmt.executeUpdate();
          if (prodRows == 0) {
            throw new InsufficientInventoryException(product.getProductId(), quantity, 0);
          }
        }
      }

      itemStmt.executeBatch();

      conn.commit();
      System.out.println("Transaction Committed Successfully. Order ID: " + orderId);
      return true;
    } catch (InsufficientInventoryException e) {
      if (conn != null) {
        try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
      }
      throw e;
    } catch (SQLException e) {
      if (conn != null) {
        try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
      }
      throw new DaoException("Database error while placing order", e);
    } finally {
      try {
        if (conn != null) conn.setAutoCommit(true);
        if (generatedKeys != null) generatedKeys.close();
        if (orderStmt != null) orderStmt.close();
        if (itemStmt != null) itemStmt.close();
        if (stockStmt != null) stockStmt.close();
        if (addrStmt != null) addrStmt.close();
      } catch (SQLException e) {
        throw new DaoException("Failed to clean up resources", e);
      }
    }
  }

  public List<Order> findByUserId(int userId) throws SQLException {
    return QueryTimer.measure("order_findByUserId", () -> {
      List<Order> orders = new ArrayList<>();
      String sql = "SELECT order_id, user_id, created_at,status, total_amount FROM orders WHERE user_id = ? ORDER BY created_at DESC";
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
    });
  }

  public Order findById(int orderId) throws SQLException {
    String sql = "SELECT order_id, user_id,status, created_at, total_amount FROM orders WHERE order_id = ?";
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
    return QueryTimer.measure("order_findAll", () -> {
      List<Order> orders = new ArrayList<>();
      String sql = "SELECT order_id, user_id,status, created_at, total_amount FROM orders ORDER BY created_at DESC";
      try (Connection conn = DatabaseUtils.getConnection();
          Statement stmt = conn.createStatement();
          ResultSet rs = stmt.executeQuery(sql)) {
        while (rs.next()) {
          orders.add(mapResultSetToOrder(rs));
        }
      }
        return orders;
    });
  }

  /**
   * Find orders by status (e.g., processing, delivered, cancelled). Returns all orders when
   * status is null or empty.
   */
  public List<Order> findByStatus(String status) throws SQLException {
    return QueryTimer.measure("order_findByStatus", () -> {
      List<Order> orders = new ArrayList<>();
      String sql;
      if (status == null || status.trim().isEmpty() || "All Orders".equalsIgnoreCase(status)) {
        sql = "SELECT order_id, user_id, status, created_at, total_amount FROM orders ORDER BY created_at DESC";
        try (Connection conn = DatabaseUtils.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {
          while (rs.next()) {
            orders.add(mapResultSetToOrder(rs));
          }
        }
      } else {
        sql = "SELECT order_id, user_id, status, created_at, total_amount FROM orders WHERE status = ? ORDER BY created_at DESC";
        try (Connection conn = DatabaseUtils.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
          stmt.setString(1, status);
          try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
              orders.add(mapResultSetToOrder(rs));
            }
          }
        }
      }
      return orders;
    });
  }

  /**
   * Update the status of an order. Returns true when the update affected a row.
   */
  public boolean updateStatus(int orderId, String newStatus) throws SQLException {
    String sql = "UPDATE orders SET status = ? WHERE order_id = ?";
    try (Connection conn = DatabaseUtils.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setString(1, newStatus);
      stmt.setInt(2, orderId);
      int rows = stmt.executeUpdate();
      return rows > 0;
    }
  }

  public List<OrderItem> findOrderItemsByOrderId(int orderId) throws SQLException {
    List<OrderItem> items = new ArrayList<>();
    String sql = "SELECT id,order_id, product_id, quantity, unit_price FROM order_items WHERE order_id = ?";
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
    order.setStatus(rs.getString("status"));
    return order;
  }

  private OrderItem mapResultSetToOrderItem(ResultSet rs) throws SQLException {
    OrderItem item = new OrderItem(
        rs.getInt("product_id"),
        rs.getInt("quantity"),
        rs.getDouble("unit_price")
    );
    item.setId(rs.getInt("id"));
    item.setOrderId(rs.getInt("order_id"));
    return item;
  }
}
