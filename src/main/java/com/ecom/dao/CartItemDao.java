package com.ecom.dao;

import com.ecom.models.Cart;
import com.ecom.models.CartItem;
import com.ecom.utils.DatabaseUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CartItemDao {
    private static final Logger LOGGER = Logger.getLogger(CartItemDao.class.getName());

    public void createCartItem(CartItem cartItem) {
       String sql = "INSERT INTO cart_item(cart_id,product_id,quantity,unit_price,total_price) VALUES(?,?,?,?,?)";

       try(Connection connection = DatabaseUtils.getConnection()) {
           PreparedStatement preparedStatement = connection.prepareStatement(sql);
           preparedStatement.setInt(1, cartItem.getCartId());
              preparedStatement.setInt(2, cartItem.getProductId());
                preparedStatement.setInt(3, cartItem.getQuantity());
                preparedStatement.setDouble(4, cartItem.getUnitPrice());
                preparedStatement.setDouble(5, cartItem.getTotalPrice());
           int update  = preparedStatement.executeUpdate();
           if(update>0){
                LOGGER.log(Level.INFO, "Cart item created successfully for cartId={0} productId={1}", new Object[]{cartItem.getCartId(), cartItem.getProductId()});
           }else {
               LOGGER.log(Level.WARNING, "Failed to create cart item for cartId={0} productId={1}", new Object[]{cartItem.getCartId(), cartItem.getProductId()});
           }
       } catch (SQLException e) {
           throw new RuntimeException(e);
       }
    }

    // Transactional overloads
    public void createCartItem(Connection conn, CartItem cartItem) throws SQLException {
        String sql = "INSERT INTO cart_item(cart_id,product_id,quantity,unit_price,total_price) VALUES(?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, cartItem.getCartId());
            ps.setInt(2, cartItem.getProductId());
            ps.setInt(3, cartItem.getQuantity());
            ps.setDouble(4, cartItem.getUnitPrice());
            ps.setDouble(5, cartItem.getTotalPrice());
            int update = ps.executeUpdate();
            if (update > 0) {
                LOGGER.log(Level.FINE, "Cart item created (tx) cartId={0} productId={1}", new Object[]{cartItem.getCartId(), cartItem.getProductId()});
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys != null && keys.next()) {
                        cartItem.setId(keys.getInt(1));
                    }
                }
            } else {
                throw new SQLException("Failed to create cart item");
            }
        }
    }

    public void updateCartItemQuantity(int cartItemId, int newQuantity, double newTotalPrice) {
        String sql = "UPDATE cart_item SET quantity = ?, total_price = ? WHERE id = ?";

        try (Connection connection = DatabaseUtils.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, newQuantity);
            preparedStatement.setDouble(2, newTotalPrice);
            preparedStatement.setInt(3, cartItemId);

            int update = preparedStatement.executeUpdate();
            if (update > 0) {
                LOGGER.log(Level.INFO, "Cart item updated successfully id={0}", cartItemId);
            } else {
                LOGGER.log(Level.WARNING, "Failed to update cart item id={0}", cartItemId);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateCartItemQuantity(Connection conn, int cartItemId, int newQuantity, double newTotalPrice) throws SQLException {
        String sql = "UPDATE cart_item SET quantity = ?, total_price = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newQuantity);
            ps.setDouble(2, newTotalPrice);
            ps.setInt(3, cartItemId);
            int update = ps.executeUpdate();
            if (update == 0) throw new SQLException("Failed to update cart item quantity id=" + cartItemId);
        }
    }

    public CartItem getCartItemById(int cartItemId) {
        String sql = "SELECT * FROM cart_item WHERE id = ?";
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
        String sql = "DELETE FROM cart_item WHERE id = ?";
        try (Connection connection = DatabaseUtils.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, cartItemId);

            int delete = preparedStatement.executeUpdate();
            if (delete > 0) {
                LOGGER.log(Level.INFO, "Cart item deleted successfully id={0}", cartItemId);
            } else {
                LOGGER.log(Level.WARNING, "Failed to delete cart item id={0}", cartItemId);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteCartItemById(Connection conn, int cartItemId) throws SQLException {
        String sql = "DELETE FROM cart_item WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cartItemId);
            int delete = ps.executeUpdate();
            if (delete == 0) throw new SQLException("Failed to delete cart item id=" + cartItemId);
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
        String sql = "SELECT * FROM cart_item WHERE cart_id = ? AND product_id = ?";
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

    public CartItem findByCartIdAndProductId(Connection conn, int id, int productId) throws SQLException {
        String sql = "SELECT * FROM cart_item WHERE cart_id = ? AND product_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapResultSetToCartItem(rs);
                return null;
            }
        }
    }

    public List<CartItem> getAllCartItemsByCardId(int cart_id) {
        String sql = "SELECT * FROM cart_item where cart_id = ?";
        try(Connection connection = DatabaseUtils.getConnection()){
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, cart_id);
            ResultSet resultSet = preparedStatement.executeQuery();
            List<CartItem> cartItems = new java.util.ArrayList<>();
            while(resultSet.next()) {
                cartItems.add( mapResultSetToCartItem(resultSet));
            }
            return cartItems;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<CartItem> getAllCartItemsByCardId(Connection conn, int cart_id) throws SQLException {
        String sql = "SELECT * FROM cart_item where cart_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cart_id);
            try (ResultSet rs = ps.executeQuery()) {
                List<CartItem> cartItems = new java.util.ArrayList<>();
                while(rs.next()) {
                    cartItems.add(mapResultSetToCartItem(rs));
                }
                return cartItems;
            }
        }
    }

}
