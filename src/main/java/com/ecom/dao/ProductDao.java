package com.ecom.dao;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.ecom.models.Product;
import com.ecom.utils.DatabaseUtils;

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
    }
  }

  public Product findById(int id) throws SQLException {
    String sql = "SELECT * FROM products WHERE product_id = ?";
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
  }

  public List<Product> findAll() throws SQLException {
    List<Product> products = new ArrayList<>();
    String sql = "SELECT * FROM products";
    try (Connection conn =  DatabaseUtils.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql)) {

      while (rs.next()) {
        products.add(mapResultSetToProduct(rs));
      }
    }
      return products;
  }

  public List<Product> findByCategoryId(int categoryId) throws SQLException {
    List<Product> products = new ArrayList<>();
    String sql = "SELECT * FROM products WHERE category_id = ?";
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
  }

  public void update(Product product) throws SQLException {
    String sql =
        "UPDATE products SET category_id = ?, name = ?, price = ?, stock_quantity = ? WHERE"
            + " product_id = ?";
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
    }
  }

  public void delete(int id) throws SQLException {
    String sql = "DELETE FROM products WHERE product_id = ?";
    try (Connection conn =  DatabaseUtils.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setInt(1, id);
      stmt.executeUpdate();
    }
  }

  private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
    return new Product(
        rs.getInt("product_id"),
        rs.getInt("category_id"),
        rs.getString("name"),
        rs.getDouble("price"),
        rs.getInt("stock_quantity"));
  }

  // Search with pagination and sorting - placeholders and parameter indexes must match
  public List<Product> search(String q, int offset, int limit, String sortBy, boolean asc) throws SQLException {
    List<Product> products = new ArrayList<>();
    String base = "SELECT * FROM products WHERE LOWER(name) LIKE ? ";
    String order = "";
    if (sortBy != null && !sortBy.isBlank()) {
      // whitelist allowed sort columns
      if ("name".equals(sortBy) || "price".equals(sortBy) || "stock_quantity".equals(sortBy)) {
        order = " ORDER BY " + sortBy + (asc ? " ASC" : " DESC");
      }
    }
    String sql = base + order + " LIMIT ? OFFSET ?";
    try (Connection conn = DatabaseUtils.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
      String like = "%" + (q == null ? "" : q.toLowerCase()) + "%";
      stmt.setString(1, like);
      stmt.setInt(2, limit);
      stmt.setInt(3, offset);
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          products.add(mapResultSetToProduct(rs));
        }
      }
    }
    return products;
  }

  public int count(String q) throws SQLException {
    String sql = "SELECT COUNT(*) FROM products WHERE LOWER(name) LIKE ?";
    try (Connection conn = DatabaseUtils.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
      String like = "%" + (q == null ? "" : q.toLowerCase()) + "%";
      stmt.setString(1, like);
      try (ResultSet rs = stmt.executeQuery()) {
        return rs.next() ? rs.getInt(1) : 0;
      }
    }
  }
}
