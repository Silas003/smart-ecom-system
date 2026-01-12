package com.ecom.dao;

import com.ecom.models.Reviews;
import com.ecom.utils.DatabaseUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReviewsDao {

    public static void create(int userId, int productId, String description, int stars) throws SQLException {
        String sql = "INSERT INTO reviews(user_id, product_id, stars, description) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseUtils.getConnection()) {
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, productId);
            preparedStatement.setInt(3, stars);
            preparedStatement.setString(4, description);
            int rd = preparedStatement.executeUpdate();
            if (rd > 0) {
                System.out.println("Review created successfully.");
            }
        }
    }

    public static List<Reviews> findAll() throws SQLException {
        List<Reviews> reviews = new ArrayList<>();
        String sql = "SELECT id, user_id, product_id, stars, description FROM reviews";
        try (Connection conn = DatabaseUtils.getConnection()) {
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                reviews.add(mapResultSetToReview(rs));
            }
        }
        return reviews;
    }

    public static List<Reviews> findByProductId(int productId) throws SQLException {
        List<Reviews> reviews = new ArrayList<>();
        String sql = "SELECT id, user_id, product_id, stars, description FROM reviews WHERE product_id = ?";
        try (Connection conn = DatabaseUtils.getConnection()) {
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1, productId);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                reviews.add(mapResultSetToReview(rs));
            }
        }
        return reviews;
    }

    public static List<Reviews> findByUserId(int userId) throws SQLException {
        List<Reviews> reviews = new ArrayList<>();
        String sql = "SELECT id, user_id, product_id, stars, description FROM reviews WHERE user_id = ?";
        try (Connection conn = DatabaseUtils.getConnection()) {
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1, userId);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                reviews.add(mapResultSetToReview(rs));
            }
        }
        return reviews;
    }

    public static Reviews findById(int id) throws SQLException {
        String sql = "SELECT id, user_id, product_id, stars, description FROM reviews WHERE id = ?";
        try (Connection conn = DatabaseUtils.getConnection()) {
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1, id);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                return mapResultSetToReview(rs);
            }
        }
        return null;
    }

    public static void update(int id, int stars, String description) throws SQLException {
        String sql = "UPDATE reviews SET stars = ?, description = ? WHERE id = ?";
        try (Connection conn = DatabaseUtils.getConnection()) {
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1, stars);
            preparedStatement.setString(2, description);
            preparedStatement.setInt(3, id);
            int rd = preparedStatement.executeUpdate();
            if (rd > 0) {
                System.out.println("Review updated successfully.");
            }
        }
    }

    public static void delete(int id) throws SQLException {
        String sql = "DELETE FROM reviews WHERE id = ?";
        try (Connection conn = DatabaseUtils.getConnection()) {
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1, id);
            int rd = preparedStatement.executeUpdate();
            if (rd > 0) {
                System.out.println("Review deleted successfully.");
            }
        }
    }

    private static Reviews mapResultSetToReview(ResultSet rs) throws SQLException {
        // Map database fields (stars, description) to model fields (rating, comment)
        Reviews review = new Reviews(
            rs.getInt("id"),
            rs.getInt("user_id"),
            rs.getInt("product_id"),
            rs.getInt("stars"), // stars -> rating
            rs.getString("description") // description -> comment
        );
        return review;
    }
}
