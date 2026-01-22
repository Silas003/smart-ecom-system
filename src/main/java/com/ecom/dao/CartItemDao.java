package com.ecom.dao;

import com.ecom.models.Cart;
import com.ecom.models.CartItem;
import com.ecom.utils.DatabaseUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CartItemDao {

    public void createCartItem(CartItem cartItem) {
       String sql = "INSERT INTO cart_items(cart_id,product_id,quantity,unit_price,total_price) VALUES(?,?,?,?,?)";

       try(Connection connection = DatabaseUtils.getConnection()) {
           PreparedStatement preparedStatement = connection.prepareStatement(sql);
           preparedStatement.setInt(1, cartItem.getCartId());
              preparedStatement.setInt(2, cartItem.getProductId());
                preparedStatement.setInt(3, cartItem.getQuantity());
                preparedStatement.setDouble(4, cartItem.getUnitPrice());
                preparedStatement.setDouble(5, cartItem.getTotalPrice());
           int update  = preparedStatement.executeUpdate();
           if(update>0){
                System.out.println("Cart item created successfully");
           }else {
               System.out.println("Failed to create cart item");
           }
       } catch (SQLException e) {
           throw new RuntimeException(e);
       }
    }

    public void updateCartItemQuantity(int cartItemId, int newQuantity, double newTotalPrice) {
        String sql = "UPDATE cart_items SET quantity = ?, total_price = ? WHERE id = ?";

        try (Connection connection = DatabaseUtils.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, newQuantity);
            preparedStatement.setDouble(2, newTotalPrice);
            preparedStatement.setInt(3, cartItemId);

            int update = preparedStatement.executeUpdate();
            if (update > 0) {
                System.out.println("Cart item updated successfully");
            } else {
                System.out.println("Failed to update cart item");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public CartItem getCartItemById(int cartItemId) {
        String sql = "SELECT * FROM cart_items WHERE id = ?";
        try (Connection connection = DatabaseUtils.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, cartItemId);

            var resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
               return mapResultSetToCartItem(resultSet);
            } return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void  deleteCartItemById(int cartItemId) {
        String sql = "DELETE FROM cart_items WHERE id = ?";
        try (Connection connection = DatabaseUtils.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, cartItemId);

            int delete = preparedStatement.executeUpdate();
            if (delete > 0) {
                System.out.println("Cart item deleted successfully");
            } else {
                System.out.println("Failed to delete cart item");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public CartItem mapResultSetToCartItem(ResultSet resultSet) throws SQLException {
        CartItem cartItem = new CartItem();
        cartItem.setId(resultSet.getInt("id"));
        cartItem.setCartId(resultSet.getInt("cart_id"));
        cartItem.setProductId(resultSet.getInt("product_id"));
        cartItem.setQuantity(resultSet.getInt("quantity"));
        cartItem.setUnitPrice(resultSet.getDouble("unit_price"));
        cartItem.setTotalPrice(resultSet.getDouble("total_price"));
        return cartItem;
    }

    public CartItem findByCartIdAndProductId(int id, int productId) {
        String sql = "SELECT * FROM cart_items WHERE cart_id = ? AND product_id = ?";
        try (Connection connection = DatabaseUtils.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, id);
            preparedStatement.setInt(2, productId);

            var resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                return mapResultSetToCartItem(resultSet);
            } return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
