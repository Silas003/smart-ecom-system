package com.ecom.dao;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.ecom.models.Product;
import com.ecom.utils.DatabaseUtils;
public class ProductDao {

  public void create(Product product) throws SQLException {
    String sql =
        "INSERT INTO products (category_id, name, price, brand,stock_quantity) VALUES (?, ?, ?, ?)";
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
    } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
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
    } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
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
    } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
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
    } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
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
    } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
    }
  }

  public void delete(int id) throws SQLException {
    String sql = "DELETE FROM products WHERE product_id = ?";
    try (Connection conn =  DatabaseUtils.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setInt(1, id);
      stmt.executeUpdate();
    } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
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
}
