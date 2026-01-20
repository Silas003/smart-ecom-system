package com.ecom.dao;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.ecom.models.Product;
import com.ecom.utils.DatabaseUtils;
import com.ecom.utils.QueryTimer;

public class ProductDao {

  public void create(Product product) throws SQLException {
    String sql =
        "INSERT INTO products (category_id, name, price, stock_quantity) VALUES (?, ?, ?, ?)";
    try (Connection conn =  DatabaseUtils.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      if (product.getCategoryId() > 0) {
        stmt.setInt(1, product.getCategoryId());
      } else {
        stmt.setNull(1, Types.INTEGER);
      }
      stmt.setString(2, product.getName());
      stmt.setDouble(3, product.getPrice());
      stmt.setInt(4, product.getStockQuantity());

      stmt.executeUpdate();

      try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
        if (generatedKeys.next()) {
          product.setProductId(generatedKeys.getInt(1));
        }
      }

      // create inventory row as source-of-truth for stock (Postgres upsert)
      String invSql = "INSERT INTO inventory (product_id, quantity_in_stock) VALUES (?, ?) ON CONFLICT (product_id) DO UPDATE SET quantity_in_stock = EXCLUDED.quantity_in_stock";
      try (PreparedStatement invStmt = conn.prepareStatement(invSql)) {
        invStmt.setInt(1, product.getProductId());
        invStmt.setInt(2, product.getStockQuantity());
        invStmt.executeUpdate();
      }
    }
  }

  public Product findById(int id) {
    return QueryTimer.measure("product_findById", () -> {
      String sql = "SELECT p.id, p.category_id, p.name, p.price, COALESCE(i.quantity_in_stock, p.stock_quantity) AS stock_quantity "
          + "FROM products p LEFT JOIN inventory i ON p.id = i.product_id WHERE p.id = ?";
      try (Connection conn =  DatabaseUtils.getConnection();
          PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setInt(1, id);
        try (ResultSet rs = stmt.executeQuery()) {
          if (rs.next()) {
            return mapResultSetToProduct(rs);
          }
        }
      }
        return null;
    });
  }

  public List<Product> findAll() throws SQLException {
    return QueryTimer.measure("product_findAll", () -> {
      List<Product> products = new ArrayList<>();
      String sql = "SELECT p.id, p.category_id, p.name, p.price, COALESCE(i.quantity_in_stock, p.stock_quantity) AS stock_quantity FROM products p LEFT JOIN inventory i ON p.id = i.product_id";
      try (Connection conn =  DatabaseUtils.getConnection();
          Statement stmt = conn.createStatement();
          ResultSet rs = stmt.executeQuery(sql)) {

        while (rs.next()) {
          products.add(mapResultSetToProduct(rs));
        }
      }
        return products;
    });
  }

  public List<Product> findByCategoryId(int categoryId) {
    return QueryTimer.measure("product_findByCategoryId", () -> {
      List<Product> products = new ArrayList<>();
      String sql = "SELECT p.id, p.category_id, p.name, p.price, COALESCE(i.quantity_in_stock, p.stock_quantity) AS stock_quantity FROM products p LEFT JOIN inventory i ON p.id = i.product_id WHERE p.category_id = ?";
      try (Connection conn =  DatabaseUtils.getConnection();
          PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setInt(1, categoryId);
        try (ResultSet rs = stmt.executeQuery()) {
          while (rs.next()) {
            products.add(mapResultSetToProduct(rs));
          }
        }
      }
        return products;
    });
  }

  public void update(Product product) throws SQLException {
    String sql =
        "UPDATE products SET category_id = ?, name = ?, price = ?, stock_quantity = ? WHERE"
            + " id = ?";
    try (Connection conn =  DatabaseUtils.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      if (product.getCategoryId() > 0) {
        stmt.setInt(1, product.getCategoryId());
      } else {
        stmt.setNull(1, Types.INTEGER);
      }
      stmt.setString(2, product.getName());
      stmt.setDouble(3, product.getPrice());
      stmt.setInt(4, product.getStockQuantity());
      stmt.setInt(5, product.getProductId());

      stmt.executeUpdate();

      String invSql = "INSERT INTO inventory (product_id, quantity_in_stock) VALUES (?, ?) ON CONFLICT (product_id) DO UPDATE SET quantity_in_stock = EXCLUDED.quantity_in_stock";
      try (PreparedStatement invStmt = conn.prepareStatement(invSql)) {
        invStmt.setInt(1, product.getProductId());
        invStmt.setInt(2, product.getStockQuantity());
        invStmt.executeUpdate();
      }
    }
  }

  public void delete(int id) throws SQLException {
    String sql = "DELETE FROM products WHERE id = ?";
    try (Connection conn =  DatabaseUtils.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setInt(1, id);
      stmt.executeUpdate();

      // cleanup inventory row if exists
      try (PreparedStatement inv = conn.prepareStatement("DELETE FROM inventory WHERE product_id = ?")) {
        inv.setInt(1, id);
        inv.executeUpdate();
      }
    }
  }

  private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
    return new Product(
        rs.getInt("id"),
        rs.getInt("category_id"),
        rs.getString("name"),
        rs.getDouble("price"),
        rs.getInt("stock_quantity"));
  }

  // Search with pagination, sorting and optional category filter
  public List<Product> search(String q, Integer categoryId, int offset, int limit, String sortBy, boolean asc) throws SQLException {
    return QueryTimer.measure("product_search", () -> {
      List<Product> products = new ArrayList<>();
      StringBuilder base = new StringBuilder("SELECT p.id, p.category_id, p.name, p.price, COALESCE(i.quantity_in_stock, p.stock_quantity) AS stock_quantity FROM products p LEFT JOIN inventory i ON p.id = i.product_id WHERE LOWER(name) LIKE ?");
      if (categoryId != null) {
        base.append(" AND category_id = ?");
      }
      String order = "";
      if (sortBy != null && !sortBy.isBlank()) {
        if ("name".equals(sortBy) || "price".equals(sortBy) || "stock_quantity".equals(sortBy)) {
          order = " ORDER BY " + sortBy + (asc ? " ASC" : " DESC");
        }
      }
      base.append(order).append(" LIMIT ? OFFSET ?");
      String sql = base.toString();
      try (Connection conn = DatabaseUtils.getConnection();
           PreparedStatement stmt = conn.prepareStatement(sql)) {
        int idx = 1;
        String like = "%" + (q == null ? "" : q.toLowerCase()) + "%";
        stmt.setString(idx++, like);
        if (categoryId != null) {
          stmt.setInt(idx++, categoryId);
        }
        stmt.setInt(idx++, limit);
        stmt.setInt(idx, offset);
        try (ResultSet rs = stmt.executeQuery()) {
          while (rs.next()) {
            products.add(mapResultSetToProduct(rs));
          }
        }
      }
      return products;
    });
  }

  public int count(String q, Integer categoryId) {
    return QueryTimer.measure("product_count", () -> {
      StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM products WHERE LOWER(name) LIKE ?");
      if (categoryId != null) sql.append(" AND category_id = ?");
      try (Connection conn = DatabaseUtils.getConnection();
           PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
        String like = "%" + (q == null ? "" : q.toLowerCase()) + "%";
        stmt.setString(1, like);
        if (categoryId != null) {
          stmt.setInt(2, categoryId);
        }
        try (ResultSet rs = stmt.executeQuery()) {
          return rs.next() ? rs.getInt(1) : 0;
        }
      }
    });
  }

  // New helper: find product by exact name (case-insensitive)
  public Product findByName(String name) throws SQLException {
    return QueryTimer.measure("product_findByName", () -> {
      String sql = "SELECT p.id, p.category_id, p.name, p.price, COALESCE(i.quantity_in_stock, p.stock_quantity) AS stock_quantity FROM products p LEFT JOIN inventory i ON p.id = i.product_id WHERE LOWER(p.name) = LOWER(?) LIMIT 1";
      try (Connection conn = DatabaseUtils.getConnection();
           PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, name);
        try (ResultSet rs = stmt.executeQuery()) {
          if (rs.next()) return mapResultSetToProduct(rs);
        }
      }
      return null;
    });
  }
}
