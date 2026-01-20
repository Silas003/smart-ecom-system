package com.ecom.dao;
import com.ecom.models.Category;
import com.ecom.utils.DatabaseUtils;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDao {
    
  public void create(Category category) throws SQLException {
    String sql = "INSERT INTO categories (name) VALUES (?)";
    try (Connection conn = DatabaseUtils.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      stmt.setString(1, category.getName());
      stmt.executeUpdate();

      try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
        if (generatedKeys.next()) {
          category.setCategoryId(generatedKeys.getInt(1));
        }
      }
    }
  }

  public Category findById(int id) throws SQLException {
    String sql = "SELECT * FROM categories WHERE id = ?";
    try (Connection conn =  DatabaseUtils.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setInt(1, id);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          return mapResultSetToCategory(rs);
        }
      }
    }
      return null;
  }

  public List<Category> findAll() throws SQLException {
    List<Category> categories = new ArrayList<>();
    String sql = "SELECT * FROM categories";
    try (Connection conn =  DatabaseUtils.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql)) {

      while (rs.next()) {
        categories.add(mapResultSetToCategory(rs));
      }
    }
      return categories;
  }

  public void update(Category category) throws SQLException {
    String sql = "UPDATE categories SET name = ? WHERE id = ?";
    try (Connection conn =  DatabaseUtils.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, category.getName());
      stmt.setInt(2, category.getCategoryId());
      stmt.executeUpdate();
    }
  }

  public void delete(int id) throws SQLException {
    String sql = "DELETE FROM categories WHERE id = ?";
    try (Connection conn =  DatabaseUtils.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setInt(1, id);
      stmt.executeUpdate();
    }
  }

  private Category mapResultSetToCategory(ResultSet rs) throws SQLException {
    return new Category(rs.getInt("id"), rs.getString("name"));
  }
    
}
