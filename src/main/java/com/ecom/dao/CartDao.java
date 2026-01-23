package com.ecom.dao;

import com.ecom.models.Cart;
import com.ecom.utils.DatabaseUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CartDao {
    private static final Logger LOGGER = Logger.getLogger(CartDao.class.getName());

    public void  createCart(Cart cart) {
        String sql = "INSERT INTO carts(user_id,status) values(?,?)";
        try(Connection conn = DatabaseUtils.getConnection();
            PreparedStatement preparedStatement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setInt(1, cart.getUserId());
            preparedStatement.setString(2, cart.getStatus());
            int update = preparedStatement.executeUpdate();
            if(update > 0) {
                // retrieve generated id and set it on the model
                try (ResultSet keys = preparedStatement.getGeneratedKeys()) {
                    if (keys != null && keys.next()) {
                        cart.setId(keys.getInt(1));
                    }
                }
                LOGGER.log(Level.INFO, "Cart created successfully with id={0}", cart.getId());
            } else {
                LOGGER.log(Level.WARNING, "Failed to create cart for userId={0}", cart.getUserId());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    // Transactional variant: uses the provided connection and returns with cart.id populated
    public void createCart(Connection conn, Cart cart) throws SQLException {
        String sql = "INSERT INTO carts(user_id,status) values(?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, cart.getUserId());
            ps.setString(2, cart.getStatus());
            int update = ps.executeUpdate();
            if (update > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys != null && keys.next()) {
                        cart.setId(keys.getInt(1));
                    }
                }
                LOGGER.log(Level.INFO, "Cart created (tx) with id={0}", cart.getId());
            } else {
                throw new SQLException("Failed to create cart");
            }
        }
    }

    public Cart getCartById(int id) {
        String sql = "SELECT * FROM carts where id = ?";
        try(Connection connection = DatabaseUtils.getConnection()){
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            while(resultSet.next()) {
                return mapResultSetToCart(resultSet);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public List<Cart> getAllCarts() {
        String sql = "SELECT * FROM carts";
        try(Connection connection = DatabaseUtils.getConnection()){
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();
            List<Cart> carts = new java.util.ArrayList<>();
            while(resultSet.next()) {
                carts.add(mapResultSetToCart(resultSet));
            }
            return carts;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateCart(int id, Cart cart) {
        String sql = "UPDATE carts SET user_id = ?, status = ?,  WHERE id = ?";
        try(Connection connection = DatabaseUtils.getConnection()){
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, cart.getUserId());
            preparedStatement.setString(2, cart.getStatus());
            preparedStatement.setInt(3, id);
            int update = preparedStatement.executeUpdate();
            if(update > 0) {
                LOGGER.log(Level.INFO, "Cart updated successfully id={0}", id);
            } else {
                LOGGER.log(Level.WARNING, "Failed to update cart id={0}", id);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public  void deleteCart(int id) {
        String sql = "DELETE FROM carts WHERE id = ?";
        try(Connection connection = DatabaseUtils.getConnection()){
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, id);
            int update = preparedStatement.executeUpdate();
            if(update > 0) {
                LOGGER.log(Level.INFO, "Cart deleted successfully id={0}", id);
            } else {
                LOGGER.log(Level.WARNING, "Failed to delete cart id={0}", id);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public Cart getCartActiveByUserId(int userId) {
        String sql = "SELECT * FROM carts where user_id = ? and status = 'active'";
        try(Connection connection = DatabaseUtils.getConnection()){
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while(resultSet.next()) {
                return mapResultSetToCart(resultSet);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    // Transactional variant: uses provided connection
    public Cart getCartActiveByUserId(Connection conn, int userId) throws SQLException {
        String sql = "SELECT * FROM carts where user_id = ? and status = 'active'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapResultSetToCart(rs);
                return null;
            }
        }
    }

    public static Cart mapResultSetToCart(ResultSet rs) throws SQLException {
        return new Cart(
            rs.getInt("id"),
            rs.getInt("user_id"),
                rs.getString("status")
        );
    }



}
