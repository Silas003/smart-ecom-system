package com.ecom.dao;

import com.ecom.models.Cart;
import com.ecom.utils.DatabaseUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class CartDao {

    public void  createCart(Cart cart) {
        String sql = "INSERT INTO carts(user_id,status,amount) values(?,?,?)";
        try(Connection conn = DatabaseUtils.getConnection()) {
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1, cart.getUserId());
            preparedStatement.setString(2, cart.getStatus());
            preparedStatement.setDouble(3, cart.getAmount());
            int update = preparedStatement.executeUpdate();
            if(update > 0) {
                System.out.println("Cart created successfully");
            } else {
                System.out.println("Failed to create cart");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
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
        String sql = "UPDATE carts SET user_id = ?, status = ?, amount = ? WHERE id = ?";
        try(Connection connection = DatabaseUtils.getConnection()){
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, cart.getUserId());
            preparedStatement.setString(2, cart.getStatus());
            preparedStatement.setDouble(3, cart.getAmount());
            preparedStatement.setInt(4, id);
            int update = preparedStatement.executeUpdate();
            if(update > 0) {
                System.out.println("Cart updated successfully");
            } else {
                System.out.println("Failed to update cart");
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
                System.out.println("Cart deleted successfully");
            } else {
                System.out.println("Failed to delete cart");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public Cart getCartActiveByUserId(int userId) {
        String sql = "SELECT * FROM carts where user_id = ? and status = 'ACTIVE'";
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
    public static Cart mapResultSetToCart(ResultSet rs) throws SQLException {
        return new Cart(
            rs.getInt("id"),
            rs.getInt("user_id"),
            rs.getString("status"),
            rs.getDouble("amount"),
            rs.getTimestamp("created_at").toLocalDateTime()
        );
    }


}
