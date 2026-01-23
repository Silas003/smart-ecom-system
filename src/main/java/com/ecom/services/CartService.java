package com.ecom.services;

import com.ecom.dao.CartDao;
import com.ecom.dao.CartItemDao;
import com.ecom.models.Cart;
import com.ecom.models.CartItem;
import com.ecom.models.Product;
import com.ecom.models.User;
import com.ecom.utils.DatabaseUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Singleton service for managing the user's shopping cart in-memory.
 */
public class CartService {
    private static final Logger LOGGER = Logger.getLogger(CartService.class.getName());
    private static CartService instance;
    // productId -> quantity, per-application default map for anonymous sessions
    private static Map<Integer, Integer> cart = new ConcurrentHashMap<>();
    private static final Map<Integer, Map<Integer,Integer>> cartsByUser = new ConcurrentHashMap<>();
    private static CartDao cartDao;
    private static CartItemDao cartItemDao;
    private static SessionService sessionService;
    private CartService() {
    }

    public static CartService getInstance() {
        if (instance == null) {
            instance = new CartService();
            sessionService = SessionService.getInstance();
            cartDao = new CartDao();
            cartItemDao = new CartItemDao();
            // cart already initialized above
        }
        return instance;
    }

    // Dependency injection setters for testability
    public void setCartDao(CartDao dao) { cartDao = dao; }
    public void setCartItemDao(CartItemDao dao) { cartItemDao = dao; }
    public void setSessionService(SessionService svc) { sessionService = svc; }

    private Map<Integer,Integer> getInMemoryCartForCurrentUser() {
        User user = null;
        try { user = sessionService.getCurrentUser(); } catch (Exception e) { user = null; }
        if (user == null) return cart; // fallback to shared anonymous cart
        return cartsByUser.computeIfAbsent(user.getUserId(), k -> new ConcurrentHashMap<>());
    }

    public void addToCart(Product product, int quantity) {
        if (product == null || quantity <= 0) return;

        User user = null;
        try { user = sessionService.getCurrentUser(); } catch (Exception e) { user = null; }

        if (user == null) {
            // anonymous: update in-memory only
            Map<Integer,Integer> inMemory = getInMemoryCartForCurrentUser();
            inMemory.put(product.getProductId(), inMemory.getOrDefault(product.getProductId(), 0) + quantity);
            return;
        }

        // logged-in user: do DB op inside transaction, then update cache on success
        Connection conn = null;
        try {
            conn = DatabaseUtils.getConnection();
            conn.setAutoCommit(false);

            Cart dbCart = cartDao.getCartActiveByUserId(conn, user.getUserId());
            if (dbCart == null) {
                dbCart = new Cart(user.getUserId(), "active");
                cartDao.createCart(conn, dbCart);
            }

            CartItem existing = cartItemDao.findByCartIdAndProductId(conn, dbCart.getId(), product.getProductId());
            if (existing != null) {
                int newQty = existing.getQuantity() + quantity;
                cartItemDao.updateCartItemQuantity(conn, existing.getId(), newQty, existing.getUnitPrice() * newQty);
            } else {
                CartItem cartItem = new CartItem();
                cartItem.setProductId(product.getProductId());
                cartItem.setUnitPrice(product.getPrice());
                cartItem.setTotalPrice(product.getPrice() * quantity);
                cartItem.setQuantity(quantity);
                cartItem.setCartId(dbCart.getId());
                cartItemDao.createCartItem(conn, cartItem);
            }

            conn.commit();

            // update in-memory cache for this user after commit
            Map<Integer,Integer> inMemory = getInMemoryCartForCurrentUser();
            inMemory.put(product.getProductId(), inMemory.getOrDefault(product.getProductId(), 0) + quantity);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to add to cart for user", e);
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { LOGGER.log(Level.SEVERE, "Rollback failed", ex); }
            }
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) { LOGGER.log(Level.WARNING, "Failed to close connection", ex); }
            }
        }
    }

    public void removeFromCart(int productId) {
        User user = null;
        try { user = sessionService.getCurrentUser(); } catch (Exception e) { user = null; }
        if (user == null) {
            Map<Integer,Integer> inMemory = getInMemoryCartForCurrentUser();
            inMemory.remove(productId);
            return;
        }

        Connection conn = null;
        try {
            conn = DatabaseUtils.getConnection();
            conn.setAutoCommit(false);
            Cart dbCart = cartDao.getCartActiveByUserId(conn, user.getUserId());
            if (dbCart != null) {
                CartItem item = cartItemDao.findByCartIdAndProductId(conn, dbCart.getId(), productId);
                if (item != null) {
                    cartItemDao.deleteCartItemById(conn, item.getId());
                }
            }
            conn.commit();
            Map<Integer,Integer> inMemory = getInMemoryCartForCurrentUser();
            inMemory.remove(productId);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to remove item from cart", e);
            if (conn != null) { try { conn.rollback(); } catch (SQLException ex) { LOGGER.log(Level.SEVERE, "Rollback failed", ex); } }
            throw new RuntimeException(e);
        } finally {
            if (conn != null) { try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) { LOGGER.log(Level.WARNING, "Failed to close connection", ex); } }
        }
    }

    public void updateQuantity(int productId, int quantity) {
        User user = null;
        try { user = sessionService.getCurrentUser(); } catch (Exception e) { user = null; }
        if (user == null) {
            Map<Integer,Integer> inMemory = getInMemoryCartForCurrentUser();
            if (quantity <= 0) inMemory.remove(productId); else inMemory.put(productId, quantity);
            return;
        }

        Connection conn = null;
        try {
            conn = DatabaseUtils.getConnection();
            conn.setAutoCommit(false);
            Cart dbCart = cartDao.getCartActiveByUserId(conn, user.getUserId());
            if (dbCart != null) {
                CartItem item = cartItemDao.findByCartIdAndProductId(conn, dbCart.getId(), productId);
                if (item != null) {
                    if (quantity <= 0) {
                        cartItemDao.deleteCartItemById(conn, item.getId());
                    } else {
                        cartItemDao.updateCartItemQuantity(conn, item.getId(), quantity, item.getUnitPrice() * quantity);
                    }
                }
            }
            conn.commit();
            Map<Integer,Integer> inMemory = getInMemoryCartForCurrentUser();
            if (quantity <= 0) inMemory.remove(productId); else inMemory.put(productId, quantity);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to update cart item quantity", e);
            if (conn != null) { try { conn.rollback(); } catch (SQLException ex) { LOGGER.log(Level.SEVERE, "Rollback failed", ex); } }
            throw new RuntimeException(e);
        } finally {
            if (conn != null) { try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) { LOGGER.log(Level.WARNING, "Failed to close connection", ex); } }
        }
    }

    public int getQuantity(int productId) {
        Map<Integer,Integer> inMemory = getInMemoryCartForCurrentUser();
        return inMemory.getOrDefault(productId, 0);
    }

    public Map<Integer, Integer> getCart() {
        Map<Integer,Integer> inMemory = getInMemoryCartForCurrentUser();
        return new HashMap<>(inMemory);
    }

    public void clearCart() {
        Map<Integer,Integer> inMemory = getInMemoryCartForCurrentUser();
        inMemory.clear();
    }

    public int getTotalItems() {
        Map<Integer,Integer> inMemory = getInMemoryCartForCurrentUser();
        return inMemory.values().stream().mapToInt(Integer::intValue).sum();

    }

    public boolean isEmpty() {
        Map<Integer,Integer> inMemory = getInMemoryCartForCurrentUser();
        return inMemory.isEmpty();
    }
    public void getCartItemsFromDb(int userId) {
        Cart cartDb = cartDao.getCartActiveByUserId(userId);
        if (cartDb != null) {
            List<CartItem> items = cartItemDao.getAllCartItemsByCardId(cartDb.getId());
            Map<Integer,Integer> userMap = new ConcurrentHashMap<>();
            items.forEach(item-> userMap.put(item.getProductId(), item.getQuantity()));
            cartsByUser.put(userId, userMap);
        }
    }
}
